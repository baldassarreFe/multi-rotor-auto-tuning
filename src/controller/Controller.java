package controller;

import java.lang.reflect.InvocationTargetException;

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
		mainView.initGraphic();
	}
	
	public void startRoutine(Routine routine) {
		routine.setEsc(esc);
		new Thread(routine).start();
	}
	
	public void exitEsc(){
		esc.disconnect();
	}
}
