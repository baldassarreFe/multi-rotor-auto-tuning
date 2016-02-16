package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

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
import procedure.MathProcedure;
import routine.Routine;

public class GraphTelemetryView extends JFrame {
	private static final long serialVersionUID = 1L;
	private Set<TelemetryParameter> parameters;
	private Map<TelemetryParameter, XYSeries> dataSeries;
	private PipedInputStream pis;
	protected AtomicBoolean terminated;

	public GraphTelemetryView(Routine routine) {
		this.terminated = new AtomicBoolean(false);
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
		for (TelemetryParameter p : parameters) {
			dataSeries.put(p, new XYSeries(p.name));
		}
		initGraphics();
		new Updater(pis).start();
	}

	private void initGraphics() {
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(2, (int) Math.ceil(parameters.size() / 2.0)));
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
			graphPanel.add(panel);
		}
		this.add(graphPanel, BorderLayout.NORTH);

		JPanel parameterPanel = new JPanel();
		JTextField[] valueFields = new JTextField[3];

		parameterPanel.setLayout(new GridLayout(3, 1));
		for (int i = 0; i < 3; i++) {
			JPanel panel = new JPanel();
			String title = null;
			switch (i) {
			case 0:
				title = "Kq";
				break;
			case 1:
				title = "Ke";
				break;
			case 2:
				title = "Ra";
				break;
			}
			panel.add(new JLabel(title));
			panel.add(valueFields[i]);
			parameterPanel.add(panel);
		}
		
		add(parameterPanel,BorderLayout.SOUTH);
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);

		
		// ASPETTO FINCHE' NON TERMINO LE LETTURE
		while (!terminated.get())
			;

		// TODO: non farlo cablato
		double I = 5.148 * Math.pow(10, -5);

		// TODO: RISOLVERE LA QUESTIONE TEMPO --> NON SO SE FUNZIONA
		valueFields[0].setText("" + MathProcedure.calculateKq(dataSeries.get(TelemetryParameter.RPM).toArray()[1],
				dataSeries.get(TelemetryParameter.AMPS_AVG).toArray()[1], I,
				dataSeries.get(TelemetryParameter.RPM).toArray()[0]));
		valueFields[1]
				.setText("" + MathProcedure.calculateKe(dataSeries.get(TelemetryParameter.MOTOR_VOLTS).toArray()[1],
						dataSeries.get(TelemetryParameter.RPM).toArray()[1]));
		valueFields[2]
				.setText("" + MathProcedure.calculateRa(dataSeries.get(TelemetryParameter.MOTOR_VOLTS).toArray()[1],
						dataSeries.get(TelemetryParameter.RPM).toArray()[1],
						dataSeries.get(TelemetryParameter.AMPS_AVG).toArray()[1]));

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
				int telemetry = dis.readInt();
				// PER PRIMA COSA LEGGO LA FREQUENZA
				Map<TelemetryParameter, Long> counters = new HashMap<TelemetryParameter, Long>(parameters.size());
				// perchè abbiamo usato un long?
				for (TelemetryParameter p : parameters) {
					counters.put(p, 0L);
				}
				TelemetryParameter p;
				while ((p = (TelemetryParameter) dis.readObject()) != null) {
					Object value = dis.readObject();
					counters.put(p, counters.get(p) + 1 / telemetry);
					dataSeries.get(p).add(counters.get(p), (Number) value); 
					// non passare parametri non number
				}
			} catch (IOException e) {
				// succede quando la telemetry viene fermata, non è una cosa
				// negativa
				terminated.set(true);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
