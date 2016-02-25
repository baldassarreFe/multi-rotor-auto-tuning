package controller;

import analyzer.Analyzer;
import esc.AbstractEsc;
import routine.Routine;
import view.AnalyzerFrame;
import view.GraphTelemetryView;
import view.MainFrame;

public class Controller {

	public static void main(String[] args) {
		new MainFrame(new Controller());
	}

	private AbstractEsc esc;
	private Routine routine;

	/**
	 * Avvia un'analisi di dati basata sull' {@link Analyzer} passato come
	 * parametro, gestendo anche la creazione di un {@link AnalyzerFrame} per
	 * visualizzare i risultati
	 *
	 * @param analyzer
	 */
	public void startAnalysis(Analyzer analyzer) {
		new AnalyzerFrame(analyzer);
	}

	/**
	 * Avvia una determinata routine su un determinato esc, gestendo la
	 * creazione della finestra grafica sulla quale mostrate l'output
	 * 
	 * @param routine
	 * @param esc
	 */
	public void startRoutine(Routine routine, AbstractEsc esc) {
		this.routine = routine;
		this.esc = esc;
		this.routine.setEsc(esc);
		new Thread(routine).start();
		new GraphTelemetryView(routine);
		// Si poteva usare anche questa volendo
		// new SimpleTelemetryView(routine);
	}

	/**
	 * Ferma la routine e disconnette l'esc permettendo l'esecuzione di altre routines, si vedano
	 * {@link AbstractEsc#stopAndDisconnect()} e {@link Routine#stopImmediately()}
	 *
	 */
	public void stopRoutineAndDisconnectEsc() {
		if (routine != null) {
			routine.stopImmediately();
			routine = null;
		}
		if (esc != null) {
			esc.stopAndDisconnect();
			esc = null;
		}
	}
}
