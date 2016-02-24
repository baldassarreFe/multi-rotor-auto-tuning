package esc;

public class FileFormatException extends Exception {
	private static final long serialVersionUID = 2406667586993417691L;

	public FileFormatException(String string) {
		super(string);
	}

	public FileFormatException(String string, Throwable e) {
		super(string, e);
	}

}
