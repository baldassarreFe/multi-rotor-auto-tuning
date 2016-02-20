package viewtest;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CustomClassRenderer extends JLabel implements ListCellRenderer<Class<?>> {
	private static final long serialVersionUID = 2385007885134918354L;

	public CustomClassRenderer() {
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Class<?>> list, Class<?> value, int index,
			boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		// value non sarà mai null, tranne se la lista non contiene elementi
		// in questo caso Swing fa il renderer di una cella vuota per la quale passa al renderer value=null
		this.setText(value != null ? value.getSimpleName() : "No classes available");
		return this;
	}

}
