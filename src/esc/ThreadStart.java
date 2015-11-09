package esc;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import esc.ElectronicSpeedController;
import gnu.io.SerialPort;
import serialPorts.PortSelector;

public class ThreadStart extends Thread {

	public ElectronicSpeedController esc;
	private SerialPort serialPort;
	private OutputStreamWriter writer;
	
	public ThreadStart(OutputStream outputArea) throws IOException{
		writer = new OutputStreamWriter(outputArea);
		PortSelector sel = new PortSelector();
		serialPort = sel.connect(sel.selectPort(sel.scanPorts()));	
		esc = new ElectronicSpeedController(serialPort);
	}
	
	public void run() {
		esc.setTelemetryParameters(new String[] {"RPM", "AMPS AVG", "MOTOR VOLTS"});
		esc.arm()
			.sleep(1000)
			.startTelemetry(50, writer)
			.start()
			.sleep(10000)
			//.accelerate(4000, 9000, 1000)
			//.accelerate(9000, 1000, -3000)
			//.accelerate(1000, 1050, 10)
			//.accelerate(2000, 5000, 10)
			//.accelerate(5000, 7000, 100)
			.stop()
			.sleep(5000)
			.stopTelemetry()
			.disarm();
		serialPort.close();
	}

}
