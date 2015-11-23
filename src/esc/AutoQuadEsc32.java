package esc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.StringTokenizer;
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
		default:
			instruction.parameters.clear();
		}
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
		if (frequency < 0 || frequency > 60) {
			throw new IllegalArgumentException("frequenza non valida per la telemetry " + frequency);
		}
		if (frequency==0)
			return stopTelemetry();
		if (output == null) {
			throw new IllegalArgumentException("writer nullo");
		}
		sendRawCommand("telemetry " + frequency);
		if (reader != null) {
			reader.shouldRead.set(false);
		}
		reader = new ReaderThread();
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

	public void addTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.add(parameter);
	}

	public void setTelemetryParameters(Set<TelemetryParameter> parameters) {
		telemetryParameters.clear();
		telemetryParameters.addAll(parameters);
	}

	public void removeTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.remove(parameter);
	}

	private class ReaderThread extends Thread {
		private ObjectOutputStream writer;
		private ByteArrayOutputStream inputBuffer;
		protected AtomicBoolean shouldRead = new AtomicBoolean(true);

		public ReaderThread() {
			this.setName("ReaderThread");
			try {
				this.writer = new ObjectOutputStream(pipedOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.inputBuffer = new ByteArrayOutputStream();
		}

		public void run() {
			try {
				byte singleData;
				while (shouldRead.get() == true && (singleData = (byte) input.read()) != -1) {
					inputBuffer.write(singleData);
					if (singleData == 10) {
						ByteArrayInputStream bin = new ByteArrayInputStream(inputBuffer.toByteArray());
						BufferedReader reader = new BufferedReader(new InputStreamReader(bin, "UTF-8"));
						String[] tokens = reader.readLine().split("\\s{2,}");
						TelemetryParameter p = TelemetryParameter.valoreDi(tokens[0]);
						if (p != null && telemetryParameters.contains(p)) {
							Object value = null;
							try {
								if (p.c == String.class) {
									value = tokens[1];
								} else if (p.c == Integer.class) {

									value = Integer.parseInt(tokens[1]);

								} else if (p.c == Double.class) {
									value = Double.parseDouble(tokens[1]);
								}
								writer.writeObject(p);
								writer.writeObject(value);
							} catch (NumberFormatException ignore) {
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
