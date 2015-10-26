package port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

public class PortSelector {
	
	final int TIMEOUT = 2000;
	
	public CommPortIdentifier selectPort(Map<String, CommPortIdentifier> portMap ) {
		CommPortIdentifier selectedPortIdentifier;
		for (CommPortIdentifier c : portMap.values()) {
			System.out.println(c.getName());
		}
		if (portMap.size() == 1) {
			selectedPortIdentifier = portMap.values().toArray(new CommPortIdentifier[1])[0];
			return selectedPortIdentifier;
		}
		selectedPortIdentifier = null;
		BufferedReader console = null;
		try {
			console = new BufferedReader(new InputStreamReader(System.in, "CP850"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		do {
			try {
				
				System.out.print("Choose a port: ");
				String choice = console.readLine();
				selectedPortIdentifier = portMap.get(choice);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} while (selectedPortIdentifier == null);
		return selectedPortIdentifier;
	}

	public Map<String, CommPortIdentifier> scanPorts() {
		// for containing the ports that will be found
		Enumeration<CommPortIdentifier> ports = null;
		
		// map the port names to CommPortIdentifiers
		HashMap<String, CommPortIdentifier> portMap = new HashMap<>();
		
		ports = CommPortIdentifier.getPortIdentifiers();

		if (!ports.hasMoreElements()) {
			System.out.println("No Ports Available");
			System.exit(1);
		}
		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
			// get only serial ports
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portMap.put(curPort.getName(), curPort);
			}
		}
		
		return portMap;
	}

	public SerialPort connect(CommPortIdentifier selectedPortIdentifier) {
		SerialPort serialPort = null;
		try {
			// the method below returns an object of type SerialPort
			serialPort = (SerialPort) selectedPortIdentifier.open("federico", TIMEOUT);
			serialPort.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			System.out.println("Connection Established");
		} catch (PortInUseException e) {
			System.out.println(selectedPortIdentifier.getName() + " is in use. (" + e.toString() + ")");
		} catch (Exception e) {
			System.out.println("Failed to open " + selectedPortIdentifier.getName() + "(" + e.toString() + ")");
		}
		
		return serialPort;
	}
	
	public void disconnect(SerialPort serialPort) {
		System.out.println("Closing connection");
		serialPort.close();
	}
}

