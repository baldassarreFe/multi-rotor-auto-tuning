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
	
	public void startRoutine(Class routine) {
		if (Routine.class.isAssignableFrom(routine)) {
			try {
				Routine r = (Routine) routine.newInstance();
				r.setEsc(esc);
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
			}
		}
	}
	
	public void exitEsc(){
		esc.disconnect();
	}
}
