package GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import esc.ThreadStart;

import javax.swing.JTextArea;
import java.awt.Rectangle;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class Graphic extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTextArea display;

	public Graphic() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
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
		(new ThreadStart()).run(this.getOutputArea());
	}

}
