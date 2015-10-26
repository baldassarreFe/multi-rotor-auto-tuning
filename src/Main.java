import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import esc.ElectronicSpeedController;
import gnu.io.SerialPort;
import port.PortSelector;

public class Main {

	public static void main(String[] args) throws IOException {
		PortSelector sel = new PortSelector();
		SerialPort serialPort = sel.connect(sel.selectPort(sel.scanPorts()));
		ElectronicSpeedController esc = null;
		try {
			esc = new ElectronicSpeedController(serialPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileWriter file = new FileWriter("test.txt");
		
		esc.setTelemetryParameters(new String[] {"RPM", "AMPS AVG", "MOTOR VOLTS"});
		esc.arm()
			.sleep(1000)
			.startTelemetry(50, file)
			.start()
			//.accelerate(4000, 9000, 1000)
			//.accelerate(9000, 1000, -3000)
			.accelerate(1000, 1050, 10)
			//.accelerate(2000, 5000, 10)
			//.accelerate(5000, 7000, 100)
			.stop()
			.sleep(5000)
			.stopTelemetry()
			.disarm();
		
		serialPort.close();
		System.exit(0);
	}

}
