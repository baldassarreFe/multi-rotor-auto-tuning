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
			.accelerate(4000, 9000, 1000)
			.accelerate(9000, 1000, -3000)
			.accelerate(1000, 1050, 10)
			.accelerate(2000, 5000, 10)
			.accelerate(5000, 7000, 100)
			.stop()
			.sleep(5000)
			.stopTelemetry()
			.disarm();
		serialPort.close();
	}

}
