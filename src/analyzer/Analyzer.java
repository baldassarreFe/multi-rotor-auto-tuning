package analyzer;

import java.io.File;

public abstract class Analyzer {
	private File logFile;

	public Analyzer(File logFile){
		this.logFile = logFile;
	}
}
