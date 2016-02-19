package controller;

import java.io.InputStream;
import java.io.PipedInputStream;

import esc.AbstractEsc;
import routine.Routine;
import view.EscModelAndPortFrame;
import view.GraphTelemetryView;
import view.MainFrame;
import view.SimpleTelemetryView;
//import view.SimpleTelemetryView;

public class Controller {

	private AbstractEsc esc;
	private PipedInputStream in;

	public Controller() {
		EscModelAndPortFrame esg = new EscModelAndPortFrame();
		esc = esg.getConnectedEsc();
		MainFrame mainView = new MainFrame(this);
		mainView.initGraphic();
	}

	public void startRoutine(Routine routine) {
		routine.setEsc(esc);
		
		new Thread(routine).start();
		new GraphTelemetryView(routine);
		// Si poteva usare anche questa volendo
		// new SimpleTelemetryView(routine);
	}

	public void disconnectEsc() {
		esc.stopAndDisconnect();;
	}

	public InputStream getInput() {
		return in;
	}
}
