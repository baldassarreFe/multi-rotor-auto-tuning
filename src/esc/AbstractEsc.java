package esc;

import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import routine.Instruction;
import routine.InstructionType;

/**
 * Rappresentazione astratta di un ESC (electronic speed controller) in grado di
 * connettere, inviare comandi e leggere dati in input da un motore tramite una
 * porta seriale. Prevede l'uso di una lista di parametri che definisce i campi
 * di interesse del'ESC nell'eventuale lettura della telemetria del motore
 *
 */
public abstract class AbstractEsc {
	protected static ArrayList<TelemetryParameter> telemetryParameters = new ArrayList<>();
	protected InputStream input;
	protected OutputStream output;
	protected PipedOutputStream pipedOutput;
	private SerialPort port;
	private AtomicBoolean shouldExecuteInstructions;

	/**
	 * Crea un'istanza di {@link AbstractEsc} a partire da una porta seriale.
	 * Genera contenstualmente una {@link PipedOutputStream} utile per la
	 * gestione dei dati ricevuti dal motore
	 *
	 * @param port
	 *            , istanza di {@link SerialPort} dalla quale vengono estratti
	 *            input ed output stream per la comunicazione con il motore
	 * @throws IOException
	 */
	public AbstractEsc(SerialPort port) throws IOException {
		this.port = port;
		input = port.getInputStream();
		output = port.getOutputStream();
		pipedOutput = new PipedOutputStream();
		shouldExecuteInstructions = new AtomicBoolean(true);
	}

	/**
	 * Aggiunge un parametro alla lista di parametri di interesse dell'ESC.
	 *
	 * @param parameter
	 */
	public abstract void addTelemetryParameter(TelemetryParameter parameter);

	/**
	 * Fa eseguire al motore un'istruzione generica. L'implementazione deve
	 * prevedere la realizzazione specifica e le comunicazioni necessarie
	 * affinchè il particolare tipo di ESC invii i giusti comandi per
	 * l'esecuzione sul particolare motore. Nell'implementazione è necessario
	 * valutare caso per caso tutti i valori che può assumere
	 * {@link Instruction} e l'associato enum {@link InstructionType} ed
	 * effettuare le operazioni necessarie per effettuare l'operazione associata
	 * a quella istruzione
	 *
	 * @param instruction
	 */
	public final void executeInstruction(Instruction instruction) {
		if (shouldExecuteInstructions.get() == true)
			privilegedExecuteInstruction(instruction);
	}

	/**
	 * Restituisce la {@link PipedOutputStream} associata all'ESC sulla quale
	 * avviene la scrittura dei dati ricevuti in input dal motore
	 *
	 * @return {@link PipedOutputStream}
	 */
	public PipedOutputStream getPipedOutput() {
		return pipedOutput;
	}

	protected abstract void privilegedExecuteInstruction(Instruction instruction);

	/**
	 * Rimuove il parametro passato come argomento dalla lista di interesse
	 * dell'ESC.
	 *
	 * @param parameter
	 */
	public abstract void removeTelemetryParameter(TelemetryParameter parameter);

	/**
	 * Setta o sostituisce la lista di parametri di interesse dell'ESC.
	 *
	 * @param params
	 */
	public abstract void setTelemetryParameters(List<TelemetryParameter> params);

	/**
	 * Ferma l'invio di comandi verso il motore per un tempo pari a millis,
	 * invoca il metodo sleep sul processo che invia istruzioni al motore
	 *
	 * @param millis
	 * @return
	 */
	public final AbstractEsc sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Ferma e disconnette l'esc dal motore. Invia le istruzioni di
	 * {@link Instruction#STOP}, {@link Instruction#STOP_TELEMETRY} e
	 * {@link Instruction#DISARM} al motore e chiude la connessione virtuale con
	 * la porta seriale
	 */
	public void stopAndDisconnect() {
		shouldExecuteInstructions.set(false);
		privilegedExecuteInstruction(Instruction.newStop());
		privilegedExecuteInstruction(Instruction.newStopTelemetry());
		privilegedExecuteInstruction(Instruction.newDisarm());
		port.close();
	}
}
