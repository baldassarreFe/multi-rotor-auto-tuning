package routine;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import esc.AbstractEsc;
import esc.TelemetryParameter;

/**
 * A routine is a {@link Runnable} that given a {@link List} of instructions and
 * an {@link AbstractEsc} executes the instructions sequentially. A routine can
 * be stopped before its completion, stopping a routine in this way has the
 * effect of leaving the Esc running the last instruction that was sent, unless
 * stopped otherwise. Also a Static premade routines are provided as example and
 * for a fast test of other components, therefore should not be used otherwise.
 *
 */
public class Routine implements Runnable {

	public final static Routine accelerateRoutine = new AccelerateRoutine();
	public final static Routine exampleRoutine = new ConstantSpeedRoutine();
	public final static Routine stopAll = new Routine("Stop",
			new ArrayList<TelemetryParameter>(), new ArrayList<Instruction>(
					Arrays.asList(new Instruction[] { Instruction.newStop(),
							Instruction.newStopTelemetry(),
							Instruction.newDisarm() })));

	public AbstractEsc esc;
	protected String name;
	protected List<TelemetryParameter> params;
	protected List<Instruction> instructions;
	private AtomicBoolean isRunning;

	/**
	 * Constructs a routine without attaching an Esc to it. Associates a name,
	 * the {@link List} of {@link Instruction} and the {@link List} of
	 * {@link TelemetryParameter} to use.
	 * 
	 * @param name
	 *            the name of the routine
	 * @param params
	 *            a {@link List} of {@link TelemetryParameter}, pass an empty
	 *            list if none are required
	 * @param instructions
	 *            a {@link List} of {@link Instruction}
	 */
	public Routine(String name, List<TelemetryParameter> params,
			List<Instruction> instructions) {
		if (name == null || params == null || instructions == null)
			throw new IllegalArgumentException();
		this.name = name;
		this.params = params;
		this.instructions = instructions;
		isRunning = new AtomicBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Routine other = (Routine) obj;
		if (instructions == null) {
			if (other.instructions != null)
				return false;
		} else if (!instructions.equals(other.instructions))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

	/**
	 * @return the {@link PipedOutputStream} where the Esc writes its telemetry
	 *         filtered by the {@link TelemetryParameter} specified when
	 *         creating the routine
	 */
	public PipedOutputStream getOutput() {
		return esc.getPipedOutput();
	}

	/**
	 * @return the {@link List} of {@link TelemetryParameter} that can be read
	 *         through the {@link PipedOutputStream} given by
	 *         {@link #getOutput()}
	 */
	public List<TelemetryParameter> getParameters() {
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (instructions == null ? 0 : instructions.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (params == null ? 0 : params.hashCode());
		return result;
	}

	/**
	 * After setting the telemetry parameters on the Esc, continues sending the
	 * instructions unless the routine is stopped with
	 * {@link #stopImmediately()}
	 */
	@Override
	public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		isRunning.set(true);
		if (instructions == null)
			throw new IllegalStateException(
					"Routine has null set of instructions");
		if (esc == null)
			throw new IllegalStateException("Routine has no ESC attached");
		esc.setTelemetryParameters(params);
		long start = System.nanoTime();
		for (Instruction i : instructions)
			if (isRunning.get() == true)
				esc.executeInstruction(i);
			else
				break;
		long end = System.nanoTime();
		System.out.println("Routine " + this + " terminata dopo: " + (end - start) * 1E-6 + " ms");
	}

	/**
	 * Sets the Esc on which this routine will be run, must be called before a
	 * thread is created and started with this routine
	 * 
	 * @param esc
	 */
	public void setEsc(AbstractEsc esc) {
		this.esc = esc;
	}

	/**
	 * Prevents the routine from sending other instructions to the Esc, has no
	 * effect if the routine has not been started yet
	 */
	public void stopImmediately() {
		isRunning.set(false);
	}

	@Override
	public String toString() {
		return name;
	}
}
