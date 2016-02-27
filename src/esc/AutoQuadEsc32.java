package esc;

import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import routine.Instruction;

/**
 * Implementazione rappresentante un ESC modello AutoQuadEsc32
 *
 */
public class AutoQuadEsc32 extends AbstractEsc {
	/**
	 * Thread che gestisce la lettura dell'output di un esc.
	 */
	private class ReaderThread extends Thread {
		private double period;
		private ObjectOutputStream writer;
		private ByteArrayOutputStream inputBuffer;
		protected AtomicBoolean shouldRead = new AtomicBoolean(true);
		private HashMap<TelemetryParameter, Object> bundle;

		/**
		 * Alla creazione si imposta la frequenza della telemetria cos� che ai
		 * dati letti venga associato un timestamp indipendente dall'orologio
		 * della JVM che esegue la lettura. <br>
		 * Se il pipedOutput di {@link AbstractEsc} non � stato collegato a un
		 * pipedInput fallisce la creazione di un ObjectOutputStream. Per questo
		 * motivo dopo un primo fallimento il ReaderThread si mette in attesa
		 * che un altro thread chiami notify() su pipedOutput
		 * 
		 * @param telemetryFrequency
		 */
		public ReaderThread(int telemetryFrequency) {
			setName("ReaderThread");
			period = 1.0 / telemetryFrequency;
			// Se il pipedOutput non � stato collegato a un pipedInput
			// fallisce
			// la creazione di un ObjectOutputStream
			// perci� dopo un primo fallimento il ReaderThread si mette in
			// attesa che un altro thread chiami notify()
			// su pipedOutput
			try {
				writer = new ObjectOutputStream(pipedOutput);
			} catch (IOException e) {
				synchronized (pipedOutput) {
					try {
						pipedOutput.wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						return;
					}
				}
				try {
					writer = new ObjectOutputStream(pipedOutput);
				} catch (IOException nonFallisce) {
					nonFallisce.printStackTrace();
					return;
				}
			}
			inputBuffer = new ByteArrayOutputStream();
			bundle = new HashMap<>();
		}

		/**
		 * Se la telemetria � attiva, il thread legge a caratteri dallo stream
		 * della porta seriale fino alla fine dello stream. Una volta raggiunto
		 * un fine linea, il thread analizza la stringa appena trovata. Se la
		 * prima parte di essa identifica un parametro di interesse per la
		 * routine, il {@link TelemetryParameter} e il valore di esso che viene
		 * letto vengono aggiunti ad una mappa appositamente creata. Una volta
		 * che avviene una lettura per ogni parametro di interesse la mappa con
		 * i dati viene inviata sul {@link PipedOutputStream} e riinizializzata.
		 * Nel caso di errori nella lettura di un valore esso viene comunque
		 * inserito nella mappa con valore nullo, sar� compito dell'oggetto che
		 * si occupa dell'analisi dei dati o della rappresentazione di essi di
		 * gestire l'errore. Nel caso di errore nella lettura di un tipo di
		 * parametro invece, l'intero bundle di dati viene scartato e si attende
		 * la successiva lettura.
		 */
		@Override
		public void run() {
			try {
				Double time = 0.0;

				byte singleData;
				while (shouldRead.get() == true
						&& (singleData = (byte) input.read()) != -1) {
					inputBuffer.write(singleData);
					// letto fine riga -> valueClass'è un dato da parsare
					if (singleData == '\n') {
						ByteArrayInputStream bin = new ByteArrayInputStream(
								inputBuffer.toByteArray());
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(bin, "UTF-8"));
						String line = reader.readLine();
						String[] tokens = line.split("\\s{2,}");
						TelemetryParameter p = null;
						// se c'è almeno un token e
						// se ha parsato la prima parte della stringa come
						// parametro e questo parametro ci interessa
						if (tokens.length != 0
								&& (p = TelemetryParameter.parse(tokens[0])) != null
								&& telemetryParameters.contains(p)) {
							Object value = null;
							try {
								if (p.valueClass == String.class)
									value = tokens[1];
								else if (p.valueClass == Integer.class)
									value = Integer.parseInt(tokens[1]);
								else if (p.valueClass == Double.class)
									value = Double.parseDouble(tokens[1]);
							} catch (NumberFormatException
									| ArrayIndexOutOfBoundsException ignore) {
								// c'è stato un errore di lettura, succede
								// spesso con valori alti di telemetria
								// metto comunque null nel bundle
								System.out.println(line + " "
										+ Arrays.toString(tokens));
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

	private ReaderThread reader;

	public AutoQuadEsc32(SerialPort port) throws IOException {
		super(port);
	}

	/**
	 * Permette di effettuare un'accelerazione del motore su un certo intervallo
	 * di RPM. A partire dagli RPM di partenza aumenta di 1 ogni intervallo di
	 * tempo deltaT calcolato in base alla accelerazione fino a raggiungere gli
	 * RPM desiderati. In questo modello di ESC vi è un limite alla
	 * decelerazione di circa -400 rpm/s. Per cambiamenti più rapidi si nota
	 * che l'esc pone a 0V i motor volts e non ottiene la decelerazione
	 * richiesta. Per quanto riguarda l'accelerazione questa sarà limitata
	 * superiormente dal valore per il quale l'esc mette i motor volts a 15V
	 * (tensione di alimentazione).
	 *
	 * @param from
	 *            starting rpm
	 * @param to
	 *            ending rpm
	 * @param pace
	 *            acceleration in rpm/s
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 * @throws IllegalArgumentException
	 *             nel caso di parametri non compatibili
	 *
	 */
	private AutoQuadEsc32 accelerate(int from, int to, double pace) {
		if (pace == 0 || from == to)
			throw new IllegalArgumentException("Cannot accelerate");

		if (pace < -400)
			System.out
					.println("WARNING: deceleration with a rate greater than 400 rpm/s cannot be achieved");

		int deltaRpm = pace > 0 ? 1 : -1;
		long deltaT = Math.round(deltaRpm / pace * 1000);

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
		} else
			throw new IllegalArgumentException("from, to, pace not compatible");
		return this;
	}

	@Deprecated
	@Override
	public void addTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.add(parameter);
	}

	/**
	 * Invia l'istruzione di arm del motore; "arm"
	 *
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 arm() {
		return sendRawCommand("arm");
	}

	/**
	 * Invia l'istruzione di disarm del motore: "disarm"
	 *
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 disarm() {
		return sendRawCommand("disarm");
	}

	/**
	 * Considera tutti i possibili tipi di istruzione definiti in
	 * {@link Instruction} ed esegue contestualmente le operazioni associate
	 * (con l'eventuale uso di parametri contenuti nella particolare istanza di
	 * Instruction) per inviare il commando tramite il modello AutoQuadEsc32
	 *
	 * @param instruction
	 *            istanza di Instruction contenente tipo di istruzione e
	 *            parametri associati da eseguire
	 *
	 * @see esc.AbstractEsc#executeInstruction(routine.Instruction)
	 */
	@Override
	protected void privilegedExecuteInstruction(Instruction instruction) {
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
			int direction = ((String) instruction.parameters.get("direction"))
					.equals("forward") ? 1 : -1;
			setDirection(direction);
		default:
			instruction.parameters.clear();
		}
	}

	@Deprecated
	@Override
	public void removeTelemetryParameter(TelemetryParameter parameter) {
		telemetryParameters.remove(parameter);
	}

	/**
	 * Invia il comando passato come parametro nel formato utilizzato dal
	 * modello AutoQuadEsc32. In particolare invia tramite porta seriale il
	 * comando serializzato con codifica UTF-8, seguito dai caratteri LineFeed e
	 * CarriageReturn necessari affinchè l'esc riceva l'istruzione
	 *
	 * @param command
	 *            stringa che definisce il comando da inviare
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 sendRawCommand(String command) {
		try {
			// l'ESC usa la codifica UTF-8 e ha bisogno dei caratteri di LF e CR
			output.write(command.trim().getBytes("UTF-8"));
			output.write(new byte[] { 13, 10 }); // LF, CR
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Invia lo specifico comando per seleziona la direzione di rotazione del
	 * motore: "set DIRECTION + direction"
	 *
	 * @param direction
	 *            valore intero che può assumere i valori 1 e -1 (forward e
	 *            backward)
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 setDirection(int direction) {
		return sendRawCommand("set DIRECTION " + direction);
	}

	/**
	 * Invia lo specifico comando per seleziona la direzione di rotazione del
	 * motore: "set DIRECTION + direction"
	 *
	 * @param direction
	 *            valore intero che può assumere i valori 1 e -1 (forward e
	 *            backward)
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 setRPM(int rpm) {
		return sendRawCommand("rpm " + rpm);
	}

	@Override
	public void setTelemetryParameters(List<TelemetryParameter> parameters) {
		telemetryParameters.clear();
		telemetryParameters.addAll(parameters);
	}

	/**
	 * Invia l'istruzione di avvio del motore: "start"
	 *
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 start() {
		return sendRawCommand("start");
	}

	/**
	 * Avvia la telemetria del motore in base alla frequenza passata come
	 * parametro: "telemetry " + frequency
	 *
	 * @param frequency
	 *            wanted frequency in Hz
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 startTelemetry(int frequency) {
		if (frequency < 0 || frequency > 30)
			throw new IllegalArgumentException(
					"frequenza non valida per la telemetry " + frequency);
		if (frequency == 0)
			return stopTelemetry();

		sendRawCommand("telemetry " + frequency);
		if (reader != null)
			reader.shouldRead.set(false);

		reader = new ReaderThread(frequency);
		reader.start();
		return this;
	}

	/**
	 * Invia l'istruzione per arrestare il motore: "stop"
	 *
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 stop() {
		return sendRawCommand("stop");
	}

	/**
	 * Stoppa la telemetria del motore settando la frequenza di aggiornamento a
	 * 0: "telemetry 0"
	 *
	 * @return istanza di AutoQuadEsc32 stessa per poter effettuare eventuale
	 *         chaining di comandi in successione
	 */
	private AutoQuadEsc32 stopTelemetry() {
		sendRawCommand("telemetry 0");
		if (reader != null)
			reader.shouldRead.set(false);
		return this;
	}

	@Override
	public String toString() {
		return "AutoQuadEsc32";
	}
}
