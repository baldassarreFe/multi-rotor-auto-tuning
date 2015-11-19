package esc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;

import gnu.io.SerialPort;
import routine.Instruction;

public abstract class AbstractEsc {
	protected InputStream input;
	protected OutputStream output;
	protected PipedOutputStream pipedOutput;
	private SerialPort port;
	protected static HashSet<TelemetryParameter> telemetryParameters = new HashSet<>();

	public AbstractEsc(SerialPort port) throws IOException {
		this.port = port;
		this.input = port.getInputStream();
		this.output = port.getOutputStream();
		this.pipedOutput = new PipedOutputStream();
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
	
	public void stopAndDisconnect(){
		executeInstruction(Instruction.STOP);
		executeInstruction(Instruction.STOP_TELEMETRY);
		executeInstruction(Instruction.DISARM);
		port.close();
	}

	public PipedOutputStream getPipedOutput() {
		return pipedOutput;
	}

	public abstract void executeInstruction(Instruction instruction);

	public abstract void addTelemetryParameter(TelemetryParameter parameter);
	
	public abstract void setTelemetryParameters(Set<TelemetryParameter> parameters);
	
	public abstract void removeTelemetryParameter(TelemetryParameter parameter);
}
