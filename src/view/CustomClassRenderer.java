package view;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Class for displaying a {@link JComboBox} of {@link Class} using their method
 * {@link Class#getSimpleName()} to get the string to display inside a
 * {@link JLabel}
 *
 */
public class CustomClassRenderer extends JLabel implements
		ListCellRenderer<Class<?>> {
	private static final long serialVersionUID = 2385007885134918354L;

	public CustomClassRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Class<?>> list, Class<?> value, int index,
			boolean isSelected, boolean cellHasFocus) {
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
		setText(value != null ? value.getSimpleName() : "No classes available");
		return this;
	}

}
