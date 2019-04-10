package org.processmining.plugins.clusterexplainer.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;

import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardPrecalcPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	private static final long serialVersionUID = 987774154789652641L;

	private boolean precalc;
	
	private JCheckBox checkPrecalc;
	
	public WizardPrecalcPanel() {
		super(2);
		this.init();
	}

	protected void init() {
		precalc = true;
		checkPrecalc = addCheckbox("Pre-calculate models and explanations", precalc, 1, false);
	}

	@Override
	public String getTitle() {
		return "Precalculation settings";
	}

	@Override
	public void resetSettings() {
		
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		precalc = settings.get("precalc").toString().equals("1");	
		checkPrecalc.setSelected(precalc);
	}

	@Override
	public Map<String, Object> getSettings() {
		precalc = checkPrecalc.isSelected();
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("precalc", precalc ? "1" : "0");
		return settings;
	}

	@Override
	protected void updateFields() {
		
	}

}
