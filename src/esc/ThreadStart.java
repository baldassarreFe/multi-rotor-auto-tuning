package esc;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import esc.ElectronicSpeedController;
import gnu.io.SerialPort;
import port.PortSelector;

public class ThreadStart extends Thread {

	public void run(OutputStream outputArea) {
		PortSelector sel = new PortSelector();
		SerialPort serialPort = sel.connect(sel.selectPort(sel.scanPorts()));
		ElectronicSpeedController esc = null;
		try {
			esc = new ElectronicSpeedController(serialPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OutputStreamWriter writer = new OutputStreamWriter(outputArea);
		
		esc.setTelemetryParameters(new String[] {"RPM", "AMPS AVG", "MOTOR VOLTS"});
		esc.arm()
			.sleep(1000)
			.startTelemetry(50, writer)
			.start()
			.accelerate(2000, 4000, 500)
			.accelerate(4000, 2000, -300)
			.sleep(5000)
			.stopTelemetry()
			.stop()
			.disarm();
		
		serialPort.close();
	}

}
