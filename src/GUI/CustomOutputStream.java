package GUI;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

/**
 *  * This class extends from OutputStream to redirect output to a JTextArrea
 */

public class CustomOutputStream extends OutputStream {
	private JTextArea textArea;


	public CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
	}


	public void write(int arg0) throws IOException {
		textArea.append(String.valueOf((char)arg0));
		textArea.setCaretPosition(textArea.getDocument().getLength());
		textArea.update(textArea.getGraphics());
	}

}
