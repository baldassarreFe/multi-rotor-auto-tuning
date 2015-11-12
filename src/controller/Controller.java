package controller;

import esc.AbstractEsc;
import esc.Routine;
import view.EscSelectorGui;
import view.MainView;

public class Controller {
	
	private AbstractEsc esc;
	
	public Controller() {
		EscSelectorGui esg = new EscSelectorGui();
		esc = esg.getConnectedEsc();
		MainView mainView = new MainView(this);
	}
	
	public void startRoutine(Routine routine) {
		routine.setEsc(esc);
		routine.start();
	}
}
