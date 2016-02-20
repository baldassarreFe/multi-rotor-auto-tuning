package view;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import esc.AbstractEsc;

public class CustomClassRenderer extends JLabel implements ListCellRenderer<Class<? extends AbstractEsc>> {
	private static final long serialVersionUID = 2385007885134918354L;

	public CustomClassRenderer() {
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Class<? extends AbstractEsc>> list,
			Class<? extends AbstractEsc> value, int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		this.setText(value.getSimpleName());
		return this;
	}
}
