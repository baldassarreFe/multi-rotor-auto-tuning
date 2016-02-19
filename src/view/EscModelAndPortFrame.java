package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import esc.AbstractEsc;
import esc.EscLoader;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import serialPorts.PortSelector;

public class EscModelAndPortFrame extends JPanel {
	private static final long serialVersionUID = 1L;

	private Object selected = new Object();
	private CommPortIdentifier portId;
	private List<Class<? extends AbstractEsc>> escs;
	private JComboBox<Object> esclist;

	public EscModelAndPortFrame() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		final Map<String, CommPortIdentifier> ports = PortSelector.scanPorts();
		final JComboBox<Object> portList = new JComboBox<>(ports.keySet().toArray());
		portList.setVisible(true);
		this.add(portList);
		
		EscLoader escload = new EscLoader();
		escs = escload.getEscList();
		esclist = new JComboBox<>(escs.toArray());
		esclist.setVisible(true);
		this.add(esclist);
		
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
		this.add(okButton);
		this.setVisible(true);
	}

	public AbstractEsc getConnectedEsc() {
		synchronized (selected) {
			try {
				selected.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}		
		SerialPort result = PortSelector.connect(portId);
		if (result==null){
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi alla porta " + portId);
			return getConnectedEsc();
		} else {
			EscLoader escl = new EscLoader();
			return escl.newInstanceOf(escs.get(esclist.getSelectedIndex()), result);
		}
	}	
}
