package view;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

/**
 * This class implements a simple JFrame which display the data extracted from
 * the data file in a previous run in a series of ctarts as done in
 * {@link GraphTelemetryView}. In this particular class we have broken the
 * model-view-controller structure and everything is done into the same class:
 * the class autonomosly parse data from file and displays them in the graphs.
 */
public class GraphsFrame extends JFrame {
	/**
	 * Thread that updates the graphs on his {@link GraphsFrame} with the values
	 * parsed from the file given to the GraphFrame
	 */
	private class Updater extends Thread {

		@Override
		public void run() {
			try {
				// lettura e aggiunta dei dati
				String line;
				while ((line = reader.readLine()) != null) {
					String[] values = line.split(",");
					for (int i = 1; i < values.length; i++)
						try {
							Double timestamp = Double.valueOf(values[0]);
							Double value = Double.valueOf(values[i]);
							dataSeries.get(parameters[i]).add(timestamp, value);
						} catch (ArrayIndexOutOfBoundsException
								| NumberFormatException ignore) {
							System.err.println("Errore alla riga: " + line);
							ignore.printStackTrace();
						}
				}
			} catch (IOException | NullPointerException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException ignore) {
					ignore.printStackTrace();
				}
			}
		}
	}

	private static final long serialVersionUID = 1801638182066987430L;
	private String[] parameters;
	private Map<String, XYSeries> dataSeries;

	private BufferedReader reader;

	/**
	 * Constructor of a {@link GraphsFrame}, receives the file containing the
	 * data to display
	 * 
	 * @param dataFile
	 * @throws IOException
	 */
	public GraphsFrame(File dataFile) throws IOException {
		reader = new BufferedReader(new FileReader(dataFile));
		loadHeader();
		initGraphics();
		new Updater().start();
	}

	/**
	 * Creazione dei grafici sulla base dei parametri contenuti in
	 * {@link #parameters}
	 */
	private void initGraphics() {
		setTitle("Data viewer");
		setLayout(new GridLayout(2, (int) Math.ceil(parameters.length / 2.0)));
		for (XYSeries xys : dataSeries.values()) {
			JFreeChart chart = ChartFactory.createXYLineChart(xys
					.getDescription(), null, null, new XYSeriesCollection(xys),
					PlotOrientation.VERTICAL, true, true, false);
			XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setBaseShapesVisible(true);
				renderer.setBaseShapesFilled(true);
			}
			ChartPanel panel = new ChartPanel(chart);
			this.add(panel);
		}
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Parsing dell'header e creazione delle dataSeries
	 * 
	 * @throws IOException
	 */
	private void loadHeader() throws IOException {
		String header = reader.readLine();
		parameters = header.split(",");
		dataSeries = new HashMap<String, XYSeries>();
		for (int i = 1; i < parameters.length; i++)
			if (Number.class.isAssignableFrom(TelemetryParameter
					.parse(parameters[i]).valueClass))
				dataSeries.put(parameters[i], new XYSeries(parameters[i]));
	}
}
