package esc;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.io.SerialPort;

/**used only for test purposes, custom routines can be created passing
 *  a list of {@link Instruction} to {@link Routine#Routine(List)} 
*/
public class AccelerateRoutine extends Routine {

	public AbstractEsc esc;
	
	public AccelerateRoutine() {
		super(null);
		List<Instruction> instrs = new ArrayList<>();
		instrs.add(Instruction.ARM);
		instrs.add(Instruction.START);
		instrs.add(Instruction.newAcceleration(1000, 2000, 50));
		instrs.add(Instruction.newSleep(3000));
		instrs.add(Instruction.STOP);
		instrs.add(Instruction.DISARM);
		this.instructions = instrs;
	}
	
	public String toString(){
		return "Routine base";
	}
}
