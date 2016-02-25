package esc;

/**
 * Thrown when the parsing of a formatted file has resulted in an error
 */
public class FileFormatException extends Exception {
	private static final long serialVersionUID = 2406667586993417691L;

	public FileFormatException(String string) {
		super(string);
	}

	public FileFormatException(String string, Throwable e) {
		super(string, e);
	}

}
