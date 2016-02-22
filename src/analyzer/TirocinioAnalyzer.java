package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
public class TirocinioAnalyzer extends Analyzer {

	Map<String, ArrayList<Double>> table;
	PrintWriter pw;

	public TirocinioAnalyzer(File file, File propertyFile) throws IOException {
		super(file, propertyFile);

		pw = new PrintWriter(new File(file.getName().substring(0, file.getName().length() - 4) + "-ANALYSIS.csv"));

		parametersRequired.put("I", null);
		parametersRequired.put("ΔI", null);
		parametersRequired.put("SubsetSize", null);

		results.put("Kq", null);
		results.put("ΔKq", null);
		results.put("Ke", null);
		results.put("ΔKe", null);
		results.put("Ra", null);
		results.put("ΔRa", null);

		loadParameters();
		readDataFromFile();
	}

	public void calcola() {
		pw.write("Kq,ΔKq,Ke,ΔKe,Ra,ΔRa,SamplesCount\n");
		ArrayList<double[]> KqsAndΔ = new ArrayList<>();
		ArrayList<double[]> KesAndΔ = new ArrayList<>();
		ArrayList<double[]> RasAndΔ = new ArrayList<>();

		// rpm to rad/s
		ArrayList<Double> temp = table.get("RPM");

		for (int i = 0; i < temp.size(); i++)
			temp.set(i, (temp.get(i) * 2 * Math.PI / 60));

		for (Integer[] set : findSubsets()) {
			int first = set[0];
			int last = set[1];
			double[] times = toPrimitiveType(table.get("TIME").subList(first, last + 1));
			double[] rpms = toPrimitiveType(table.get("RPM").subList(first, last + 1));
			double[] currents = toPrimitiveType(table.get("AMPS AVG").subList(first, last + 1));
			double[] volts = toPrimitiveType(table.get("MOTOR VOLTS").subList(first, last + 1));

			double[] tempKq = calculateKq(rpms, currents, parametersRequired.get("I"), parametersRequired.get("ΔI"),
					times);
			double[] tempKe = calculateKe(volts, rpms);
			double[] tempRa = calculateRa(volts, rpms, currents);

			KqsAndΔ.add(tempKq);
			KesAndΔ.add(tempKe);
			RasAndΔ.add(tempRa);

			StringBuilder sb = new StringBuilder();
			sb.append(tempKq[0] + "," + tempKq[1] + "," + tempKe[0] + "," + tempKe[1] + "," + tempRa[0] + ","
					+ tempRa[1] + "," + (set[1] - set[0] + 1) + "\n");
			pw.write(sb.toString());
		}
		pw.close();

		double[] Kqs = new double[KqsAndΔ.size()];
		double[] Kes = new double[KesAndΔ.size()];
		double[] Ras = new double[RasAndΔ.size()];
		double[] ΔKqs = new double[KqsAndΔ.size()];
		double[] ΔKes = new double[KesAndΔ.size()];
		double[] ΔRas = new double[RasAndΔ.size()];

		for (int i = 0; i < KqsAndΔ.size(); i++) {
			Kqs[i] = KqsAndΔ.get(i)[0];
			Kes[i] = KesAndΔ.get(i)[0];
			Ras[i] = RasAndΔ.get(i)[0];
			ΔKqs[i] = KqsAndΔ.get(i)[1];
			ΔKes[i] = KesAndΔ.get(i)[1];
			ΔRas[i] = RasAndΔ.get(i)[1];
		}

		Mean mean = new Mean();
		results.put("Kq", mean.evaluate(Kqs));
		results.put("Ke", mean.evaluate(Kes));
		results.put("Ra", mean.evaluate(Ras));
		results.put("ΔKq", mean.evaluate(ΔKqs));
		results.put("ΔKe", mean.evaluate(ΔKes));
		results.put("ΔRa", mean.evaluate(ΔRas));

	}

	private void readDataFromFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(logFile));

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
	}

	private ArrayList<Integer[]> findSubsets() {
		ArrayList<Integer[]> result = new ArrayList<>();
		int first = 0;
		int last = 0;
		int samplesToConsider = (int) (parametersRequired.get("SubsetSize") / 3);

		for (int i = samplesToConsider / 2; i < table.get("TIME").size() - samplesToConsider / 2
				- samplesToConsider % 2; i++) {

			double avgNextDerivatives = 0;
			for (int j = 1; j <= samplesToConsider / 2; j++) {
				avgNextDerivatives += table.get("RPM").get(i + j) - table.get("RPM").get(i);
				avgNextDerivatives += table.get("RPM").get(i) - table.get("RPM").get(i - j);
			}
			avgNextDerivatives /= samplesToConsider;

			if (avgNextDerivatives > 0)
				last++;
			else {
				if (last - first > parametersRequired.get("SubsetSize")) {
					result.add(new Integer[] { first, last });
					System.out.println(first + " -> " + last + " [" + (last - first + 1) + "]");
				}
				first = i;
				last = i;
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

	public static double[] calculateKq(double[] omegas, double[] currents, double I, double ΔI, double[] times) {
		// calcolare accelerazione angolare con regressione lineare sugli rpm
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(times, omegas));
		double alfa = regression.getSlope();
		double Δ_alfa = regression.getSlopeStdErr();

		// calcolare la coppia applicata dal motore come acc. per inerzia
		double torque = I * alfa;
		double Δ_torque = torque * (Δ_alfa / alfa + ΔI / I);

		// calcolare la corrente media con media sui valori della corrente
		Mean mean = new Mean();
		double current_mean = mean.evaluate(currents);
		StandardDeviation sd = new StandardDeviation();
		double Δ_current_mean = sd.evaluate(currents, current_mean);

		// calcolare Kq come rapporto tra coppia e corrente media
		return new double[] { torque / current_mean,
				torque / current_mean * (Δ_torque / torque + Δ_current_mean / current_mean) };
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
		double Δ_intercept = regression.getInterceptStdErr();

		// calcolare la corrente media con media sui valori della corrente
		Mean mean = new Mean();
		double current_mean = mean.evaluate(currents);
		StandardDeviation sd = new StandardDeviation();
		double Δ_current_mean = sd.evaluate(currents, current_mean);

		return new double[] { intercept / current_mean,
				(intercept / current_mean) * (Δ_current_mean / current_mean + Δ_intercept / intercept) };
	}

	private double[] toPrimitiveType(List<Double> list) {
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		new TirocinioAnalyzer(new File("Motor_data_2016-02-50_15-16-26.csv"), new File("tirocigno.properties"))
				.calcola();
	}
}
