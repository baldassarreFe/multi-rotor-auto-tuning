package viewtest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

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
	private List<Class<? extends AbstractEsc>> escs;
	private JComboBox<Class<? extends AbstractEsc>> esclist;
	private JComboBox<CommPortIdentifier> portList;
	private List<CommPortIdentifier> ports;
	private JComboBox<Routine> routines;
	private Controller controller;

	public LeftPanel(Controller cont) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.controller = cont;
		
		ports = PortSelector.scanPorts();
		portList = new JComboBox<>(ports.toArray(new CommPortIdentifier[] {}));
		portList.setRenderer(new CustomPortRenderer());
		this.add(portList);
		
		escs = EscFactory.getEscsList();
		esclist = new JComboBox<>();
		for (Class<? extends AbstractEsc> c : escs) {
			esclist.addItem(c);
		}
		esclist.setRenderer(new CustomClassRenderer());
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
		portId = (CommPortIdentifier) portList.getSelectedItem();
		if (portId == null) {
			JOptionPane.showMessageDialog(this, "Porta non selezionata");
			return null;
		}
		SerialPort result = PortSelector.connect(portId);
		if (result == null) {
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi alla porta " + portId);
			return null;
		} 
		@SuppressWarnings("unchecked")
		AbstractEsc esc = EscFactory.newInstanceOf((Class<? extends AbstractEsc>) esclist.getSelectedItem(), result); 
		if (esc == null) {
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi all'esc alla porta " + portId);
			return null;
		}
		return esc;
	}
}
