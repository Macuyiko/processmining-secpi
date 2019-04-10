package org.processmining.plugins.clusterexplainer.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardPathsPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	private static final long serialVersionUID = 5401447749409252641L;

	private JFileChooser fc;
	private JTextField tWorking, tTrain, tPredict;
	private File workingDir, trainExe, predictExe;
	
	public WizardPathsPanel() {
		super(6);
		this.init();
	}

	protected void init() {
		workingDir = new File("c:\\");
		trainExe = new File("train.exe");
		predictExe = new File("predict.exe");
		tWorking = new JTextField(workingDir.getAbsolutePath());
		tWorking.setEnabled(false);
		tTrain = new JTextField(trainExe.getAbsolutePath());
		tTrain.setEnabled(false);
		tPredict = new JTextField(predictExe.getAbsolutePath());
		tPredict.setEnabled(false);
		
		fc = new JFileChooser();
		
		JButton bWorking = new JButton("...");
		bWorking.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(WizardPathsPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					workingDir = fc.getSelectedFile();
					tWorking.setText(workingDir.getAbsolutePath());
				}
			}
		});
		
		JButton bTrain = new JButton("...");
		bTrain.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = fc.showOpenDialog(WizardPathsPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					trainExe = fc.getSelectedFile();
					tTrain.setText(trainExe.getAbsolutePath());
				}
			}
		});
		
		JButton bPredict = new JButton("...");
		bPredict.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = fc.showOpenDialog(WizardPathsPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					predictExe = fc.getSelectedFile();
					tPredict.setText(predictExe.getAbsolutePath());
				}
			}
		});


		addLabel("Select the working directory:", 1);
		add(tWorking, "1, " + 2 + ", left, top");
		add(bWorking, "2, " + 2 + ", left, top");
		
		addLabel("Select the training executable:", 3);
		add(tTrain, "1, " + 4 + ", left, top");
		add(bTrain, "2, " + 4 + ", left, top");
		
		addLabel("Select the predict executable:", 5);
		add(tPredict, "1, " + 6 + ", left, top");
		add(bPredict, "2, " + 6 + ", left, top");
	}

	@Override
	public String getTitle() {
		return "Paths configuration";
	}

	@Override
	public void resetSettings() {
		
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		workingDir = new File(settings.get("workingDir").toString());
		trainExe = new File(settings.get("trainExe").toString());
		predictExe = new File(settings.get("predictExe").toString());
		
		tWorking.setText(workingDir.getAbsolutePath());
		tTrain.setText(trainExe.getAbsolutePath());
		tPredict.setText(predictExe.getAbsolutePath());
	}

	@Override
	public Map<String, Object> getSettings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("workingDir", workingDir.getAbsolutePath());
		settings.put("trainExe", trainExe.getAbsolutePath());
		settings.put("predictExe", predictExe.getAbsolutePath());
		return settings;
	}

	@Override
	protected void updateFields() {
		
	}

}
