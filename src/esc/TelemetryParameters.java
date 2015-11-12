package esc;

import java.util.HashMap;
import java.util.Map;

public enum TelemetryParameters {
	ARM, DISARM, START, STOP, SET_RPM, ACCELERATE, START_TELEMETRY, STOP_TELEMETRY;

	// questa map name-value è condivisa tra i singoli comandi (ARM ha la sua, START ha la sua ecc)
	public Map<String, Object> parameters = new HashMap<>();
}