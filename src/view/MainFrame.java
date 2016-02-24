package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import controller.Controller;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String DATE = "March 2016";
	private static final String VERSION = "1.0";
	protected static final String ABOUT = "Developed by Federico Baldassarre e Federico Venturini\n"
			+ "CASY (Center for Research on Complex Automated Systems)"
			+ "\nAlma Mater Studiorum - Università di Bologna"
			+ "\nVersion: "
			+ VERSION + " " + DATE;
	private Controller controller;

	public MainFrame(final Controller controller) {
		this.controller = controller;
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.stopRoutineAndDisconnectEsc();
				System.exit(0);
			}
		});
	}

	public void initGraphic() {
		this.setLayout(new GridLayout(1, 2));
		this.setTitle("Rotor Model Identification System");
		this.add(new LeftPanel(controller));
		this.add(new RightPanel(controller));
		this.setSize(600, 300);

		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(getParent(), ABOUT);
			}
		});

		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		help.add(about);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(help);
		
		this.setJMenuBar(menuBar);

		this.setVisible(true);
	}
}
