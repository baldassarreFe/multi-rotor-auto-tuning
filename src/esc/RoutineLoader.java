package esc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RoutineLoader {
	private static List<Routine> list = new ArrayList<>();

	public static void loadFrom(File... directories) {
		for (File d : directories) {
			if (d.isDirectory() && d.canRead()) {
				for (File f : d.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".rou");
					}
				})) {
					list.add(parseFile(f));
				}
			}
		}
	}

	private static Routine parseFile(File f) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));

			String name = r.readLine();
			if (name == null)
				throw new FileFormatException("Non c'è nome");
			List<TelemetryParameters> params = new ArrayList<>();

			String paramsLine = r.readLine();
			if (paramsLine == null)
				throw new FileFormatException("Non ci sono parametri");
			StringTokenizer st = new StringTokenizer(paramsLine, ",");
			while (st.hasMoreTokens()) {
				try {
					String paramName = st.nextToken().trim().toUpperCase();
					TelemetryParameters p = TelemetryParameters.valueOf(paramName);
					params.add(p);
				} catch (IllegalArgumentException e) {
					throw new FileFormatException("Errore nei parametri", e);
				}
			}

			List<Instruction> instructions = new ArrayList<>();
			String command;
			while ((command = r.readLine()) != null) {
				for (InstructionType t : InstructionType.values()) {
					if (command.trim().toUpperCase().startsWith(t.toString())) {
						switch (t) {
						case SET_RPM:
							st = new StringTokenizer(command);
							try {
								st.nextToken();
								st.nextToken();
								int rpm = Integer.parseInt(st.nextToken());
								instructions.add(Instruction.newSetRpm(rpm));
							} catch (NumberFormatException e) {
								throw new FileFormatException("Errore alla linea: " + command, e);
							}
							break;
						case STOP_TELEMETRY:
							instructions.add(Instruction.STOP_TELEMETRY);
						case START_TELEMETRY:
							st = new StringTokenizer(command);
							try {
								st.nextToken();
								int frequency = Integer.parseInt(st.nextToken());
								instructions.add(Instruction.newSetTelemetry(frequency));
							} catch (NumberFormatException e) {
								throw new FileFormatException("Errore alla linea: " + command, e);
							}
							break;
						case ACCELERATE:
							st = new StringTokenizer(command);
							try {
								st.nextToken();
								int from = Integer.parseInt(st.nextToken());
								int to = Integer.parseInt(st.nextToken());
								double pace = Double.parseDouble(st.nextToken());
								instructions.add(Instruction.newAcceleration(from, to, pace));
							} catch (NumberFormatException e) {
								throw new FileFormatException("Errore alla linea: " + command, e);
							}
							break;
						default:
							instructions.add(new Instruction(t, null));
						}
					}
				}
			}
			return new Routine(name, params, instructions);
		} catch (

		FileNotFoundException e)

		{
			e.printStackTrace();
			return null;
		} catch (

		FileFormatException e)

		{
			e.printStackTrace();
			return null;
		} catch (

		IOException e)

		{
			e.printStackTrace();
			return null;
		}

	}

	public static List<Routine> getRoutines() {
		list.add(new AccelerateRoutine());
		list.add(new ExampleRoutine());
		return list;
	}
}
