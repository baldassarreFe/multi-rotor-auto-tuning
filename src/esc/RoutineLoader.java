package esc;

import java.util.ArrayList;
import java.util.List;

public class RoutineLoader {
	private static List<Class> list = new ArrayList<>();
	public static List<Class> getRoutines(){
		list.add(AccelerateRoutine.class);
		list.add(ExampleRoutine.class);		
		return list;
	}
}
