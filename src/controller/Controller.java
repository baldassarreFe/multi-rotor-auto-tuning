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

	public void startAnalysis(Analyzer analyzer) {
		new AnalyzerFrame(analyzer);
	}

	public void disconnectEsc() {
		if (esc != null)
			esc.stopAndDisconnect();
	}

	public static void main(String[] args) {
		MainFrame mainView = new MainFrame(new Controller());
		mainView.initGraphic();
	}
}
