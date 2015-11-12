package controller;

import view.EscSelectorGui;

public class Controller {
	
	private AbstractEsc aesc;
	
	public Controller() {
		EscSelectorGui esg = new EscSelectorGui();
		aesc= esg.getConnectedEsc();
		MainView mainView = new MainView(this);
	}
	
	public void startRoutine(Routine routine) {
		routine.setEsc(aesc);
		routine.start();
	}
}
