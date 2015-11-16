package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.Controller;
import esc.Routine;
import esc.RoutineLoader;

public class MainView extends JFrame implements ActionListener {
	private Controller controller;
	private JComboBox<Routine> routines; 
	private JButton start;
	
	public MainView(final Controller controller) {
		this.controller = controller;
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				controller.exitEsc();
				System.exit(0);
			}
		});
	}

	private static final long serialVersionUID = 1L;

	public void initGraphic() {
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		RoutineLoader.loadFrom(new File("routines"));
		routines = new JComboBox<>(RoutineLoader.getRoutines().toArray(new Routine[10]));
		panel.add(routines, BorderLayout.NORTH);
		
		start = new JButton("START");
		start.addActionListener(this);
		panel.add(start, BorderLayout.SOUTH);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (start == e.getSource()) {
			controller.startRoutine((Routine) routines.getSelectedItem());
		}
	}
}
