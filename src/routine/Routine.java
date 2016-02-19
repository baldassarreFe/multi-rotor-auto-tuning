package routine;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import esc.AbstractEsc;
import esc.TelemetryParameter;
import gnu.io.SerialPort;

public class Routine implements Runnable {
	
	@Deprecated
	public final static Routine stopAll = new Routine("Stop", new ArrayList<TelemetryParameter>(), new ArrayList<Instruction>(Arrays.asList(new Instruction[] {Instruction.STOP,Instruction.STOP_TELEMETRY,Instruction.DISARM})));
	
	public final static Routine exampleRoutine = new ExampleRoutine();
	public final static Routine accelerateRoutine = new AccelerateRoutine();
	
	public AbstractEsc esc;
	protected SerialPort serialPort;
	protected String name;
	protected List<TelemetryParameter> params;
	protected List<Instruction> instructions;

	public Routine(String name, List<TelemetryParameter> params, List<Instruction> instructions) {
		this.name = name;
		this.params = params;
		this.instructions = instructions;
	}

	public void setEsc(AbstractEsc esc) {
		this.esc = esc;
	}

	public void run() {
		if (instructions == null)
			throw new IllegalStateException("Routine has null set of instructions");
		if (esc == null)
			throw new IllegalStateException("Routine has no ESC attached");
		esc.setTelemetryParameters(params);
		for (Instruction i : instructions)
			esc.executeInstruction(i);
	}
	
	public List<TelemetryParameter> getParameters(){
		return params;
	}

	public String toString(){
		return name;
	}
	
	public PipedOutputStream getOutput() {
		return esc.getPipedOutput();
	}
}
