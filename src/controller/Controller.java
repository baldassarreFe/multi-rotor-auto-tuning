package controller;

import java.io.InputStream;
import java.io.PipedInputStream;

import analyzer.Analyzer;
import esc.AbstractEsc;
import routine.Routine;
import view.LeftPanel;
import view.AnalyzerFrame;
import view.GraphTelemetryView;
import view.MainFrame;
import view.SimpleTelemetryView;
//import view.SimpleTelemetryView;

public class Controller {

	private AbstractEsc esc;
	private PipedInputStream in;

	public void startRoutine(Routine routine) {
		routine.setEsc(esc);
		
		new Thread(routine).start();
		new GraphTelemetryView(routine);
		// Si poteva usare anche questa volendo
		// new SimpleTelemetryView(routine);
	}
	
	public void startAnalysis(Analyzer analyzer){
		new AnalyzerFrame(analyzer);
	}

	public void disconnectEsc() {
		esc.stopAndDisconnect();;
	}

	public InputStream getInput() {
		return in;
	}
	
	public static void main(String[] args) {
		MainFrame mainView = new MainFrame(new Controller());
		mainView.initGraphic();
	}
}
