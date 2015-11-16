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

	public void startRoutine(Class routine) {
		if (Routine.class.isAssignableFrom(routine)) {
			try {
				Routine r = (Routine) routine.newInstance();
				r.setEsc(esc);
				this.in = new PipedInputStream(r.getOutput());
				r.start();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void exitEsc() {
		esc.disconnect();
	}

	public InputStream getInput() {
		return in;
	}
}
