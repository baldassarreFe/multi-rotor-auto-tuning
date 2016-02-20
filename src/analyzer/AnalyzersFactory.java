package analyzer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class AnalyzersFactory {

	public static List<Class<? extends Analyzer>> getAnalyzersList() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("analyzer");
		Set<Class<? extends Analyzer>> subTypes = reflections.getSubTypesOf(Analyzer.class);
		return new ArrayList<>(subTypes);
	}

	public static Analyzer newInstanceOf(Class<? extends Analyzer> cls, File logFile) {
		try {
			return (Analyzer) cls.getConstructor(File.class).newInstance(logFile);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
