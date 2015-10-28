package GUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import esc.ThreadStart;


public class Graphic extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea display;
	private ThreadStart ts;

	public Graphic() {
		try {
			ts = new ThreadStart();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(null, "Are you sure to close this window?", "Really Closing?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
		            ts.esc.stopTelemetry().stop().disarm();
		            System.exit(0);
		        }
		    }
		});
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton = new JButton("Start");
		btnNewButton.addActionListener(this );
		getContentPane().add(btnNewButton, BorderLayout.NORTH);
		
		JPanel middlePanel=new JPanel();
	    middlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Display Area"));

	    // create the middle panel components

	    display = new JTextArea(16, 58);
	    display.setEditable(false);
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	    //Add Textarea in to middle panel
	    middlePanel.add(scroll);
	    
	    getContentPane().add(middlePanel);
	    this.pack();
		this.setVisible(true);
	}
	
	public OutputStream getOutputArea() {
		return new CustomOutputStream(display);
		
	}
	
	public void actionPerformed(ActionEvent arg0) {
		ts.run(this.getOutputArea());
	}

}
