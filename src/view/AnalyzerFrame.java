package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import analyzer.Analyzer;

public class AnalyzerFrame extends JFrame {
	private static final long serialVersionUID = -8435649542156541758L;
	private Analyzer analyzer;
	private JPanel parametersPanel;
	private JPanel resultsPanel;
	private Map<String, JTextField> resultMap;
	private Map<String, JTextField> parametersMap;

	public AnalyzerFrame(Analyzer analyzer) {
		this.analyzer = analyzer;
		resultMap = new HashMap<>();
		parametersMap = new HashMap<>();
		initGraphic();
	}

	public void initGraphic() {
		this.setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		parametersPanel = new JPanel(new GridLayout(analyzer.parametersRequired.size(), 2, 20, 10));
		for (String s : analyzer.parametersRequired.keySet()) {
			JTextField field = new JTextField();
			Double value = analyzer.parametersRequired.get(s);
			if (value != null)
				field.setText("" + value);
			field.setEditable(true);
			JLabel label = new JLabel(s, SwingConstants.RIGHT);
			label.setSize(new Dimension(50, field.getPreferredSize().height));
			field.setPreferredSize(new Dimension(300, field.getPreferredSize().height));
			parametersMap.put(s, field);
			parametersPanel.add(label);
			parametersPanel.add(field);
		}
		this.add(parametersPanel);

		resultsPanel = new JPanel(new GridLayout(analyzer.results.size(), 2, 20, 10));
		for (String s : analyzer.results.keySet()) {
			JLabel label = new JLabel(s, SwingConstants.RIGHT);
			JTextField value = new JTextField();
			value.setEditable(false);
			label.setSize(new Dimension(50, value.getPreferredSize().height));
			value.setPreferredSize(new Dimension(300, value.getPreferredSize().height));

			resultMap.put(s, value);
			resultsPanel.add(label);
			resultsPanel.add(value);
		}

		this.add(Box.createRigidArea(new Dimension(0, 35)));
		this.add(resultsPanel);

		JButton analyze = new JButton("Analyze");
		analyze.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					for (String s : parametersMap.keySet()) {
						Double value = Double.valueOf(parametersMap.get(s).getText());
						analyzer.parametersRequired.put(s, value);
					}
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Errore nei parametri");
					return;
				}
				analyzer.calcola();

				DecimalFormat df = new DecimalFormat("#.######E0");
				for (String s : resultMap.keySet()) {
					String r = df.format(analyzer.results.get(s));
					resultMap.get(s).setText(r);
				}

			}
		});
		analyze.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.add(Box.createRigidArea(new Dimension(0, 30)));
		this.add(analyze);

		this.pack();
		this.setVisible(true);
	}
}
