package analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Analyzer {
	protected File logFile;
	public Map<String, Double> parametersRequired;
	public Map<String, Double> results;
	private File propertyFile;

	public Analyzer(File logFile, File propertyFile) {
		this.logFile = logFile;
		this.parametersRequired = new LinkedHashMap<>();
		this.results = new LinkedHashMap<>();
		this.propertyFile = propertyFile;
	}

	protected void loadParameters() {
		if (propertyFile != null) {
			Properties properties = new Properties();
			try {
				InputStream inputStream = new FileInputStream(propertyFile);
				properties.load(inputStream);
				inputStream.close();
			} catch (IOException e) {
				System.out.println("Properties loading failed");
				return;
			}
			// provo a loadare tutti i parameters richiesti dalla mappa leggendo
			// il file
			for (String s : parametersRequired.keySet()) {
				Double value = Double.valueOf((String) properties.get(s));
				parametersRequired.put(s, value);
			}
		}
	}

	public abstract void calcola();
}
