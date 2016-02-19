package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import analyzer.Analyzer;
import analyzer.AnalyzersFactory;
import controller.Controller;

public class RightPanel extends JPanel {
	private static final long serialVersionUID = 2486037060223064661L;
	private JComboBox<String> analyzersList;
	private File logFile;
	private JButton analyze;
	Map<String, Class<? extends Analyzer>> analyzers;
	private final Controller controller;

	public RightPanel(Controller cont) {
		this.controller = cont;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		analyzers = AnalyzersFactory.getAnalyzersMap();
		analyzersList = new JComboBox<>(analyzers.keySet().toArray(new String[] {}));

		analyze = new JButton("Analyze");
		analyze.setEnabled(false);
		analyze.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.startAnalysis(AnalyzersFactory.newInstanceOf(analyzers.get(analyzersList.getSelectedItem()), logFile));
			}
		});

		JButton browse = new JButton("Browse");
		final JLabel l = new JLabel("Choose file");
		final JFileChooser fc = new JFileChooser();
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fc.showDialog(getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
					try {
						logFile = fc.getSelectedFile();
						l.setText(logFile.getCanonicalPath());
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
		this.add(analyzersList);
		this.add(analyze);
	}
}
