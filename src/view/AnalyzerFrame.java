package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import analyzer.Analyzer;

public class AnalyzerFrame extends JFrame {
	private static final long serialVersionUID = -8435649542156541758L;
	private Analyzer analyzer;
	private JPanel parametersPanel;
	private JPanel resultsPanel;
	private Map<String, JLabel> resultMap;
	private Map<String, JFormattedTextField> parametersMap;

	public AnalyzerFrame(Analyzer analyzer) {
		this.analyzer = analyzer;
		initGraphic();
	}

	public void initGraphic() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.parametersPanel = new JPanel(new GridLayout(analyzer.parametersRequired.size(), 2));
		DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance();
		for (String s : analyzer.parametersRequired.keySet()) {
			JFormattedTextField field = new JFormattedTextField(format);
			field.setEditable(true);
			parametersMap.put(s, field);
			parametersPanel.add(new JLabel(s));
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
			}
		});

		this.parametersPanel = new JPanel(new GridLayout(analyzer.results.size(), 2));
		for (String s : analyzer.parametersRequired.keySet()) {
			resultsPanel.add(new JLabel(s));
			JLabel value = new JLabel();
			resultMap.put(s, value);
			resultsPanel.add(value);

		}
		this.add(resultsPanel);

		this.pack();
		this.setVisible(true);
	}
}
