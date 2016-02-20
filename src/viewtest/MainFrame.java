package viewtest;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import controller.Controller;

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
		this.setSize(600, 300);
		this.setVisible(true);
	}
}
