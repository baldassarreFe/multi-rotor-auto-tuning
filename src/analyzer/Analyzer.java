package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

/**
 * Classe astratta di base per analizzare dati generati da una routine. Permette
 * caricare delle specifiche colonne da un file di input sulla base delle
 * colonne indicate dalle sottoclassi nel campo {@link table}. Permette
 * inoltre di caricare dei parametri da un file di properties sulla base dei
 * parametri indicate dalle sottoclassi nel campo
 * {@link parametersRequired}. I risultati del calcolo saranno
 * accessibili tramite il campo results dopo aver invocato il metodo
 * {@link calcola()}
 *
 */
public abstract class Analyzer {
	private File propertyFile;
	private File dataFile;

	protected Map<String, ArrayList<Double>> table;
	public Map<String, Double> parametersRequired;
	public Map<String, Double> results;

	public Analyzer(File logFile, File propertyFile) {
		this.dataFile = logFile;
		this.propertyFile = propertyFile;
		this.table = new HashMap<>();
		this.parametersRequired = new LinkedHashMap<>();
		this.results = new LinkedHashMap<>();
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

	protected void readDataFromFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));

		// Le colonne richieste dalla routine sono specificate nel campo table,
		// qui verifichiamo che ci siano tutte quelle necessarie
		int cont = 0;
		String line = reader.readLine();
		String[] header = line.split(",");
		for (int i = 0; i < header.length; i++) {
			if (table.containsKey(header[i]))
				cont++;
		}
		if (table.size() != cont) {
			// nel file non ci sono le colonne necessarie all'analisi
			reader.close();
			throw new IOException("File formatting problem: unable to find some columns");
		}

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(",");
			// se su una riga c'è anche solo un buco saltiamo la riga, si
			// potrebbe migliorare,
			// ma per comodità nel fare la regressione è meglio fare così
			if (tokens.length != table.size())
				continue;

			for (int i = 0; i < tokens.length; i++) {
				try {
					if (table.containsKey(tokens[i]))
						table.get(header[i]).add(Double.parseDouble(tokens[i]));
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException ignore) {
					System.err.println("Errore alla riga: " + line);
					ignore.printStackTrace();
				}
			}
		}
		reader.close();
	}

	public abstract void calcola();
}
