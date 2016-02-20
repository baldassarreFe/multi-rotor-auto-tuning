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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import analyzer.Analyzer;
import analyzer.AnalyzersFactory;
import controller.Controller;

public class RightPanel extends JPanel {
	private static final long serialVersionUID = 2486037060223064661L;
	private JComboBox<Class<? extends Analyzer>> analyzersList;
	private File logFile;
	private JButton analyze;
	List<Class<? extends Analyzer>> analyzers;
	private final Controller controller;

	public RightPanel(Controller cont) {
		this.controller = cont;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		analyzers = AnalyzersFactory.getAnalyzersList();
		analyzersList = new JComboBox<>();
		for (Class<? extends Analyzer> c : analyzers) {
			analyzersList.addItem(c);
		}
		analyzersList.setRenderer(new CustomClassRenderer());
		analyzersList.setMaximumSize(new Dimension(400,(int)analyzersList.getPreferredSize().getHeight()+10));


		analyze = new JButton("OPEN");
		analyze.setEnabled(false);
		analyze.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				Analyzer analyzer = AnalyzersFactory
						.newInstanceOf((Class<? extends Analyzer>) analyzersList.getSelectedItem(), logFile);
				controller.startAnalysis(analyzer);
			}
		});
		analyze.setAlignmentX(Component.CENTER_ALIGNMENT);


		JButton browse = new JButton("Browse");
		final JTextField l = new JTextField("Choose file");
		l.setEditable(false);
		l.setMinimumSize(new Dimension(200, (int) browse.getPreferredSize().getHeight()));
		l.setMaximumSize(new Dimension(400, (int) browse.getPreferredSize().getHeight()));
		l.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));

		final JFileChooser fc = new JFileChooser(new File("."));
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fc.showDialog(getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
					try {
						logFile = fc.getSelectedFile();
						l.setText(logFile.getName());
						analyze.setEnabled(true);
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

		this.add(chooser);
		this.add(Box.createRigidArea(new Dimension(0,20)));
		this.add(analyzersList);
		this.add(Box.createRigidArea(new Dimension(0,20)));
		this.add(analyze);
		this.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

	}
}
