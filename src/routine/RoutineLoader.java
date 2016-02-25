package routine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import esc.FileFormatException;
import esc.TelemetryParameter;

public class RoutineLoader {
	private static List<Routine> list = new ArrayList<>();

	public static List<Routine> getRoutines() {
		list.add(Routine.exampleRoutine);
		list.add(Routine.accelerateRoutine);
		list.add(Routine.stopAll);
		return list;
	}

	/**
	 * Loads all the routines files (.rou) from the directories specified as
	 * parameters, the routines that were parsed correctly can be found with
	 * {@link #getRoutines()}. Routines are considerd equal and hence are not
	 * duplicated in the list on the base of the {@link Routine#equals()}
	 * method.
	 * 
	 * @param directories
	 */
	public static void loadFrom(File... directories) {
		if (directories == null)
			throw new IllegalArgumentException();
		for (File d : directories)
			if (d.isDirectory() && d.canRead())
				for (File f : d.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".rou");
					}
				})) {
					try {
						Routine r = parseFile(f);
						if (!list.contains(r))
							list.add(r);
					} catch (IOException | FileFormatException e) {
						e.printStackTrace();
					}
				}
	}

	/**
	 * Parses a .rou file in the following way.<br>
	 * The first line must be the name of the routine.<br>
	 * The second line must contain a comma separated list of telemetry
	 * parameters written as they are in {@link TelemetryParameter}<br>
	 * The following lines must contain valid instructions for the Esc, with
	 * their parameters preceeded by ':' and separated by spaces if needed. <br>
	 * Empty lines or lines starting with '#' are skipped.<br>
	 * <br>
	 * An example of a valid routine file is the following:<br>
	 * <br>
	 * <code>
	 * Rapid acceleration from 2000 to 6000 rpm<br>
	 * RPM, AMPS AVG, MOTOR VOLTS<br>
	 * arm<br>
	 * start<br>
	 * rpm: 2000<br>
	 * sleep: 10000<br>
	 * telemetry: 50<br>
	 * sleep: 1000<br>
	 *<br>
	 * accelerate: 2000 6000 1200<br>
	 * accelerate 6000 1000 -400<br>
	 * # It is impossible for the AutoquadEsc32 to decelerate faster than -400 rpm/s
	 * <br>
	 * sleep: 5000<br>
	 * stop telemetry<br>
	 * stop<br>
	 * disarm<br>
	 * </code>
	 * 
	 * @param file
	 *            the file to parse
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Routine parseFile(File file) throws IOException, FileFormatException {
		assert file != null && file.getName().endsWith(".rou");

		BufferedReader r = new BufferedReader(new FileReader(file));
		String name = r.readLine();
		if (name == null) {
			r.close();
			throw new FileFormatException("Non c'è nome nel file" + file.getName());
		}

		ArrayList<TelemetryParameter> params = new ArrayList<>();
		String paramsLine = r.readLine();
		if (paramsLine == null) {
			r.close();
			throw new FileFormatException("Non ci sono parametri nel file" + file.getName());
		}
		StringTokenizer st = new StringTokenizer(paramsLine, ",");
		while (st.hasMoreTokens())
			try {
				String paramName = st.nextToken().trim().toUpperCase();
				TelemetryParameter p = TelemetryParameter.parse(paramName);
				params.add(p);
			} catch (IllegalArgumentException e) {
				r.close();
				throw new FileFormatException("Errore nei parametri nel file" + file.getName(), e);
			}

		List<Instruction> instructions = new ArrayList<>();
		String line;
		while ((line = r.readLine()) != null) {
			if (!line.isEmpty() && !line.startsWith("#")) {
				st = new StringTokenizer(line);
				String type = st.nextToken(":");
				InstructionType t = InstructionType.parse(type);
				if (t == null) {
					r.close();
					throw new FileFormatException("Istruzione non riconosciuta: " + line);
				}

				// some instructions need to parse some parameters
				try {
					switch (t) {
					case SET_RPM:
						int rpm = Integer.parseInt(st.nextToken(": "));
						instructions.add(Instruction.newSetRpm(rpm));
						break;
					case SLEEP:
						int millis = Integer.parseInt(st.nextToken(": "));
						instructions.add(Instruction.newSleep(millis));
						break;
					case START_TELEMETRY:
						int frequency = Integer.parseInt(st.nextToken(": "));
						instructions.add(Instruction.newSetTelemetry(frequency));
						break;
					case ACCELERATE:
						int from = Integer.parseInt(st.nextToken(": "));
						int to = Integer.parseInt(st.nextToken(": "));
						double pace = Double.parseDouble(st.nextToken(": "));
						instructions.add(Instruction.newAcceleration(from, to, pace));
						break;
					case DIRECTION:
						String direction = st.nextToken(": ");
						instructions.add(Instruction.newDirection(direction));
						break;
					case ARM:
						instructions.add(Instruction.newArm());
						break;
					case STOP:
						instructions.add(Instruction.newStop());
						break;
					case STOP_TELEMETRY:
						instructions.add(Instruction.newStopTelemetry());
						break;
					case DISARM:
						instructions.add(Instruction.newDisarm());
						break;
					case START:
						instructions.add(Instruction.newStart());
						break;
					}
				} catch (NumberFormatException e) {
					r.close();
					throw new FileFormatException("Errore alla linea: " + line, e);
				}
			}
		}
		r.close();
		return new Routine(name, params, instructions);
	}
}
