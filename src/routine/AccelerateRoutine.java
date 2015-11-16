package routine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import esc.AbstractEsc;
import esc.TelemetryParameters;

/**used only for test purposes, custom routines can be created passing
 *  a list of {@link Instruction} to {@link Routine#Routine(List)} 
*/
public class AccelerateRoutine extends Routine {

	public AbstractEsc esc;
	
	public AccelerateRoutine() {
		super(null,Arrays.asList(TelemetryParameters.values()),null);
		List<Instruction> instrs = new ArrayList<>();
		instrs.add(Instruction.ARM);
		instrs.add(Instruction.START);
		instrs.add(Instruction.newSetTelemetry(10));
		instrs.add(Instruction.newAcceleration(1000, 2000, 50));
		instrs.add(Instruction.newSleep(3000));
		instrs.add(Instruction.STOP);
		instrs.add(Instruction.DISARM);
		this.instructions = instrs;
	}
	
	public String toString(){
		return "Accelera 1000->2000 a 50 rpm/s";
	}
}
