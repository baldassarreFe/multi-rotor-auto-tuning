
//import javax.comm.*;
import gnu.io.*;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

public class Communicator {
	private static FileWriter file;
	// for containing the ports that will be found
	private static Enumeration<CommPortIdentifier> ports = null;
	// map the port names to CommPortIdentifiers
	private static HashMap<String, CommPortIdentifier> portMap = new HashMap<>();

	// this is the object that contains the opened port
	private static CommPortIdentifier selectedPortIdentifier = null;
	private static SerialPort serialPort = null;

	// input and output streams for sending and receiving data
	private static InputStream input = null;
	private static OutputStream output = null;
	private static ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();

	// the timeout value for connecting with the port
	final static int TIMEOUT = 2000;

	private static BufferedReader console;

	public static void main(String[] args) {
		try {
			console = new BufferedReader(new InputStreamReader(System.in, "CP850"));
			new File("log.csv").delete();
			file = new FileWriter("log.csv");
		} catch (IOException e1) {
			e1.printStackTrace(); System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("FINE DEI GIOCHI");
				try {
					file.flush();
					file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				close();
			}
		});
		
		scanPorts();
		selectPort();
		connect();
		
		new Thread(new Runnable() {
			
			private int i=1;
			@Override
			public void run() {
				try {
					byte singleData;
					while ((singleData = (byte) input.read()) != -1) {
						inputBuffer.write(singleData);
						if (singleData == 10) {
							ByteArrayInputStream bin = new ByteArrayInputStream(inputBuffer.toByteArray());
							BufferedReader reader = new BufferedReader(new InputStreamReader(bin, "UTF-8"));
							String line = reader.readLine();
							if (line.startsWith("RPM")) {
								StringTokenizer st = new StringTokenizer(line);
								st.nextToken();
								file.write(st.nextToken() + ",");
							} else if (line.startsWith("MOTOR VOLTS")) {
								StringTokenizer st = new StringTokenizer(line);
								st.nextToken();
								st.nextToken();
								file.write(st.nextToken() + "\n");
								if(i++%50==0) 
									System.out.println("lette triplette " + i);
							} else if (line.startsWith("AMPS AVG")) {
								StringTokenizer st = new StringTokenizer(line);
								st.nextToken();
								st.nextToken();
								file.write(st.nextToken() + ",");
							}
							inputBuffer.reset();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		sendCommand("arm");
		sendCommand("start");
		sendCommand("rpm 1000");
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendCommand("telemetry 50");
		for (int i = 1000; i <= 6000; i += 50) {
			sendCommand("rpm " + i);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		for (int i = 3000; i >= 1000; i -= 10) {
//			sendCommand("rpm " + i);
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		sendCommand("stop");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendCommand("telemetry 0");
		try {
			while (input.available()>0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void close() {
		sendCommand("telemetry 0");
		sendCommand("stop");
		sendCommand("disarm");
		serialPort.close();
		System.out.println("Connessione chiusa");
	}

	private static void sendCommand(String string) {
		try {
			// l'ESC usa la codifica UTF-8 e ha bisogno dei caratteri di LF e CR
			output.write(string.trim().getBytes("UTF-8"));
			output.write(13); // LF
			output.write(10); // CR
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void selectPort() {
		for (CommPortIdentifier c : portMap.values()) {
			System.out.println(c.getName());
		}
		if (portMap.size() == 1) {
			selectedPortIdentifier = portMap.values().toArray(new CommPortIdentifier[1])[0];
			return;
		}
		selectedPortIdentifier = null;
		do {
			try {
				System.out.print("DECIDITI: ");
				String choice = console.readLine();
				selectedPortIdentifier = portMap.get(choice);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} while (selectedPortIdentifier == null);
	}

	public static void scanPorts() {
		ports = CommPortIdentifier.getPortIdentifiers();
		if (!ports.hasMoreElements()) {
			System.out.println("Niente porte");
			System.exit(1);
		}
		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
			// get only serial ports
			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portMap.put(curPort.getName(), curPort);
			}
		}
	}

	public static void connect() {
		try {
			// the method below returns an object of type CommPort, the CommPort
			// object can be casted to a SerialPort object
			serialPort = (SerialPort) selectedPortIdentifier.open("federico", TIMEOUT);
			serialPort.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			System.out.println("Connessione aperta");
		} catch (PortInUseException e) {
			System.out.println(selectedPortIdentifier.getName() + " is in use. (" + e.toString() + ")");
		} catch (Exception e) {
			System.out.println("Failed to open " + selectedPortIdentifier.getName() + "(" + e.toString() + ")");
		}
	}
}