package routine;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import esc.AbstractEsc;
import esc.TelemetryParameter;
import gnu.io.SerialPort;

public class Routine implements Runnable {

	@Deprecated
	public final static Routine stopAll = new Routine("Stop", new ArrayList<TelemetryParameter>(),
			new ArrayList<Instruction>(Arrays
					.asList(new Instruction[] { Instruction.STOP, Instruction.STOP_TELEMETRY, Instruction.DISARM })));

	public final static Routine exampleRoutine = new ExampleRoutine();
	public final static Routine accelerateRoutine = new AccelerateRoutine();

	public AbstractEsc esc;
	protected SerialPort serialPort;
	protected String name;
	protected List<TelemetryParameter> params;
	protected List<Instruction> instructions;
	private AtomicBoolean isRunning;

	public Routine(String name, List<TelemetryParameter> params, List<Instruction> instructions) {
		this.name = name;
		this.params = params;
		this.instructions = instructions;
		isRunning = new AtomicBoolean();
	}

	public PipedOutputStream getOutput() {
		return esc.getPipedOutput();
	}

	public List<TelemetryParameter> getParameters() {
		return params;
	}

	@Override
	public void run() {
		isRunning.set(true);
		if (instructions == null)
			throw new IllegalStateException("Routine has null set of instructions");
		if (esc == null)
			throw new IllegalStateException("Routine has no ESC attached");
		esc.setTelemetryParameters(params);
		for (Instruction i : instructions)
			if (isRunning.get() == true)
				esc.executeInstruction(i);
			else
				return;
	}

	public void setEsc(AbstractEsc esc) {
		this.esc = esc;
	}

	public void stopImmediately() {
		isRunning.set(false);
	}

	@Override
	public String toString() {
		return name;
	}
}
