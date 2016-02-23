package controller;

import analyzer.Analyzer;
import esc.AbstractEsc;
import routine.Routine;
import view.AnalyzerFrame;
import view.GraphTelemetryView;
import view.MainFrame;

public class Controller {

	private AbstractEsc esc;

	public void setEsc(AbstractEsc esc) {
		this.esc = esc;
	}

	public void startRoutine(Routine routine) {
		routine.setEsc(esc);

		new Thread(routine).start();
		new GraphTelemetryView(routine);
		// Si poteva usare anche questa volendo
		// new SimpleTelemetryView(routine);
	}

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
	 * Disconnette l'esc permettendo l'esecuzione di altre routines, si veda:
	 * {@link AbstractEsc#stopAndDisconnect()}
	 * 
	 */
	public void disconnectEsc() {
		if (esc != null)
			esc.stopAndDisconnect();
		esc = null;
	}

	public static void main(String[] args) {
		MainFrame mainView = new MainFrame(new Controller());
		mainView.initGraphic();
	}
}
