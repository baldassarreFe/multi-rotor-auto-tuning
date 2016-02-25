package routine;

/**
 * An enumeration representing the types of instructions available to an Esc
 *
 */
public enum InstructionType {
	ARM("ARM"), DISARM("DISARM"), SLEEP("SLEEP"), START("START"), STOP("STOP"), SET_RPM("RPM"), ACCELERATE(
			"ACCELERATE"), START_TELEMETRY("TELEMETRY"), STOP_TELEMETRY("STOP TELEMETRY"), DIRECTION("DIRECTION");

	private String commandName;

	private InstructionType(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Returns the {@link InstructionType} corresponding with the string passed
	 * as parameter, or null if none is found.
	 * 
	 * @param type the string to parse (case is ignored)
	 * @return
	 */
	public static InstructionType parse(String type) {
		for (InstructionType t : values()) {
			if (type.trim().equalsIgnoreCase(t.commandName))
				return t;
		}
		return null;
	}

	@Override
	public String toString() {
		return commandName;
	}
}