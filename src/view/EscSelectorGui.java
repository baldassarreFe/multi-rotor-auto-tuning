package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import esc.EscLoader;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import serialPorts.PortSelector;

public class EscSelectorGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private Object selected = new Object();
	private CommPortIdentifier portId = null;
	private List<Class> escs= null;
	private JComboBox<Object> esclist = null;

	
	public EscSelectorGui() {
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(10, 10));
		
		final Map<String, CommPortIdentifier> ports = PortSelector.scanPorts();
		final JComboBox<Object> portList = new JComboBox<>(ports.keySet().toArray());
		portList.setVisible(true);
		panel.add(portList, BorderLayout.NORTH);
		
		EscLoader escload = new EscLoader();
		escload.getEscList();
		esclist = new JComboBox<>(escs.toArray());
		esclist.setVisible(true);
		panel.add(esclist, BorderLayout.NORTH);
		
		JButton okButton = new JButton("Select port");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				portId = ports.get(portList.getSelectedItem());
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

	public AbstractEsc getConnectedEsc() {
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
			return getConnectedEsc();
		} else {
			EscLoader escl = new EscLoader();
			return escl.newInstanceOf(escs.get(esclist.getSelectedIndex()), result);
			this.dispose();
		}
	}	
}
