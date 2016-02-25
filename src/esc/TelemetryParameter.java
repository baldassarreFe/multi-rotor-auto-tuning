package esc;

/**
 * Parameters that can be read from the telemetry of a generic esc (names
 * inspired from the modele AutoquadEsc32). Each parameter has associated a
 * string that identifies that parameter in the telemetry output of the ESC and
 * a class type that determines the kind of value associated with the parameter.
 */
public enum TelemetryParameter {
	INPUT_MODE("INPUT MODE", String.class),

	RUN_MODE("RUN MODE", String.class),

	ESC_STATE("ESC STATE", String.class),

	PERCENT_IDLE("PERCENT IDLE", Double.class),

	COMM_PERIOD("COMM PERIOD", Double.class),

	BAD_DETECTS("BAD DETECTS", Integer.class),

	FET_DUTY("FET DUTY", Double.class),

	RPM("RPM", Double.class),

	AMPS_AVG("AMPS AVG", Double.class),

	AMPS_MAX("AMPS MAX", Double.class),

	BAT_VOLTS("BAT VOLTS", Double.class),

	MOTOR_VOLTS("MOTOR VOLTS", Double.class),

	DISARM_CODE("DISARM CODE", Integer.class),

	CAN_NET_ID("CAN NET ID", Integer.class);

	/**
	 * Gives the {@link TelemetryParameter} associated to the string passed as
	 * parameter, or null if none corresponds
	 * 
	 * @param string
	 *            the string to parse
	 * @return
	 */
	public static TelemetryParameter parse(String string) {
		for (TelemetryParameter p : TelemetryParameter.values())
			if (string.equals(p.name))
				return p;
		return null;
	}

	public final Class<?> valueClass;

	public final String name;

	private TelemetryParameter(String name, Class<?> valueClass) {
		this.name = name;
		this.valueClass = valueClass;
	}

	@Override
	public String toString() {
		return name + " " + valueClass.getName();
	}
}