package esc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import gnu.io.SerialPort;
import routine.Instruction;

public class AutoQuadEsc32 extends AbstractEsc {
	private ReaderThread reader;

	public AutoQuadEsc32(SerialPort port) throws IOException {
		super(port);
	}

	public String toString() {
		return "AutoQuadEsc32";
	}

	public void executeInstruction(Instruction instruction) {
		switch (instruction.type) {
		case ARM:
			arm();
			break;
		case DISARM:
			disarm();
			break;
		case START:
			start();
			break;
		case STOP:
			stop();
			break;
		case SLEEP:
			long millis = (Long) instruction.parameters.get("millis");
			sleep(millis);
			break;
		case SET_RPM:
			int rpm = (Integer) instruction.parameters.get("rpm");
			setRPM(rpm);
			break;
		case ACCELERATE:
			int from = (Integer) instruction.parameters.get("from");
			int to = (Integer) instruction.parameters.get("to");
			double pace = (Double) instruction.parameters.get("pace");
			accelerate(from, to, pace);
			break;
		case START_TELEMETRY:
			int frequency = (Integer) instruction.parameters.get("frequency");
			startTelemetry(frequency);
			break;
		case STOP_TELEMETRY:
			stopTelemetry();
			break;
		case DIRECTION:
			int direction = ((String) instruction.parameters.get("direction")).equals("forward") ? 1 : -1;
			setDirection(direction);
		default:
			instruction.parameters.clear();
		}
	}

	private AutoQuadEsc32 setDirection(int direction) {
		return sendRawCommand("set DIRECTION " + direction);
	}

	public AutoQuadEsc32 setRPM(int rpm) {
		return sendRawCommand("rpm " + rpm);
	}

	public AutoQuadEsc32 arm() {
		return sendRawCommand("arm");
	}

	public AutoQuadEsc32 disarm() {
		return sendRawCommand("disarm");
	}

	public AutoQuadEsc32 start() {
		return sendRawCommand("start");
	}

	public AutoQuadEsc32 stop() {
		return sendRawCommand("stop");
	}

	/**
	 * @param from
	 *            starting rpm
	 * @param to
	 *            ending rpm
	 * @param pace
	 *            acceleration in rpm / s
	 * @return
	 */
	public AutoQuadEsc32 accelerate(int from, int to, double pace) {
		if (pace == 0 || from == to) {
			throw new IllegalArgumentException("Cannot accelerate");
		}

		if (pace < -400)
			System.out.println("WARNING: deceleration with a rate greater than 400 rpm/s cannot be achieved");

		int deltaRpm = pace > 0 ? 1 : -1;
		long deltaT = Math.round((deltaRpm / pace) * 1000);

		if (from < to && pace > 0) {
			setRPM(from);
			while (from <= to) {
				from += deltaRpm;
				setRPM(from);
				try {
					Thread.sleep(deltaT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (from > to && pace < 0) {
			setRPM(from);
			while (from >= to) {
				from += deltaRpm;
				setRPM(from);
				try {
					Thread.sleep(deltaT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalArgumentException("from, to, pace not compatible");
		}
		return this;
	}

	public AutoQuadEsc32 sendRawCommand(String command) {
		try {
			// l'ESC usa la codifica UTF-8 e ha bisogno dei caratteri di LF e CR
			output.write(command.trim().getBytes("UTF-8"));
			output.write(13); // LF
			output.write(10); // CR
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public AutoQuadEsc32 startTelemetry(int frequency) {
		if (frequency < 0 || frequency > 100) {
			throw new IllegalArgumentException("frequenza non valida per la telemetry " + frequency);
		}
		if (frequency == 0)
			return stopTelemetry();
		if (output == null) {
			throw new IllegalArgumentException("writer nullo");
		}
		sendRawCommand("telemetry " + frequency);
		if (reader != null) {
			reader.shouldRead.set(false);
		}

		reader = new ReaderThread(frequency);
		reader.start();
		return this;
	}

	public AutoQuadEsc32 stopTelemetry() {
		sendRawCommand("telemetry 0");
		if (reader != null) {
			reader.shouldRead.set(false);
		}
		return this;
	}

	@Deprecated
	@Override
	public void addTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.add(parameter);
	}

	@Override
	public void setTelemetryParameters(List<TelemetryParameter> parameters) {
		telemetryParameters.clear();
		telemetryParameters.addAll(parameters);
	}

	@Deprecated
	@Override
	public void removeTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.remove(parameter);
	}

	private class ReaderThread extends Thread {
		private double period;
		private ObjectOutputStream writer;
		private ByteArrayOutputStream inputBuffer;
		protected AtomicBoolean shouldRead = new AtomicBoolean(true);
		private HashMap<TelemetryParameter, Object> bundle;

		public ReaderThread(int telemetryFrequency) {
			this.setName("ReaderThread");
			this.period = 1.0 / telemetryFrequency;
			try {
				this.writer = new ObjectOutputStream(pipedOutput);
			} catch (IOException e) {
				synchronized (pipedOutput) {
					try {
						pipedOutput.wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				try {
					this.writer = new ObjectOutputStream(pipedOutput);
				} catch (IOException nonFallisce) {
					nonFallisce.printStackTrace();
				}
			}
			this.inputBuffer = new ByteArrayOutputStream();
			bundle = new HashMap<>();
		}

		public void run() {
			try {
				Double time = 0.0;

				byte singleData;
				while (shouldRead.get() == true && (singleData = (byte) input.read()) != -1) {
					inputBuffer.write(singleData);
					// letto fine riga -> classe'è un dato da parsare
					if (singleData == '\n') {
						ByteArrayInputStream bin = new ByteArrayInputStream(inputBuffer.toByteArray());
						BufferedReader reader = new BufferedReader(new InputStreamReader(bin, "UTF-8"));
						String line = reader.readLine();
						String[] tokens = line.split("\\s{2,}");
						TelemetryParameter p = null;
						// se c'è almeno un token e 
						// se ha parsato la prima parte della stringa come
						// parametro e questo parametro ci interessa
						if (tokens.length != 0 && (p = TelemetryParameter.valoreDi(tokens[0])) != null
								&& telemetryParameters.contains(p)) {
							Object value = null;
							try {
								if (p.classe == String.class) {
									value = tokens[1];
								} else if (p.classe == Integer.class) {
									value = Integer.parseInt(tokens[1]);
								} else if (p.classe == Double.class) {
									value = Double.parseDouble(tokens[1]);
								}
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignore) {
								// c'è stato un errore di lettura, succede
								// spesso con valori alti di telemetria
								// metto comunque null nel bundle
								System.out.println(line + " " + Arrays.toString(tokens));
								ignore.printStackTrace();
							} finally {
								bundle.put(p, value);
							}

							// il bundle è riempito, lo mando insieme al
							// timestamp
							if (bundle.size() == telemetryParameters.size()) {
								writer.writeDouble(time);
								writer.writeObject(bundle);
								// bundle.clear(); NON FUNZIONA, IL BUNDLE
								// AGGIORNA I VALORI, MA CON WRITEOBJECT INVIA
								// SEMPRE I VALORI VECCHI (CACHE?)
								bundle = new HashMap<>();
								time += period;
							}
						}
						inputBuffer.reset();
					}
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
