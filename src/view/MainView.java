package view;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.Controller;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JButton;

public class MainView extends JFrame implements ActionListener {
	private Controller controller;
	private JComboBox<Object> routines;
	private JButton start;
	
	public MainView(Controller controller) {
		this.controller = controller;
	}

	private static final long serialVersionUID = 1L;

	public void initGraphic() {
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JComboBox<Object> routines = new JComboBox<Object>(RoutineLoader.getRoutines.toArray());
		panel.add(routines, BorderLayout.NORTH);
		
		JButton start = new JButton("START");
		start.addActionListener(this);
		panel.add(start, BorderLayout.SOUTH);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (start == e.getSource()) {
			controller.startRoutine(routines.getSelectedItem());
		}
	}
}
