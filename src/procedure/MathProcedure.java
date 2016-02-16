package procedure;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class MathProcedure {

	private static double[][] toMatrix(double[] x, double[] y) {
		double[][] result = new double[x.length][2]; // must be the same size
		for (int i = 0; i < x.length; i++) {
			result[i][0] = x[i];
			result[i][1] = y[i];
		}
		return result;
	}

	public static double calculateKq(double[] w, double[] i, double I, double[] times) {
		double wmax;
		double wmin;

		if (w[0] < w[w.length]) {
			wmax = w[w.length];
			wmin = w[0];
		}

		else {
			wmax = w[0];
			wmin = w[w.length];
		}

		double time = times[w.length] - times[0];

		double acceleration = (wmax - wmin) / time; // SE SUPPONIAMO CHE SIA
													// COSTANTE
		double torque = I * acceleration;
		Mean mean = new Mean();
		double imean = mean.evaluate(i, 0, i.length);

		return torque / imean;
	}

	public static double calculateKe(double[] v, double[] w) {
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(v, w));

		return regression.getSlope();
	}

	public static double calculateRa(double[] v, double[] w, double[] i) {
		SimpleRegression regression = new SimpleRegression();
		regression.addData(toMatrix(v, w));
		double a = regression.getIntercept();

		Mean mean = new Mean();
		double imean = mean.evaluate(i, 0, i.length);

		return a / imean;
	}

}
