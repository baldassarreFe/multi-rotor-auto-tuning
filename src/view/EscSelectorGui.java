package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import esc.AbstractEsc;
import esc.EscLoader;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import serialPorts.PortSelector;

public class EscSelectorGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private Object selected = new Object();
	private CommPortIdentifier portId;
	private List<Class<? extends AbstractEsc>> escs;
	private JComboBox<Object> esclist;

	public EscSelectorGui() {
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		final Map<String, CommPortIdentifier> ports = PortSelector.scanPorts();
		final JComboBox<Object> portList = new JComboBox<>(ports.keySet().toArray());
		portList.setVisible(true);
		panel.add(portList);
		
		EscLoader escload = new EscLoader();
		escs = escload.getEscList();
		esclist = new JComboBox<>(escs.toArray());
		esclist.setVisible(true);
		panel.add(esclist);
		
		JButton okButton = new JButton("Select ESC");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				portId = ports.get(portList.getSelectedItem());
				synchronized (selected) {
					selected.notify();
				}
			}
			
		});
		panel.add(okButton);
		
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
			this.dispose();
			return escl.newInstanceOf(escs.get(esclist.getSelectedIndex()), result);
		}
	}	
}
