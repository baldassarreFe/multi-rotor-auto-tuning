package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import esc.TelemetryParameter;

/**
 * 1. caricare da un file csv i dati di timestamp, motor volts, amps avg, rpm,
 * scartare le altre colonne<br>
 * 2. trovare gli N subset di valori per i quali si ha la derivata di rpm
 * positiva (accelerazione)<br>
 * 3. per ogni subset fare il calcolo dei coefficienti:<br>
 * 3a. calcolare accelerazione angolare con regressione lineare sugli rpm<br>
 * 3b. calcolare la coppia applicata dal motore come acc. per inerzia<br>
 * 3c. calcolare la corrente media con media sui valori della corrente<br>
 * 3d. calcolare Kq come rapporto tra coppia e corrente media<br>
 * 3e. calcolare Ke e Ra come coefficienti (risp slope e intercept/corrente
 * media) della regressione lineare tra tensione e velocità angolare<br>
 */
public class TirocinioAnalyzer {

	Map<String, ArrayList<Double>> table;
	double[] Kq = new double[2];
	double[] Ke = new double[2];
	double[] Ra = new double[2];
	double[] Kqs;
	double[] Kes;
	double[] Ras;
	double[] deltaKqs;
	double[] deltaKes;
	double[] deltaRas;
	
	public TirocinioAnalyzer(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// mappa parametro - lista di dati
		table = new HashMap<>();
		table.put("TIME", new ArrayList<Double>());
		table.put("RPM", new ArrayList<Double>());
		table.put("AMPS AVG", new ArrayList<Double>());
		table.put("MOTOR VOLTS", new ArrayList<Double>());

		// individuare le colonne utili
		String header = reader.readLine();
		String[] tokens = header.split(",");
		int timeColumn = 0;
		int rpmColumn = -1;
		int ampsColumn = -1;
		int voltsColumn = -1;
		for (int i = 1; i < tokens.length; i++) {
			switch (TelemetryParameter.valoreDi(tokens[i])) {
			case RPM:
				rpmColumn = i;
				break;
			case AMPS_AVG:
				ampsColumn = i;
				break;
			case MOTOR_VOLTS:
				voltsColumn = i;
				break;
			default:
				break;
			}
		}
		if (rpmColumn == -1 || ampsColumn == -1 || voltsColumn == -1) {
			reader.close();
			throw new IOException("File formatting problem");
		}

		// se su una riga c'è anche solo un buco saltiamo la riga, si può
		// migliorare,
		// ma per comodità nel fare la regressione è meglio fare così
		String line;
		while ((line = reader.readLine()) != null) {
			tokens = line.split(",");
			try {
				table.get("TIME").add(Double.parseDouble(tokens[timeColumn]));
				table.get("RPM").add(Double.parseDouble(tokens[rpmColumn]));
				table.get("AMPS AVG").add(Double.parseDouble(tokens[ampsColumn]));
				table.get("MOTOR VOLTS").add(Double.parseDouble(tokens[voltsColumn]));
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException ignore) {
				System.err.println("Errore alla riga: " + line);
				ignore.printStackTrace();
			}
		}
		reader.close();

		ArrayList<double[]> KqsAndDelta = new ArrayList<>();
		ArrayList<double[]> KesAndDelta = new ArrayList<>();
		ArrayList<double[]> RasAndDelta = new ArrayList<>();
		for (Integer[] set : findSubsets()) {
			int first = set[0];
			int last = set[1];
			double[] times = toPrimitiveType(table.get("TIME").subList(first, last + 1));
			double[] rpms = toPrimitiveType(table.get("RPM").subList(first, last + 1));
			double[] currents = toPrimitiveType(table.get("AMPS AVG").subList(first, last + 1));
			double[] volts = toPrimitiveType(table.get("MOTOR VOLTS").subList(first, last + 1));
			KqsAndDelta.add(calculateKq(rpms, currents, I, deltaI, times));
			KesAndDelta.add(calculateKe(volts, rpms));
			RasAndDelta.add(calculateRa(volts, rpms, currents));
		}
		Kqs = new double[KqsAndDelta.size()];
		Kes = new double[KesAndDelta.size()];
		Ras = new double[RasAndDelta.size()];
		deltaKqs = new double[KqsAndDelta.size()];
		deltaKes = new double[KesAndDelta.size()];
		deltaRas = new double[RasAndDelta.size()];

		for (int i = 0; i < KqsAndDelta.size(); i++) {
			Kqs[i] = KqsAndDelta.get(i)[0];
			Kes[i] = KesAndDelta.get(i)[0];
			Ras[i] = RasAndDelta.get(i)[0];
			deltaKqs[i] = KqsAndDelta.get(i)[1];
			deltaKes[i] = KesAndDelta.get(i)[1];
			deltaRas[i] = RasAndDelta.get(i)[1];
		}
		
		Mean mean = new Mean();
		Kq[0] = mean.evaluate(Kqs);
		Ke[0] = mean.evaluate(Kes);
		Ra[0] = mean.evaluate(Ras);
		Kq[1] = mean.evaluate(deltaKqs);
		Ke[1] = mean.evaluate(deltaKes);
		Ra[1] = mean.evaluate(deltaRas);

		// TODO: capire come gestire gli errori
	}

	private ArrayList<Integer[]> findSubsets() {
		ArrayList<Integer[]> result = new ArrayList<>();
		int initial = 0;
		for (int i = 2; i < table.get("TIME").size() - 2; i++) {
			double oldDerivative = table.get("RPM").get(i - 1) - table.get("RPM").get(i - 2);
			double nextDerivative = table.get("RPM").get(i + 1) - table.get("RPM").get(i);
			double derivative = table.get("RPM").get(i) - table.get("RPM").get(i - 1);
			// se questa derivata e quella successiva sono negative ho due
			// condizioni: o punto di massimo (e quindi devo aggiungere il
			// subset) oppure sono in discesa
			if (derivative <= 0 && nextDerivative <= 0) {
				// la derivata di prima era positiva (quindi sono in un punto di
				// massimo) e il set ha dimensione
				// significativa per fare delle statistiche
				if (oldDerivative > 0 && i - initial > 50)
					result.add(new Integer[] { initial, i - 1 });
				initial = i;
			}
		}
		return result;
	}

	private static double[][] toMatrix(double[] x, double[] y) {
		double[][] result = new double[x.length][2]; // must be the same size
		for (int i = 0; i < x.length; i++) {
			result[i][0] = x[i];
			result[i][1] = y[i];
		}
		return result;
	}

	public static double[] calculateKq(double[] omegas, double[] currents, double I, double deltaI, double[] times) {
		// calcolare accelerazione angolare con regressione lineare sugli rpm
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(times, omegas));
		double alfa = regression.getSlope();
		double delta_alfa = regression.getSlopeStdErr();

		// calcolare la coppia applicata dal motore come acc. per inerzia
		double torque = I * alfa;
		double delta_torque = torque * (delta_alfa / alfa + deltaI / I);

		// calcolare la corrente media con media sui valori della corrente
		Mean mean = new Mean();
		double current_mean = mean.evaluate(currents);
		StandardDeviation sd = new StandardDeviation();
		double delta_current_mean = sd.evaluate(currents, current_mean);

		// calcolare Kq come rapporto tra coppia e corrente media
		return new double[] { torque / current_mean,
				torque / current_mean * (delta_torque / torque + delta_current_mean / current_mean) };
	}

	public static double[] calculateKe(double[] tensions, double[] omegas) {
		// calcolare Ke come slope della regressione lineare tra tensione e
		// velocità angolare
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(omegas, tensions));
		return new double[] { regression.getSlope(), regression.getSlopeStdErr() };
	}

	public static double[] calculateRa(double[] tensions, double[] omegas, double[] currents) {
		// calcolare Ra come intercept/mean_current della regressione lineare
		// tra tensione e velocità angolare
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(omegas, tensions));
		double intercept = regression.getIntercept();
		double delta_intercept = regression.getInterceptStdErr();

		// calcolare la corrente media con media sui valori della corrente
		Mean mean = new Mean();
		double current_mean = mean.evaluate(currents);
		StandardDeviation sd = new StandardDeviation();
		double delta_current_mean = sd.evaluate(currents, current_mean);

		return new double[] { intercept / current_mean,
				(intercept / current_mean) * (delta_current_mean / current_mean + delta_intercept / intercept) };
	}

	public double getKq() {
		return Kq[0];
	}

	public double getKe() {
		return Ke[0];
	}

	public double getRa() {
		return Ra[0];
	}

	public double getKqError() {
		return Kq[1];
	}

	public double getKeError() {
		return Ke[1];
	}

	public double getRaError() {
		return Ra[1];
	}

	public double[] getKqs() {
		return Kqs;
	}

	public double[] getKes() {
		return Kes;
	}

	public double[] getRas() {
		return Ras;
	}

	public double[] getDeltaKqs() {
		return deltaKqs;
	}

	public double[] getDeltaKes() {
		return deltaKes;
	}

	public double[] getDeltaRas() {
		return deltaRas;
	}

	private double[] toPrimitiveType(List<Double> list) {
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		TirocinioAnalyzer t = new TirocinioAnalyzer(new File("Log-1455879733091.csv"), 5.148 * 0.00001, 0);
		System.out.println(t.getKq() + " ± " + t.getKqError());
		System.out.println(t.getKe() + " ± " + t.getKeError());
		System.out.println(t.getRa() + " ± " + t.getRaError());
	}
}
