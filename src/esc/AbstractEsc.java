package esc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;

import gnu.io.SerialPort;

public abstract class AbstractEsc {
	protected InputStream input;
	protected OutputStream output;
	private SerialPort port;
	protected static HashSet<String> telemetryParameters = new HashSet<>();

	public AbstractEsc(SerialPort port) throws IOException {
		this.port = port;
		this.input = port.getInputStream();
		this.output = port.getOutputStream();
	}
	
	public final AbstractEsc sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public String toString(){
		return "AbstractEsc";
	}
	
	public void disconnect(){
		stop().disarm();
		port.close();
	}
	
	public abstract void executeInstruction(Instruction instruction);
	
	public abstract AbstractEsc setRPM(int rpm);
	
	public abstract AbstractEsc arm();
	
	public abstract AbstractEsc disarm();
	
	public abstract AbstractEsc start();
	
	public abstract AbstractEsc stop();
	
	/**
	 * @param from starting rpm
	 * @param to ending rpm
	 * @param pace acceleration in rpm / s 
	 * @return
	 */
	public abstract AbstractEsc accelerate(int from, int to, double pace);
	
	public abstract AbstractEsc sendRawCommand(String command);
	
	public abstract AbstractEsc startTelemetry(int frequency, Writer writer);
	
	public abstract AbstractEsc stopTelemetry();

	public abstract void addTelemetryParameter(String parameter);
	
	public abstract void setTelemetryParameters(String[] parameters);
	
	public abstract void removeTelemetryParameter(String parameter);
}
