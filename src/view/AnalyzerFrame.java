package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
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
		this.parametersPanel = new JPanel(new GridLayout(analyzer.parametersRequired.size(), 2));
		for (String s : analyzer.parametersRequired.keySet()) {
			JTextField field = new JTextField();
			field.setEditable(true);
			JLabel label = new JLabel(s);
			label.setHorizontalTextPosition(SwingConstants.RIGHT);
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
			field.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
			
			parametersMap.put(s, field);
			parametersPanel.add(label);
			parametersPanel.add(field);
		}
		this.add(parametersPanel);

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
					// si verifica solo se uno dei campi è vuoto
					JOptionPane.showMessageDialog(getParent(), "Errore nei parametri");
				}
				analyzer.calcola();
				for (String s : resultMap.keySet()) {
					resultMap.get(s).setText("" + analyzer.results.get(s));
				}

			}
		});
		this.add(analyze);

		this.resultsPanel = new JPanel(new GridLayout(analyzer.results.size(), 2));
		for (String s : analyzer.results.keySet()) {
			JLabel label = new JLabel(s);
			label.setHorizontalTextPosition(SwingConstants.RIGHT);
			JTextField value = new JTextField();
			value.setEditable(false);
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
			value.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

			resultMap.put(s, value);
			resultsPanel.add(label);
			resultsPanel.add(value);

		}
		this.add(resultsPanel);

		this.setSize(450, 200);
		this.setVisible(true);
	}
}
