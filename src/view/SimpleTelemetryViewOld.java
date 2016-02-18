package view;

import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTextField;

import esc.TelemetryParameter;
import routine.Routine;

public class SimpleTelemetryViewOld extends JFrame {
	private static final long serialVersionUID = 1L;
	private Set<TelemetryParameter> parameters;
	private Map<TelemetryParameter, JTextField> binding;
	private PipedInputStream pis;

	public SimpleTelemetryViewOld(Routine routine) {
		this.parameters = routine.getParameters();
		this.binding = new HashMap<>(parameters.size());
		try {
			this.pis = new PipedInputStream(routine.getOutput());
			synchronized (routine.getOutput()) {
				routine.getOutput().notify();
			}
		} catch (IOException e) {
			// speriamo che vada bene
			e.printStackTrace();
		}
		initGraphics();
		new Updater(pis).start();
	}
	
	private void initGraphics(){
		this.setLayout(new GridLayout(parameters.size(), 2));
		for(TelemetryParameter p : parameters){
			this.add(new JTextField(p.name));
			JTextField field = new JTextField();
			binding.put(p, field);
			this.add(field);
		}
		setSize(640, 480);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		
	}
	
	private class Updater extends Thread {

		private ObjectInputStream dis;
		

		public Updater(InputStream is) {
		
			try {
				this.dis = new ObjectInputStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				TelemetryParameter p;
				while ((p = (TelemetryParameter) dis.readObject()) != null) {
					Object value = null;
//					if (p.c == String.class){
//						value = (String) dis.readObject();
//					} else if (p.c == Integer.class) {
//						value = dis.readInt();
//					} else if (p.c == Double.class) {
//						value = dis.readDouble();
//					}
					value = dis.readObject();
					binding.get(p).setText(value.toString());
					binding.get(p).repaint();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
