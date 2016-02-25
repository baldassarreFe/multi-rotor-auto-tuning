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

/**
 * In LeftPanel are displayed all the options concerning routines and escs,
 * using three combo box and a button. <br>
 * The first combo box allows you to select which serial port you need to use,
 * showing "No ports available" if none available.<br>
 * The second combo box allows you to choose which implementation of an esc you
 * want to use.<br>
 * The third one lets you select the routine you want to launch on the selected
 * esc and associated motor.<br>
 * The button START launch the selected routine.
 */
public class LeftPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private CommPortIdentifier portId;
	private List<Class<? extends AbstractEsc>> escs;
	private JComboBox<Class<? extends AbstractEsc>> escList;
	private JComboBox<CommPortIdentifier> portList;
	private List<CommPortIdentifier> ports;
	private JComboBox<Routine> routines;
	private Controller controller;

	/**
	 * Instantiate a LeftPanel which operates using the {@link Controller}
	 * passed as a parameter. Generates three combo boxes populated respectively
	 * using {@link PortSelector#scanPorts()}, {@link EscFactory#getEscsList()}
	 * and {@link RoutineLoader#loadFrom(File...)}. Then adds a button START
	 * which launches the selected routine on the selected esc from the
	 * controller, opening another panel in which datas will be displayed in the
	 * most appropriate form (see {@link Controller#startRoutine(Routine, AbstractEsc)}) .
	 * 
	 * @param cont,
	 *            the controller needed to operate from the main frame.
	 */
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

	/**
	 * Istantiate the selected implementation of {@link AbstractEsc} using the
	 * method {@link EscFactory#newInstanceOf(Class, SerialPort)} based on the
	 * selected port, and opens the connection. Catch every eventual error and
	 * exception showing a specifical message error on the GUI.
	 * 
	 * @return the connected esc based on parameters selection.
	 */
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
