package esc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import gnu.io.SerialPort;

public class EscFactory {

	public static List<Class<? extends AbstractEsc>> getEscsList() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("esc");
		Set<Class<? extends AbstractEsc>> subTypes = reflections.getSubTypesOf(AbstractEsc.class);
		return new ArrayList<>(subTypes);
	}

	public static AbstractEsc newInstanceOf(Class<? extends AbstractEsc> cls, SerialPort port)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return (AbstractEsc) cls.getConstructor(SerialPort.class).newInstance(port);
	}
}
