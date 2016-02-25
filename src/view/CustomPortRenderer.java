package view;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import gnu.io.CommPortIdentifier;

/**
 * Class for displaying a {@link JComboBox} of {@link CommPortIdentifier} using their method
 * {@link CommPortIdentifier#getName()} to get the string to display inside a {@link JLabel}
 *
 */
public class CustomPortRenderer extends JLabel implements ListCellRenderer<CommPortIdentifier> {
	private static final long serialVersionUID = 2385007885134918354L;

	public CustomPortRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends CommPortIdentifier> list, CommPortIdentifier value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		// value non sarà mai null, tranne se la lista non contiene elementi
		// in questo caso Swing fa il renderer di una cella vuota per la quale
		// passa al renderer value=null
		this.setText(value != null ? value.getName() : "No ports available");
		return this;
	}

}
