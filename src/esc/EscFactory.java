package esc;

import gnu.io.SerialPort;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class EscFactory {

	/**
	 * Scans the classes through reflection to find all the subclasses of
	 * {@link AbstractEsc}
	 *
	 * @return a list of subclasses of {@link AbstractEsc}
	 */
	public static List<Class<? extends AbstractEsc>> getEscsList() {
		// https://code.google.com/archive/p/reflections/
		Reflections reflections = new Reflections("esc");
		Set<Class<? extends AbstractEsc>> subTypes = reflections
				.getSubTypesOf(AbstractEsc.class);
		return new ArrayList<>(subTypes);
	}

	/**
	 * Given a {@link Class} that extends {@link AbstractEsc} and the
	 * {@link SerialPort} to use for the connection, creates a new instance of
	 * that class
	 *
	 * @param escImplementation
	 * @param port
	 *            the port to connect the esc to
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static AbstractEsc newInstanceOf(
			Class<? extends AbstractEsc> escImplementation, SerialPort port)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (escImplementation == null || port == null)
			throw new IllegalArgumentException();
		return escImplementation.getConstructor(SerialPort.class).newInstance(
				port);
	}
}
