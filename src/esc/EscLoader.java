package esc;

import java.util.List;

import gnu.io.SerialPort;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class EscLoader {
	private List<Class> escList;
	
	public EscLoader() {
		escList = new ArrayList<Class>();
		escList.add(AutoQuadEsc32.class);  //HARDCODED BRUTTO
	}
	public List getEscList() {
		return escList;
	}

	public AbstractEsc newInstanceOf(Class cls, SerialPort port) {
		if (AbstractEsc.class.isAssignableFrom(cls)) {
			try {
				return (AbstractEsc) cls.getConstructor(SerialPort.class).newInstance(port);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;

	}
}
