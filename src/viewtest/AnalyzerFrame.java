package viewtest;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
				for (String s : resultMap.keySet()) {
					resultMap.get(s).setText(""+analyzer.results.get(s));
				}

			}
		});
		this.add(analyze);

		this.resultsPanel = new JPanel(new GridLayout(analyzer.results.size(), 2));
		for (String s : analyzer.results.keySet()) {
			resultsPanel.add(new JLabel(s));
			JTextField value = new JTextField();
			value.setEditable(false);
			resultMap.put(s, value);
			resultsPanel.add(value);

		}
		this.add(resultsPanel);

		this.setSize(450, 200);
		this.setVisible(true);
	}
}
