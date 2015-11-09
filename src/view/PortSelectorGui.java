package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import serialPorts.PortSelector;

public class PortSelectorGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private Object selected = new Object();
	private CommPortIdentifier portId = null;
	
	public PortSelectorGui() {
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(10, 10));
		
		final Map<String, CommPortIdentifier> ports = PortSelector.scanPorts();
		final JComboBox<Object> list = new JComboBox<>(ports.keySet().toArray());
		list.setVisible(true);
		panel.add(list, BorderLayout.NORTH);
		
		JButton okButton = new JButton("Select port");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				portId = ports.get(list.getSelectedItem());
				synchronized (selected) {
					selected.notify();
				}
			}
			
		});
		panel.add(okButton, BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	public SerialPort getConnectedPort() {
		synchronized (selected) {
			try {
				selected.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}		
		SerialPort result = PortSelector.connect(portId);
		if (result==null){
			JOptionPane.showMessageDialog(this, "NON SI PUò");
			return getConnectedPort();
		} else {
			this.dispose();
			return result;
		}
	}	
}
