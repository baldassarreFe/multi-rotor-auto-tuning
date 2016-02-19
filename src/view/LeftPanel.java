package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Controller;
import esc.AbstractEsc;
import esc.EscFactory;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import routine.Routine;
import routine.RoutineLoader;
import serialPorts.PortSelector;

public class LeftPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private CommPortIdentifier portId;
	private Map<String, Class<? extends AbstractEsc>> escs;
	private JComboBox<String> esclist;
	private JComboBox<String> portList;
	private Map<String, CommPortIdentifier> ports;
	private JComboBox<Routine> routines;
	private Controller controller;

	public LeftPanel(Controller cont) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.controller = cont;
		
		ports = PortSelector.scanPorts();
		portList = new JComboBox<>(ports.keySet().toArray(new String[] {}));
		this.add(portList);
		
		escs = EscFactory.getEscsMap();
		esclist = new JComboBox<>(escs.keySet().toArray(new String[] {}));
		this.add(esclist);
		
		RoutineLoader.loadFrom(new File("routines"));
		routines = new JComboBox<>(RoutineLoader.getRoutines().toArray(new Routine[] {}));
		this.add(routines);
		
		JButton start = new JButton("START");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Routine r = (Routine) routines.getSelectedItem();
				AbstractEsc esc = getConnectedEsc();
				if (esc == null)
					return;
				controller.setEsc(esc);
				controller.startRoutine(r);			
			}
		});
		this.add(start);
	}

	public AbstractEsc getConnectedEsc() {
		portId = ports.get(portList.getSelectedItem());
		if (portId == null) {
			JOptionPane.showMessageDialog(this, "Porta non selezionata");
			return null;
		}
		SerialPort result = PortSelector.connect(portId);
		if (result == null) {
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi alla porta " + portId);
			return null;
		} 
		AbstractEsc esc = EscFactory.newInstanceOf(escs.get(esclist.getSelectedItem()), result); 
		if (esc == null) {
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi all'esc alla porta " + portId);
			return null;
		}
		return esc;
	}
}
