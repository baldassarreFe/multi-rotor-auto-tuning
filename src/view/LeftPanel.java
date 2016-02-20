package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
	private JComboBox<Class<? extends AbstractEsc>> escList;
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
		portList.setMaximumSize(new Dimension(400,(int)portList.getPreferredSize().getHeight()+10));
		this.add(portList);
		
		this.add(Box.createRigidArea(new Dimension(0,20)));

		escs = EscFactory.getEscsList();
		escList = new JComboBox<>();
		for (Class<? extends AbstractEsc> c : escs) {
			escList.addItem(c);
		}
		escList.setRenderer(new CustomClassRenderer());
		escList.setMaximumSize(new Dimension(400,(int)escList.getPreferredSize().getHeight()+10));
		this.add(escList);
		
		this.add(Box.createRigidArea(new Dimension(0,20)));


		RoutineLoader.loadFrom(new File("routines"));
		routines = new JComboBox<>(RoutineLoader.getRoutines().toArray(new Routine[] {}));
		routines.setMaximumSize(new Dimension(400,(int)routines.getPreferredSize().getHeight()+10));
		this.add(routines);

		this.add(Box.createRigidArea(new Dimension(0,20)));

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
		
		start.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(start);
		this.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
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
		AbstractEsc esc = EscFactory.newInstanceOf((Class<? extends AbstractEsc>) escList.getSelectedItem(), result);
		if (esc == null) {
			JOptionPane.showMessageDialog(this, "Impossibile collegarsi all'esc alla porta " + portId);
			return null;
		}
		return esc;
	}
}
