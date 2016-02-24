package esc;

public enum TelemetryParameter {
	INPUT_MODE("INPUT MODE", String.class), RUN_MODE("RUN MODE", String.class), ESC_STATE("ESC STATE",
			String.class), PERCENT_IDLE("PERCENT IDLE", Double.class), COMM_PERIOD("COMM PERIOD",
					Double.class), BAD_DETECTS("BAD DETECTS", Integer.class), FET_DUTY("FET DUTY", Double.class), RPM(
							"RPM", Double.class), AMPS_AVG("AMPS AVG", Double.class), AMPS_MAX("AMPS MAX",
									Double.class), BAT_VOLTS("BAT VOLTS", Double.class), MOTOR_VOLTS("MOTOR VOLTS",
											Double.class), DISARM_CODE("DISARM CODE",
													Integer.class), CAN_NET_ID("CAN NET ID", Integer.class);

	public static TelemetryParameter parse(String string) {
		for (TelemetryParameter p : TelemetryParameter.values())
			if (string.equals(p.name))
				return p;
		return null;
	}

	public final Class<?> classe;

	public final String name;

	private TelemetryParameter(String name, Class<?> c) {
		this.name = name;
		classe = c;
	}

	@Override
	public String toString() {
		return name + " " + classe.getName();
	}
}