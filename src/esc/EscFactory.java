package esc;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import gnu.io.SerialPort;

public class EscFactory {

	public static Map<String, Class<? extends AbstractEsc>> getEscsMap() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("esc");
		Set<Class<? extends AbstractEsc>> subTypes = reflections.getSubTypesOf(AbstractEsc.class);
		Map<String, Class<? extends AbstractEsc>> escMap;
		escMap = new HashMap<String, Class<? extends AbstractEsc>>();
		for (Class<? extends AbstractEsc> c : subTypes)
			escMap.put(c.getSimpleName(), c);
		// TODO: HARDCODED BRUTTO, usare reflection tipo
		// escMap.put(AutoQuadEsc32.class.getSimpleName(), AutoQuadEsc32.class);
		return escMap;
	}

	public static AbstractEsc newInstanceOf(Class<? extends AbstractEsc> cls, SerialPort port) {
		try {
			return (AbstractEsc) cls.getConstructor(SerialPort.class).newInstance(port);
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