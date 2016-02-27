package esc;

/**
 * Parameters that can be read from the telemetry of a generic esc (names
 * inspired from the model AutoquadEsc32). Each parameter has associated a
 * string that identifies that parameter in the telemetry output of the ESC and
 * a class type that determines the kind of value associated with the parameter.
 */
public enum TelemetryParameter {
	// TODO non � corretto dal punto di vista della astrazione che ai telemetry
	// parameter sia associata la stringa specifica dell'AutoQuadEsc32. Sarebbe
	// pi� corretto che ogni implementazione di un ESC mantenga al suo interno
	// una tabella di conversione tra la sua telemetria interna e i telemetry
	// parameter astratti.
	// Tuttavia, pur lasciando qui le stringhe, ogni implementazione di un
	// esc pu� decidere di ignorarle e gestire la telemetria a modo suo.
	// 
	// Volendo cambiare questa implementazione bisogna anche cambiare il metodo
	// parse() basato su queste stringhe che viene utilizzato nel parsing delle
	// routines dai file .rou. L'ideale sarebbe spostare le stringhe specifiche
	// dell'AutoQuadEsc32 all'interno del suo ReaderThread, lasciando che le
	// altre implementazioni di AbstractEsc facciano le loro scelte a riguardo,
	// e poi associare ad ogni TelemetryParameter una stringa generica (e non
	// più specifica dell'AutoQuadEsc32) da utlizzare nei file .rou

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