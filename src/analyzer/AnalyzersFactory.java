package analyzer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class AnalyzersFactory {

	/**
	 * Scans the classes through reflection to find all the subclasses of
	 * {@link Analyzer}
	 * 
	 * @return a list of subclasses of {@link Analyzer}
	 */
	public static List<Class<? extends Analyzer>> getAnalyzersList() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("");
		Set<Class<? extends Analyzer>> subTypes = reflections
				.getSubTypesOf(Analyzer.class);
		return new ArrayList<>(subTypes);
	}

	/**
	 * Given a {@link Class} that extends {@link Analyzer} and the files to use
	 * as input, creates a new instance of that class
	 *
	 * @param analyzerSubclass
	 * @param dataFile
	 *            the file where the motor data should be loaded from
	 * @param propertyFile
	 *            the file where the properties should be loaded from, can be
	 *            null
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Analyzer newInstanceOf(
			Class<? extends Analyzer> analyzerSubclass, File dataFile,
			File propertyFile) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (analyzerSubclass == null || dataFile == null)
			throw new IllegalArgumentException();
		return analyzerSubclass.getConstructor(File.class, File.class)
				.newInstance(dataFile, propertyFile);
	}
}
