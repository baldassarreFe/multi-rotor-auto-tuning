package analyzer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import gnu.io.SerialPort;

public class AnalyzersFactory {

	public static Map<String, Class<? extends Analyzer>> getAnalyzersMap() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("analyzer");
		Set<Class<? extends Analyzer>> subTypes = reflections.getSubTypesOf(Analyzer.class);
		Map<String, Class<? extends Analyzer>> escMap;
		escMap = new HashMap<String, Class<? extends Analyzer>>();
		for (Class<? extends Analyzer> c : subTypes)
			escMap.put(c.getSimpleName(), c);
		return escMap;
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
