package org.processmining.plugins.clusterexplainer.experiments;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;


public class LogFrame extends JFrame {
	private static final long serialVersionUID = -5477125517996840552L;

	private JTextArea textArea;
	
	public LogFrame() {
		super("LogFrame");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
		this.getContentPane().add(scrollPane);
		this.pack();
		this.setVisible(true);

	}

	public void addLine(String line) {
		addText(line + "\r\n");
	}
	
	public void addText(String line) {
		textArea.setText(textArea.getText() + line);
	}
	
	public void addLine(String[] line) {
		String s = "";
		boolean first = true;
		for (String i : line) {
			if (!first)
				s += "\t";
			s += i;
			first = false;
		}
		textArea.setText(textArea.getText() + s + "\r\n");
	}
}
