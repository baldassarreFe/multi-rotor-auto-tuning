package analyzer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Analyzer {
	protected File logFile;
	public Map<String, Double> parametersRequired;
	public Map<String, Double> results;

	public Analyzer(File logFile) {
		this.logFile = logFile;
		this.parametersRequired = new LinkedHashMap<>();
		this.results = new LinkedHashMap<>();
	}

	public abstract void calcola();
}
