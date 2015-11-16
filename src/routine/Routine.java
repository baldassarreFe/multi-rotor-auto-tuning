package routine;

import java.io.PipedOutputStream;
import java.util.List;

import esc.AbstractEsc;
import esc.TelemetryParameters;
import gnu.io.SerialPort;

public class Routine implements Runnable {

	public AbstractEsc esc;
	protected SerialPort serialPort;
	protected String name;
	protected List<TelemetryParameters> params;
	protected List<Instruction> instructions;

	public Routine(String name, List<TelemetryParameters> params, List<Instruction> instructions) {
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
		for (Instruction i : instructions)
			esc.executeInstruction(i);
	}

	public String toString(){
		return name;
	}
	
	public PipedOutputStream getOutput() {
		return esc.getPipedOutput();
	}
}
