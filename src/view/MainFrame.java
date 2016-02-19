package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import controller.Controller;
import routine.Routine;
import routine.RoutineLoader;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private Controller controller;
	
	public MainFrame(final Controller controller) {
		this.controller = controller;
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				controller.disconnectEsc();
				System.exit(0);
			}
		});
	}

	public void initGraphic() {
		this.setLayout(new BorderLayout());
		this.add(new LeftPanel(controller), BorderLayout.WEST);
		this.add(new RightPanel(controller), BorderLayout.EAST);
		this.pack();
		this.setVisible(true);
	}
}
