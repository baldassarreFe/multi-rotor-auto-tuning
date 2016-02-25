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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import esc.AutoQuadEsc32;
import esc.TelemetryParameter;
import routine.Routine;

/**
 * This class represents a frame in which the data collected during the run of
 * the motor are displayed in the most proper way. Every data which comes in
 * numeric form is dynamically put into a graph in function of time passing,
 * while any parameter that comes in text is put into a text box constantly
 * updated. This class is also trusted with the task of writing the data coming
 * from the rotor into the specific .csv file.
 *
 */
public class GraphTelemetryView extends JFrame {
	/**
	 * This thread is made to receives data in bundles (Map of
	 * <TelelemetryParameter,Object>) from the ReaderThread in
	 * {@link AutoQuadEsc32}. Once received this map, the thread controls if the
	 * object is an instance of a String or a Number and subsequently put it in
	 * the {@link XYSeries} (graph) or the {@link JTextField} found as values
	 * associated to the specific {@link TelemetryParameter} of the object
	 * received. Also, the thread writes in a properly formatted way the file
	 * ".csv" associated with the current run of the rotor.
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
		@SuppressWarnings("unchecked") // da un lato invio una map di
										// <TelemeryParameter, Object>, da qua
										// la leggo senza problemi
		public void run() {
			try {
				Map<TelemetryParameter, Object> bundle;
				Double timestamp;
				while ((timestamp = dis.readDouble()) != null
						&& (bundle = (Map<TelemetryParameter, Object>) dis.readObject()) != null) {
					// letto un bundle, aggiungo ogni valore non null al grafico
					// corrispondente
					for (Entry<TelemetryParameter, Object> entry : bundle.entrySet())
						if (entry.getValue() != null)
							if (Number.class.isAssignableFrom(entry.getValue().getClass()))
								dataSeries.get(entry.getKey()).add(timestamp, (Number) entry.getValue());
							else
								dataBox.get(entry.getKey()).setText((String) entry.getValue());

					// scrittura ordinata anche nel file
					StringBuilder sb = new StringBuilder(timestamp.toString() + ",");
					for (int i = 0; i < parameters.size(); i++) {
						TelemetryParameter p = parameters.get(i);
						Object value = bundle.get(p);
						sb.append((value == null ? "" : value) + (i == parameters.size() - 1 ? "\n" : ","));
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
	List<TelemetryParameter> parameters;
	private Map<TelemetryParameter, XYSeries> dataSeries;
	private Map<TelemetryParameter, JTextField> dataBox;
	private PipedInputStream pis;

	private PrintWriter fileWriter;

	/**
	 * @param routine
	 */
	public GraphTelemetryView(Routine routine) {
		try {
			pis = new PipedInputStream(routine.getOutput());
			synchronized (routine.getOutput()) {
				routine.getOutput().notify();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		parameters = routine.getParameters();

		dataSeries = new HashMap<>();
		dataBox = new HashMap<>();

		try {
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			fileWriter = new PrintWriter("Motor_data_" + df.format(date) + ".csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		fileWriter.print("TIME,");
		for (int i = 0; i < parameters.size(); i++) {
			TelemetryParameter p = parameters.get(i);
			if (Number.class.isAssignableFrom(p.valueClass))
				dataSeries.put(p, new XYSeries(p.name));
			else if (String.class.isAssignableFrom(p.valueClass)) {
				JTextField textField = new JTextField();
				textField.setEditable(false);
				dataBox.put(p, textField);
			}
			fileWriter.print(p.name + (i == parameters.size() - 1 ? "\n" : ","));
		}
		initGraphics();
		new Updater(pis).start();
	}

	private void initGraphics() {
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(2, (int) Math.ceil(dataSeries.keySet().size() + 1 / 2.0)));

		for (XYSeries xys : dataSeries.values()) {
			JFreeChart chart = ChartFactory.createXYLineChart(xys.getDescription(), null, null,
					new XYSeriesCollection(xys), PlotOrientation.VERTICAL, true, true, false);
			XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setBaseShapesVisible(true);
				renderer.setBaseShapesFilled(true);
			}
			ChartPanel panel = new ChartPanel(chart);
			// panel.setSize(200, 100);
			graphPanel.add(panel);
		}

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(dataBox.keySet().size(), 2));
		for (Entry<TelemetryParameter, JTextField> e : dataBox.entrySet()) {
			JLabel label = new JLabel(e.getKey().name);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
			e.getValue().setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
			textPanel.add(label);
			textPanel.add(e.getValue());
		}
		graphPanel.add(textPanel);

		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.add(graphPanel);
		pack();

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					pis.close();
				} catch (IOException ignore) {
					// Se ci sono errori nella chiusura (non ce ne saranno mai)
					// far partire un'altra routine causerà problemi nelle
					// finestre grafiche
					// che si vogliono connetere al piped stream del esc
					ignore.printStackTrace();
				}
				dispose();
			}
		});
		setVisible(true);
	}
}
