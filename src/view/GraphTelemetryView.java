package view;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	private Set<TelemetryParameter> parameters;
	private Map<TelemetryParameter, XYSeries> dataSeries;
	private PipedInputStream pis;

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
		for(TelemetryParameter p : parameters) {
			dataSeries.put(p, new XYSeries(p.name));
		}
		initGraphics();
		new Updater(pis).start();
	}
	
	private void initGraphics(){
		this.setLayout(new GridLayout(2, (int) Math.ceil(parameters.size()/2.0)));
		for(XYSeries xys : dataSeries.values()) {
			JFreeChart chart = ChartFactory.createXYLineChart(xys.getDescription(), null, null, new XYSeriesCollection(xys),
					PlotOrientation.VERTICAL, true, true, false);
			XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setBaseShapesVisible(true);
				renderer.setBaseShapesFilled(true);
			}
			ChartPanel panel = new ChartPanel(chart);
			panel.setSize(300,150);
			add(panel);
		}
		pack();
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
				Map<TelemetryParameter, Long> counters = new HashMap<TelemetryParameter, Long>(parameters.size());
				for(TelemetryParameter p : parameters) {
					counters.put(p, 0L);
				}
				TelemetryParameter p;
				while ((p = (TelemetryParameter) dis.readObject()) != null) {
					Object value = dis.readObject();
					counters.put(p, counters.get(p) + 1);
					dataSeries.get(p).add(counters.get(p), (Number) value);  // non passare parametri non Number
				}
			} catch (IOException e) {
				// succede quando la telemetry viene fermata, non è una cosa negativa
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
