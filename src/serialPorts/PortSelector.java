package serialPorts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class PortSelector {

	static final int TIMEOUT = 2000;

	public static SerialPort connect(CommPortIdentifier selectedPortIdentifier)
			throws PortInUseException, UnsupportedCommOperationException {
		SerialPort serialPort = null;
		// the method below returns an object of type SerialPort
		serialPort = (SerialPort) selectedPortIdentifier.open("multi-rotor-auto-tuning", TIMEOUT);
		// TODO permettere all'utente di modificare questi parametri da
		// interfaccia grafica o caricarli da file .properties
		// Questi vanno bene per AutoquadEsc32
		serialPort.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		System.out.println("Connection Established");
		return serialPort;
	}

	public static void disconnect(SerialPort serialPort) {
		System.out.println("Closing connection");
		serialPort.close();
	}

	@SuppressWarnings("unchecked")
	public static List<CommPortIdentifier> scanPorts() {
		List<CommPortIdentifier> portList = new ArrayList<CommPortIdentifier>();

		// for containing the ports that will be found
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

		if (!ports.hasMoreElements())
			System.out.println("No Ports Available");
		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = ports.nextElement();
			// get only serial ports
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
				portList.add(curPort);
		}
		return portList;
	}

	/**
	 * Usato nell'interfaccia a riga di comando
	 *
	 * @param portMap
	 * @return
	 */
	@Deprecated
	public static CommPortIdentifier selectPort(Map<String, CommPortIdentifier> portMap) {
		CommPortIdentifier selectedPortIdentifier;
		for (CommPortIdentifier c : portMap.values())
			System.out.println(c.getName());
		if (portMap.size() == 1) {
			selectedPortIdentifier = portMap.values().toArray(new CommPortIdentifier[1])[0];
			return selectedPortIdentifier;
		}
		selectedPortIdentifier = null;
		BufferedReader console = null;
		try {
			console = new BufferedReader(new InputStreamReader(System.in, "CP850"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		do
			try {
				System.out.print("Choose a port: ");
				String choice = console.readLine();
				selectedPortIdentifier = portMap.get(choice);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		while (selectedPortIdentifier == null);
		return selectedPortIdentifier;
	}
}
