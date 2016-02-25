package routine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import esc.AbstractEsc;
import esc.TelemetryParameter;

/**
 * Used only for test purposes, custom routines can be created passing a list of
 * {@link Instruction} to {@link Routine#Routine(String, List, List)}, even if the best way to
 * define a routine is by creating a .rou file that can be parsed through a
 * {@link RoutineLoader}
 */
@Deprecated
public class AccelerateRoutine extends Routine {

	public AbstractEsc esc;

	public AccelerateRoutine() {
		super("Accelera 1000->2000 a 50 rpm/s", Arrays.asList(TelemetryParameter.values()), new ArrayList<Instruction>());
		List<Instruction> instrs = new ArrayList<>();
		instrs.add(Instruction.newArm());
		instrs.add(Instruction.newStart());
		instrs.add(Instruction.newSleep(3000));
		instrs.add(Instruction.newSetTelemetry(10));
		instrs.add(Instruction.newAcceleration(1000, 2000, 50));
		instrs.add(Instruction.newSleep(3000));
		instrs.add(Instruction.newStop());
		instrs.add(Instruction.newDisarm());
		this.instructions = instrs;
	}
}
