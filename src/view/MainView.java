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
import javax.swing.JTextArea;

import controller.Controller;
import routine.Routine;
import routine.RoutineLoader;

public class MainView extends JFrame implements ActionListener {
	private Controller controller;
	private JComboBox<Routine> routines; 
	private JButton start;
	private JTextArea textarea;
	
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
		this.setLayout(new BorderLayout());
		
		RoutineLoader.loadFrom(new File("routines"));
		routines = new JComboBox<>(RoutineLoader.getRoutines().toArray(new Routine[10]));
		this.add(routines, BorderLayout.NORTH);
		
		start = new JButton("START");
		start.addActionListener(this);
		this.add(start, BorderLayout.CENTER);
		
		textarea = new JTextArea();
		textarea.setVisible(true);
		JScrollPane scrollPane = new JScrollPane(textarea);
		scrollPane.setPreferredSize(new Dimension(640, 480));
		this.add(scrollPane, BorderLayout.SOUTH);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (start == e.getSource()) {
			controller.startRoutine((Routine) routines.getSelectedItem());
			(new ReaderThread(textarea, controller.getInput())).start();
		}
	}
	
	private class ReaderThread extends Thread {
		private JTextArea area;
		private InputStream in;
		
		public ReaderThread(JTextArea area, InputStream in) {
			this.area=area;
			this.in=in;
		}
		
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String buf;
			try {
				while ((buf=reader.readLine())!=null) {
					area.append(buf+"\n");
					area.setCaretPosition(area.getDocument().getLength());
					area.update(area.getGraphics());				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
