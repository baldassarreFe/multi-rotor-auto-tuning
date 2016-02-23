package analyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Questa implementazione di {@link Analyzer} compie i seguenti passi per
 * calcolare Ke, Kq, Ra: 1. trovare gli N subset di valori per i quali si ha la
 * derivata di rpm positiva (accelerazione)<br>
 * 2. per ogni subset fare il calcolo dei coefficienti:<br>
 * 2a. calcolare accelerazione angolare con regressione lineare sugli rpm<br>
 * 2b. calcolare la coppia applicata dal motore come acc. per inerzia<br>
 * 2c. calcolare la corrente media con media sui valori della corrente<br>
 * 2d. calcolare Kq come rapporto tra coppia e corrente media<br>
 * 2e. calcolare Ke e Ra come coefficienti (risp slope e intercept/corrente
 * media) della regressione lineare tra tensione e velocità angolare<br>
 */
public class TirocinioAnalyzer extends Analyzer {
	private PrintWriter pw;

	/**
	 * Dopo aver chiamato il costruttore di
	 * {@link Analyzer#Analyzer(File, File)} vengono riempite le mappe
	 * {@link Analyzer#parametersRequired}, {@link Analyzer#table},
	 * {@link Analyzer#results}.<br>
	 * Come parametri vengono richiesti il momento di inerzia (I) del disco, la
	 * sua incertezza (ΔI) e la dimensione minima che deve avere un subset di
	 * misure di rpm crescenti per essere considerato come un'accelerazione ai
	 * fini dell'analisi.<br>
	 * Come colonne nel file sono richieste TIME, RPM, AMPS AVG e MOTOR VOLTS.
	 * <br>
	 * Come risultati vengono restituiti Ke, Kq e Ra con le relative incertezze,
	 * oltre che al numero di acelerazioni individuate durante l'analisi.<br>
	 * Informazioni più dettagliate sui risultati intermedi dei calcoli possono
	 * essere lette in un nuovo file chiamato come il file dei dati con
	 * l'aggiunta di "-ANALYSIS"
	 * 
	 * @param dataFile
	 * @param propertyFile
	 * @throws IOException
	 * @see {@link Analyzer#Analyzer(File, File)}
	 */
	public TirocinioAnalyzer(File dataFile, File propertyFile) throws IOException {
		super(dataFile, propertyFile);

		pw = new PrintWriter(
				new File(dataFile.getName().substring(0, dataFile.getName().length() - 4) + "-ANALYSIS.csv"));

		parametersRequired.put("I", null);
		parametersRequired.put("ΔI", null);
		parametersRequired.put("SubsetSize", null);

		results.put("Kq", null);
		results.put("ΔKq", null);
		results.put("Ke", null);
		results.put("ΔKe", null);
		results.put("Ra", null);
		results.put("ΔRa", null);
		results.put("Subsets Count", null);

		table.put("TIME", new ArrayList<Double>());
		table.put("RPM", new ArrayList<Double>());
		table.put("AMPS AVG", new ArrayList<Double>());
		table.put("MOTOR VOLTS", new ArrayList<Double>());

		loadParameters();
		readDataFromFile();
	}

	/**
	 * La procedura di calcolo sfrutta gli strumenti di {@link Mean},
	 * {@link SimpleRegression} e {@link StandardDeviation} per dare in output
	 * Kq, ΔKq, Ke, ΔKe, Ra, ΔRa, SamplesCount. Per prima cosa individua i
	 * subset di accelerazione all'interno del set completo di dati, poi per
	 * ogni set sfrutta i metodi {@link #calculateKe(double[], double[])},
	 * {@link #calculateKq(double[], double[], double, double, double[])} e
	 * {@link #calculateRa(double[], double[], double[])}. Infine produce come
	 * output una media dei valori calcolati sui singoli subset. Produce un
	 * output più dettagliato dei calcoli all'interno del file creato nel
	 * costruttore.
	 * 
	 * @see analyzer.Analyzer#calcola()
	 */
	public void calcola() {
		pw.write("Kq,ΔKq,Ke,ΔKe,Ra,ΔRa,From,To,SamplesCount\n");
		ArrayList<double[]> KqsAndΔ = new ArrayList<>();
		ArrayList<double[]> KesAndΔ = new ArrayList<>();
		ArrayList<double[]> RasAndΔ = new ArrayList<>();

		// rpm to rad/s
		List<Double> temp = table.get("RPM");
		for (int i = 0; i < temp.size(); i++)
			temp.set(i, (temp.get(i) * 2 * Math.PI / 60));

		List<Integer[]> subsets = findSubsets();

		for (Integer[] set : subsets) {

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
					+ tempRa[1] + "," + set[0] + "," + set[1] + "," + (set[1] - set[0] + 1) + "\n");
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
		results.put("Subsets Count", (double) subsets.size());

	}

	/**
	 * Trova tutti i subset utili per l'analisi dei dati e il calcolo dei
	 * coefficienti del motore. I subset devono essere almeno grandi quanto il
	 * parametro SubsetSize. Affinchè un valore venga incluso in un subset, la
	 * media delle derivate rispetto ai 10 valori successivi e ai 10 valori
	 * precedenti deve essere positiva. Un subset viene considerato concluso
	 * quando questa condizione non è più verificata (i valori iniziano a
	 * calare).
	 * 
	 * @return una {@link List} di array di due valori, indicati rispettivamente
	 *         l'indice del primo e dell'ultimo valore del subset, estremi
	 *         inclusi.
	 */
	private List<Integer[]> findSubsets() {
		ArrayList<Integer[]> result = new ArrayList<>();
		int first = 0;
		int last = 0;
		int samplesToConsider = 20;

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
				}
				first = i;
				last = i;
			}
		}
		return result;
	}

	/**
	 * Calcola Kq con questi passi:<br>
	 * 1. calcolare accelerazione angolare con regressione lineare sugli rpm<br>
	 * 2. calcolare la coppia applicata dal motore come acc. per inerzia<br>
	 * 3. calcolare la corrente media con media sui valori della corrente<br>
	 * 4. calcolare Kq come rapporto tra coppia e corrente media
	 * 
	 * @param omegas
	 * @param currents
	 * @param I
	 * @param ΔI
	 * @param times
	 * @return
	 */
	private static double[] calculateKq(double[] omegas, double[] currents, double I, double ΔI, double[] times) {
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

	private static double[] calculateKe(double[] tensions, double[] omegas) {
		// calcolare Ke come slope della regressione lineare tra tensione e
		// velocità angolare
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(omegas, tensions));
		return new double[] { regression.getSlope(), regression.getSlopeStdErr() };
	}

	private static double[] calculateRa(double[] tensions, double[] omegas, double[] currents) {
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

	private static double[][] toMatrix(double[] x, double[] y) {
		double[][] result = new double[x.length][2]; // must be the same size
		for (int i = 0; i < x.length; i++) {
			result[i][0] = x[i];
			result[i][1] = y[i];
		}
		return result;
	}

	private double[] toPrimitiveType(List<Double> list) {
		double[] result = new double[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}
}
