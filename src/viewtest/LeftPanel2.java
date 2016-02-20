package viewtest;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import java.awt.GridLayout;

public class LeftPanel2 extends JPanel{
	public LeftPanel2() {
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JComboBox comboBox = new JComboBox();
		add(comboBox);
	}

}
