package esc;

public enum InstructionType {
	ARM("ARM"), DISARM("DISARM"), SLEEP("SLEEP"), START("START"), STOP("STOP"), SET_RPM("SET RPM"), ACCELERATE("ACCELERATE"), START_TELEMETRY("TELEMETRY"), STOP_TELEMETRY("TELEMETRY 0");
	
	private String commandName;
	private InstructionType(String commandName){
		this.commandName=commandName;
	}
	public String toString(){
		return commandName;
	}
}