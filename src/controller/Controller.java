package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;

import esc.AbstractEsc;
import routine.Routine;
import view.EscSelectorGui;
import view.MainView;

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
		try {
			this.in = new PipedInputStream(routine.getOutput());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(routine).start();
	}

	public void exitEsc() {
		esc.disconnect();
	}

	public InputStream getInput() {
		return in;
	}
}
