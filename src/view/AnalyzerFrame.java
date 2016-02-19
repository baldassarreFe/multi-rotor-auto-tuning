package view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import analyzer.Analyzer;

public class AnalyzerFrame extends JFrame {
	private Analyzer analyzer;

	public AnalyzerFrame(Analyzer analyzer) {
		this.analyzer = analyzer;
		initGraphic();
	}

	public void initGraphic() {
		this.setLayout(new BorderLayout());

		this.pack();
		this.setVisible(true);
	}
}
