package esc;

import java.io.OutputStreamWriter;
import java.util.List;

import gnu.io.SerialPort;

public class Routine implements Runnable {

	public AbstractEsc esc;
	protected SerialPort serialPort;
	protected OutputStreamWriter writer;
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
}
