package routine;

public enum InstructionType {
	ARM("ARM"), DISARM("DISARM"), SLEEP("SLEEP"), START("START"), STOP("STOP"), SET_RPM("SET RPM"), ACCELERATE(
			"ACCELERATE"), START_TELEMETRY("TELEMETRY"), STOP_TELEMETRY("TELEMETRY 0"), DIRECTION("DIRECTION");

	private String commandName;

	private InstructionType(String commandName) {
		this.commandName = commandName;
	}

	@Override
	public String toString() {
		return commandName;
	}
}