package controller;

import java.io.InputStream;
import java.io.PipedInputStream;

import esc.AbstractEsc;
import routine.Routine;
import view.EscSelectorGui;
import view.GraphTelemetryView;
import view.MainView;
//import view.SimpleTelemetryView;

public class Controller {

	private AbstractEsc esc;
	private PipedInputStream in;

	public Controller() {
		EscSelectorGui esg = new EscSelectorGui();
		esc = esg.getConnectedEsc();
		MainView mainView = new MainView(this);
		mainView.initGraphic();
	}

	public void startRoutine(Routine routine) {
		routine.setEsc(esc);
		
		new Thread(routine).start();
		new GraphTelemetryView(routine);
		//new SimpleTelemetryView(routine);
	}

	public void disconnectEsc() {
		esc.stopAndDisconnect();;
	}

	public InputStream getInput() {
		return in;
	}
}
