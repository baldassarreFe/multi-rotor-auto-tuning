package GUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
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
	private JButton btnStart;
	private JButton btnStop;

	public Graphic() {		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout(0, 0));
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(this );
		getContentPane().add(btnStart, BorderLayout.NORTH);
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(this );
		getContentPane().add(btnStop, BorderLayout.SOUTH);
		
		JPanel middlePanel=new JPanel();
	    middlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Display Area"));

	    // create the middle panel components

	    display = new JTextArea(16, 58);
	    display.setEditable(false);
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	    try {
			ts = new ThreadStart(new CustomOutputStream(display));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //Add Textarea in to middle panel
	    middlePanel.add(scroll);
	    
	    getContentPane().add(middlePanel);
	    this.pack();
		this.setVisible(true);
	}
		
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == btnStart)
			ts.start();
		else {
			ts.esc.stopTelemetry().stop().disarm();
		}
			
	}

}
