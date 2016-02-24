package routine;

import java.util.HashMap;
import java.util.Map;

public class Instruction {

	public static final Instruction ARM = new Instruction(InstructionType.ARM, null);
	public static final Instruction DISARM = new Instruction(InstructionType.DISARM, null);
	public static final Instruction START = new Instruction(InstructionType.START, null);
	public static final Instruction STOP = new Instruction(InstructionType.STOP, null);
	public static final Instruction STOP_TELEMETRY = new Instruction(InstructionType.STOP_TELEMETRY, null);

	public static final Instruction newAcceleration(int from, int to, double pace) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("from", from);
		map.put("to", to);
		map.put("pace", pace);
		return new Instruction(InstructionType.ACCELERATE, map);
	}

	public static final Instruction newDirection(String direction) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("direction", direction);
		return new Instruction(InstructionType.DIRECTION, map);
	}

	public static final Instruction newSetRpm(int rpm) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("rpm", rpm);
		return new Instruction(InstructionType.SET_RPM, map);
	}

	public static final Instruction newSetTelemetry(int frequency) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("frequency", frequency);
		return new Instruction(InstructionType.START_TELEMETRY, map);
	}

	public static final Instruction newSleep(long millis) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("millis", millis);
		return new Instruction(InstructionType.SLEEP, map);
	}

	public final InstructionType type;
	public final Map<String, Object> parameters;

	public Instruction(InstructionType type, Map<String, Object> parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return type.toString() + " " + (parameters == null ? "" : parameters);
	}
}
