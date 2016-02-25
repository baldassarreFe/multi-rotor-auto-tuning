package routine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import esc.AbstractEsc;
import esc.TelemetryParameter;

/**
 * Used only for test purposes, custom routines can be created passing a list of
 * {@link Instruction} to the constructor of {@link Routine#Routine(List)}, even
 * if the best way to define a routine is by creating a .rou file that can be
 * parsed through a {@link RoutineLoader}
 */
@Deprecated
public class ConstantSpeedRoutine extends Routine {

	public AbstractEsc esc;

	public ConstantSpeedRoutine() {
		super("Velocità costante 2000", Arrays.asList(TelemetryParameter.values()), new ArrayList<Instruction>());
		List<Instruction> instrs = new ArrayList<>();
		instrs.add(Instruction.newArm());
		instrs.add(Instruction.newStart());
		instrs.add(Instruction.newSetRpm(2000));
		instrs.add(Instruction.newSleep(10000));
		instrs.add(Instruction.newStop());
		instrs.add(Instruction.newDisarm());
		this.instructions = instrs;
	}
}
