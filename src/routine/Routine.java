package routine;

import java.io.PipedOutputStream;
import java.util.List;

import esc.AbstractEsc;
import gnu.io.SerialPort;

public class Routine extends Thread {

	public AbstractEsc esc;
	protected SerialPort serialPort;
	protected List<Instruction> instructions;

	public Routine(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	public void setEsc(AbstractEsc esc) {
		this.esc = esc;
	}

	public void run() {
		if (instructions == null)
			throw new IllegalStateException("Routine has null ");
		if (esc == null)
			throw new IllegalStateException("Routine has no ESC attached");
		for (Instruction i : instructions)
			esc.executeInstruction(i);
	}

	public String toString(){
		return "Routine";
	}
	
	public PipedOutputStream getOutput() {
		return esc.getPipedOutput();
	}
}
