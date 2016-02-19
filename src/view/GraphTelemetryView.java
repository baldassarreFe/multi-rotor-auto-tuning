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

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import esc.TelemetryParameter;
import routine.Routine;

public class GraphTelemetryView extends JFrame {
	private static final long serialVersionUID = 1L;
	private List<TelemetryParameter> parameters;
	private Map<TelemetryParameter, XYSeries> dataSeries;
	private PipedInputStream pis;
	private PrintWriter fileWriter;

	public GraphTelemetryView(Routine routine) {
		try {
			this.pis = new PipedInputStream(routine.getOutput());
			synchronized (routine.getOutput()) {
				routine.getOutput().notify();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.parameters = routine.getParameters();
		this.dataSeries = new HashMap<>(parameters.size());
		try {
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-DD_HH-mm-ss");
			fileWriter = new PrintWriter("Motor_data_" + df.format(date) + ".csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fileWriter.print("TIME,");
		for (int i = 0; i < parameters.size(); i++) {
			TelemetryParameter p = parameters.get(i);
			dataSeries.put(p, new XYSeries(p.name));
			fileWriter.print(p.name + (i == parameters.size() - 1 ? "\n" : ","));
		}
		initGraphics();
		new Updater(pis).start();
	}

	private void initGraphics() {
		this.setLayout(new GridLayout(2, (int) Math.ceil(parameters.size() / 2.0)));
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
			panel.setSize(200, 100);
			this.add(panel);
		}

		pack();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
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

	private class Updater extends Thread {

		private ObjectInputStream dis;

		public Updater(InputStream is) {
			try {
				this.dis = new ObjectInputStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
					for (Entry<TelemetryParameter, Object> entry : bundle.entrySet()) {
						if (entry.getValue() != null) {
							if (Number.class.isAssignableFrom(entry.getValue().getClass())) {
								dataSeries.get(entry.getKey()).add(timestamp, (Number) entry.getValue());
							} else {
								// TODO: Mettere delle text box per i valori di
								// telemetria non numerici
							}
						}
					}

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
}
