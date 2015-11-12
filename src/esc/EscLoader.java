package esc;

import java.util.List;

import gnu.io.SerialPort;

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
			cls.getConstructor(SerialPort.class);
		};
	}
}
