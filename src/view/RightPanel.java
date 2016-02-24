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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import analyzer.Analyzer;
import analyzer.AnalyzersFactory;
import controller.Controller;
import esc.FileFormatException;

public class RightPanel extends JPanel {
	private static final long serialVersionUID = 2486037060223064661L;
	private JComboBox<Class<? extends Analyzer>> analyzersList;
	private File logFile;
	private File parametersFile;
	private JButton analyze;
	List<Class<? extends Analyzer>> analyzers;
	private final Controller controller;
	private JButton graphs;

	public RightPanel(Controller cont) {
		this.controller = cont;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		analyzers = AnalyzersFactory.getAnalyzersList();
		analyzersList = new JComboBox<>();
		for (Class<? extends Analyzer> c : analyzers) {
			analyzersList.addItem(c);
		}
		analyzersList.setRenderer(new CustomClassRenderer());
		analyzersList.setMaximumSize(new Dimension(400, (int) analyzersList.getPreferredSize().getHeight() + 10));

		analyze = new JButton("OPEN");
		analyze.setEnabled(false);
		analyze.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					@SuppressWarnings("unchecked")
					Analyzer analyzer = AnalyzersFactory.newInstanceOf(
							(Class<? extends Analyzer>) analyzersList.getSelectedItem(), logFile, parametersFile);
					controller.startAnalysis(analyzer);
				} catch (InvocationTargetException ite) {
					if (ite.getCause() instanceof FileFormatException) {
						ite.getCause().printStackTrace();
						JOptionPane.showMessageDialog(getParent(), "Problema nel parsing del file");
					} else if (ite.getCause() instanceof IOException) {
						ite.getCause().printStackTrace();
						JOptionPane.showMessageDialog(getParent(), "Problema nella lettura del file");
					} else { // eccezione generica generata dai costruttori delle future sottoclassi
						ite.getCause().printStackTrace();
						JOptionPane.showMessageDialog(getParent(), "Problema: " + ite.getCause().getMessage());
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Problema: " + e1.getMessage());
				}
			}
		});
		analyze.setAlignmentX(Component.CENTER_ALIGNMENT);

		graphs = new JButton("VIEW GRAPHS");
		graphs.setEnabled(false);
		graphs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new GraphsFrame(logFile);
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Problema nel parsing del file");
				}
			}
		});
		graphs.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton browse = new JButton("Browse");
		final JTextField l = new JTextField("Choose file");
		l.setEditable(false);
		l.setMinimumSize(new Dimension(200, (int) browse.getPreferredSize().getHeight()));
		l.setMaximumSize(new Dimension(400, (int) browse.getPreferredSize().getHeight()));
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

		final JFileChooser fc = new JFileChooser(new File("."));
		fc.setFileFilter(new FileNameExtensionFilter("Motor data", "csv"));
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fc.showDialog(getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
					try {
						logFile = fc.getSelectedFile();
						l.setText(logFile.getName());
						analyze.setEnabled(true);
						graphs.setEnabled(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		JPanel chooser = new JPanel();
		chooser.setLayout(new BoxLayout(chooser, BoxLayout.LINE_AXIS));
		chooser.add(l);
		chooser.add(browse);

		JButton browse2 = new JButton("Browse");
		final JTextField l2 = new JTextField("Choose parameters file");
		l2.setEditable(false);
		l2.setMinimumSize(new Dimension(200, (int) browse.getPreferredSize().getHeight()));
		l2.setMaximumSize(new Dimension(400, (int) browse.getPreferredSize().getHeight()));
		l2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

		final JFileChooser fc2 = new JFileChooser(new File("."));
		fc2.setFileFilter(new FileNameExtensionFilter("Parameters file", "properties"));
		browse2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fc2.showDialog(getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
					try {
						parametersFile = fc2.getSelectedFile();
						l2.setText(parametersFile.getName());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		JPanel chooser2 = new JPanel();
		chooser2.setLayout(new BoxLayout(chooser2, BoxLayout.LINE_AXIS));
		chooser2.add(l2);
		chooser2.add(browse2);

		this.add(chooser);
		this.add(Box.createRigidArea(new Dimension(0, 20)));
		this.add(chooser2);
		this.add(Box.createRigidArea(new Dimension(0, 20)));
		this.add(analyzersList);
		this.add(Box.createRigidArea(new Dimension(0, 20)));
		this.add(analyze);
		this.add(Box.createRigidArea(new Dimension(0, 20)));
		this.add(graphs);
		this.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

	}
}
