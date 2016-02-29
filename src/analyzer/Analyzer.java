package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import esc.FileFormatException;

/**
 * Classe astratta di base per analizzare dati generati da una routine. Permette
 * caricare delle specifiche colonne da un file di input sulla base delle
 * colonne indicate dalle sottoclassi nel campo {@link #table}. Permette inoltre
 * di caricare dei parametri da un file di properties sulla base dei parametri
 * indicate dalle sottoclassi nel campo {@link #parametersRequired}. I risultati
 * del calcolo saranno accessibili tramite il campo results dopo aver invocato
 * il metodo {@link #calcola()} *
 */
public abstract class Analyzer {
	private File propertyFile;
	private File dataFile;
	private boolean hasSuperBeenCalled = false;

	protected Map<String, List<Double>> table;
	public Map<String, Double> parametersRequired;
	public Map<String, Double> results;

	/**
	 * This constructor <b>must</b> be called by the implementations or else the
	 * maps and the source files can not be initializated
	 *
	 * @param dataFile
	 * @param propertyFile
	 */
	public Analyzer(File dataFile, File propertyFile) {
		if (dataFile == null)
			throw new IllegalArgumentException();
		this.dataFile = dataFile;
		this.propertyFile = propertyFile;
		table = new HashMap<>();
		parametersRequired = new LinkedHashMap<>();
		results = new LinkedHashMap<>();
		hasSuperBeenCalled = true;
	}

	/**
	 * Sulla base dei dati presenti in {@link #table} e
	 * {@link #parametersRequired} riempie {@link #results} con i risultati dei
	 * calcoli.
	 */
	public abstract void calcola();

	/**
	 * Loads the parameters from the file specified in the constructor into the
	 * {@link #parametersRequired} map. If no file has been specified nothing is
	 * loaded. If some of the parameters are not found these are left null.
	 */
	protected void loadParameters() {
		if (!hasSuperBeenCalled)
			throw new IllegalStateException("Implementation has not called the super constructor");
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
			for (String s : parametersRequired.keySet())
				try {
					Double value = Double.valueOf((String) properties.get(s));
					parametersRequired.put(s, value);
				} catch (NullPointerException ignore) {
					// means that the key is not present in the file, no problem
				} catch (NumberFormatException e) {
					// key is present in the fle, but the number is not valid
					e.printStackTrace();
				}
		}
	}

	/**
	 * Loads the data from the file specified in the constructor into the
	 * {@link #table} map. Se una riga della tabella non presenta uno o più
	 * valori viene scartata
	 *
	 * @throws IOException
	 *             if an error occurs during I/O
	 * @throws FileFormatException
	 *             if one of the column specified in the map is missing
	 */
	protected void readDataFromFile() throws IOException, FileFormatException {
		if (!hasSuperBeenCalled)
			throw new IllegalStateException("Implementation has not called the super constructor");
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));

		// Le colonne richieste dalla routine sono specificate nel campo table,
		// qui verifichiamo che ci siano tutte quelle necessarie
		int cont = 0;
		String line = reader.readLine();
		String[] header = line.split(",");
		for (String element : header)
			if (table.containsKey(element))
				cont++;
		if (table.size() != cont) {
			// nel file non ci sono le colonne necessarie all'analisi
			reader.close();
			throw new FileFormatException("File formatting problem: unable to find some columns");
		}

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(",");
			// se su una riga c'è anche solo un buco saltiamo la riga, si
			// potrebbe migliorare (esempio saltando la riga solo se il buco è
			// in una delle colonne necessarie all'analisi),
			// ma per comodità nel fare la regressione è meglio fare così
			if (tokens.length != header.length)
				continue;

			for (int i = 0; i < tokens.length; i++)
				try {
					if (table.containsKey(header[i]))
						table.get(header[i]).add(Double.parseDouble(tokens[i]));
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException ignore) {
					System.err.println("Errore alla riga: " + line);
					ignore.printStackTrace();
				}
		}
		reader.close();
	}
}
