//ciao
package esc;

import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.SerialPort;

public class ElectronicSpeedController {
	private SerialPort port;
	private InputStream input;
	private OutputStream output;

	public ElectronicSpeedController(SerialPort port) {
		this.port = port;
		this.input = port.getInputStream();
		this.output = port.getOutputStream();
	}
	
	
}
