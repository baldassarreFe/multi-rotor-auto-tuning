package analyzer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class Analyzer {
	protected File logFile;
	public Map<String, Double> parametersRequired;
	public Map<String, Double> results;
	
	public Analyzer(File logFile){
		this.logFile = logFile;
		this.parametersRequired = new HashMap<>();
		this.results = new HashMap<>();
	}

	public abstract void calcola();
}
