package view;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JTextField;

import routine.Routine;
import esc.AutoQuadEsc32;
import esc.TelemetryParameter;

/**
 * The first prototype of a view able to display the changing values of a
 * running telemetry. Simply creates as much text boxes as the parameters to
 * display and constantly updates them when a newer value is available
 * 
 */
public class SimpleTelemetryView extends JFrame {
	/**
	 * This thread is made to receive data in bundles ({@link Map} of
	 * <TelelemetryParameter,Object>) from the ReaderThread in
	 * {@link AutoQuadEsc32}. Every value in the map is used to update its
	 * corresponding text field Also, the thread writes in a properly formatted
	 * way the file ".csv" associated with the current run of the rotor.
	 *
	 */
	private class Updater extends Thread {

		private ObjectInputStream dis;

		public Updater(InputStream is) {

			try {
				dis = new ObjectInputStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			try {
				Map<TelemetryParameter, Object> bundle;
				Double timestamp;
				while ((timestamp = dis.readDouble()) != null
						&& (bundle = (Map<TelemetryParameter, Object>) dis
								.readObject()) != null) {
					// letto un bundle, aggiungo ogni valore non null al grafico
					// corrispondente
					for (Entry<TelemetryParameter, Object> entry : bundle
							.entrySet())
						if (entry.getValue() != null) {
							binding.get(entry.getKey()).setText(
									entry.getValue().toString());
							binding.get(entry.getKey()).repaint();
						}

					// scrittura ordinata anche nel file
					StringBuilder sb = new StringBuilder(timestamp.toString()
							+ ",");
					for (int i = 0; i < parameters.size(); i++) {
						TelemetryParameter p = parameters.get(i);
						Object value = bundle.get(p);
						sb.append((value == null ? "" : value)
								+ (i == parameters.size() - 1 ? "\n" : ","));
					}
					fileWriter.print(sb.toString());
				}
			} catch (IOException e) {
				// succede quando la telemetry viene fermata, non è una cosa
				// negativa
			} catch (ClassNotFoundException e) {
				// non succederà mai perchè siamo in locale
				e.printStackTrace();
			} finally {
				fileWriter.flush();
				fileWriter.close();
			}
		}
	}

	private static final long serialVersionUID = 1L;
	private List<TelemetryParameter> parameters;
	private Map<TelemetryParameter, JTextField> binding;
	private PipedInputStream pis;

	private PrintWriter fileWriter;

	public SimpleTelemetryView(Routine routine) {
		parameters = routine.getParameters();
		binding = new HashMap<>(parameters.size());
		try {
			pis = new PipedInputStream(routine.getOutput());
			synchronized (routine.getOutput()) {
				routine.getOutput().notify();
			}
		} catch (IOException e) {
			// speriamo che vada bene
			e.printStackTrace();
		}
		try {
			fileWriter = new PrintWriter("Log-" + new Date().getTime() + ".csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fileWriter.print("TIME,");
		for (int i = 0; i < parameters.size(); i++) {
			TelemetryParameter p = parameters.get(i);
			fileWriter
					.print(p.name + (i == parameters.size() - 1 ? "\n" : ","));
		}
		initGraphics();
		new Updater(pis).start();
	}

	private void initGraphics() {
		setLayout(new GridLayout(parameters.size(), 2));
		for (TelemetryParameter p : parameters) {
			this.add(new JTextField(p.name));
			JTextField field = new JTextField();
			binding.put(p, field);
			this.add(field);
		}
		setSize(640, 480);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					pis.close();
				} catch (IOException ignore) {
					ignore.printStackTrace();
				}
				dispose();
			}
		});
		setVisible(true);

	}
}
