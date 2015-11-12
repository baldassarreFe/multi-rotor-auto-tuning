package esc;

import java.util.Map;

public class Instruction {
	
	public static final Instruction ARM = new Instruction(InstructionType.ARM, null);
	public static final Instruction DISARM = new Instruction(InstructionType.DISARM, null);
	public static final Instruction START = new Instruction(InstructionType.START, null);
	public static final Instruction STOP = new Instruction(InstructionType.STOP, null);
	public static final Instruction STOP_TELEMETRY = new Instruction(InstructionType.STOP_TELEMETRY, null);
	
	public final InstructionType type;
	public final Map<String, Object> parameters;
	
	public Instruction(InstructionType type, Map<String, Object> parameters){
		this.type = type;
		this.parameters = parameters;
	}
}