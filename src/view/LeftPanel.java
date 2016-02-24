package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Controller;
import esc.AbstractEsc;
import esc.EscFactory;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
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
		controller = cont;

		ports = PortSelector.scanPorts();
		portList = new JComboBox<>(ports.toArray(new CommPortIdentifier[] {}));
		portList.setRenderer(new CustomPortRenderer());
		portList.setMaximumSize(new Dimension(400, (int) portList.getPreferredSize().getHeight() + 10));
		this.add(portList);

		this.add(Box.createRigidArea(new Dimension(0, 20)));

		escs = EscFactory.getEscsList();
		escList = new JComboBox<>();
		for (Class<? extends AbstractEsc> c : escs)
			escList.addItem(c);
		escList.setRenderer(new CustomClassRenderer());
		escList.setMaximumSize(new Dimension(400, (int) escList.getPreferredSize().getHeight() + 10));
		this.add(escList);

		this.add(Box.createRigidArea(new Dimension(0, 20)));

		RoutineLoader.loadFrom(new File("routines"));
		routines = new JComboBox<>(RoutineLoader.getRoutines().toArray(new Routine[] {}));
		routines.setMaximumSize(new Dimension(400, (int) routines.getPreferredSize().getHeight() + 10));
		this.add(routines);

		this.add(Box.createRigidArea(new Dimension(0, 20)));

		JButton start = new JButton("START");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Routine r = (Routine) routines.getSelectedItem();
				AbstractEsc esc = getConnectedEsc();
				if (esc == null)
					return;
				controller.startRoutine(r,esc);
			}
		});

		start.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(start);
		this.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
	}

	@SuppressWarnings("unchecked")
	public AbstractEsc getConnectedEsc() {
		portId = (CommPortIdentifier) portList.getSelectedItem();
		if (portId == null) {
			JOptionPane.showMessageDialog(this, "Porta non selezionata");
			return null;
		}

		SerialPort result;
		try {
			result = PortSelector.connect(portId);
		} catch (PortInUseException e) {
			JOptionPane.showMessageDialog(getParent(), "Failed to open " + portId.getName() + ": port is in use.");
			return null;
		} catch (UnsupportedCommOperationException e) {
			JOptionPane.showMessageDialog(getParent(),
					"Failed to set serial port parameters on " + portId.getName() + ": " + e.getMessage());
			return null;
		}

		AbstractEsc esc = null;
		try {
			esc = EscFactory.newInstanceOf((Class<? extends AbstractEsc>) escList.getSelectedItem(), result);
		} catch (InvocationTargetException ite) {
			if (ite.getCause() instanceof IOException) {
				ite.getCause().printStackTrace();
				JOptionPane.showMessageDialog(getParent(), "Problema nella comunicazione con la porta");
			} else { // eccezione generica generata dai costruttori delle future
						// sottoclassi
				ite.getCause().printStackTrace();
				JOptionPane.showMessageDialog(getParent(), "Problema: " + ite.getCause().getMessage());
			}
			return null;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(getParent(), "Problema: " + e1.getMessage());
			return null;
		}

		return esc;
	}
}
