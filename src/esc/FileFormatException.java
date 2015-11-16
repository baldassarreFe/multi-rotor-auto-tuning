package esc;

public class FileFormatException extends Exception {

	public FileFormatException(String string, Throwable e) {
		super(string, e);
	}

	public FileFormatException(String string) {
		super(string);
	}

}
