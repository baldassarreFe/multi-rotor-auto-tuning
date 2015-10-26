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
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out));
		FileWriter file = new FileWriter("test.txt");
		
		esc.arm()
			.sleep(1000)
			.startTelemetry(10, writer)
			.start()
			.sleep(10000)
			.startTelemetry(10, file)
			.sleep(10000)
			.stopTelemetry()
			.stop()
			.disarm();
		
		serialPort.close();
		System.exit(0);
	}

}
