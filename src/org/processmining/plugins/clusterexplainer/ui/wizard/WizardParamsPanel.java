package org.processmining.plugins.clusterexplainer.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;

import org.processmining.plugins.kutoolbox.ui.FancyDoubleSlider;
import org.processmining.plugins.kutoolbox.ui.FancyIntegerSlider;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardParamsPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	private static final long serialVersionUID = 5401447749409252641L;

	private double c, e, removeCorrelatedThreshold;
	private boolean useZeroes, useMC, stopEarly, removeZeroVariance;
	private int iterations;

	private FancyIntegerSlider sliderIterations;
	private FancyDoubleSlider sliderC, sliderE, sliderRemoveCorrelated;
	private JCheckBox checkZeroes, checkMCExpl, checkStopEarly, checkRemoveZeroVariance;
	
	public WizardParamsPanel() {
		super(9);
		this.init();
	}

	protected void init() {
		c = 4.0;
		e = 0.1;
		useZeroes = false;
		useMC = true;
		iterations = 10;
		stopEarly = false;
		removeZeroVariance= true;
		removeCorrelatedThreshold= 1.0D;
	
		
		addLabel("Nr. of rule searching iterations:", 1);
		sliderIterations = addIntegerSlider(1, iterations, 300, 1);
		
		addLabel("C value:", 2);
		sliderC = addDoubleSlider(1, c, 10, 2);
		
		addLabel("E value:", 3);
		sliderE = addDoubleSlider(0.1, e, 1, 3);
		
		addLabel("Remove features with a correlation larger than:", 4);
		addLabel("No features will be removed for a value larger than 1.", 5);
		sliderRemoveCorrelated = addDoubleSlider(0.5, removeCorrelatedThreshold, 1.5, 4);
		
		checkZeroes = addCheckbox("Also use 0 to 1 attribute swaps", useZeroes, 6, false);
		
		checkMCExpl = addCheckbox("Use WTA prediction for finding explanations", useMC, 7, false);
		
		checkStopEarly = addCheckbox("Stop when shortest solution is found", stopEarly, 8, false);
		
		checkRemoveZeroVariance = addCheckbox("Remove features without variance", removeZeroVariance, 9, true);
		
		

		
	}

	@Override
	public String getTitle() {
		return "Parameter configuration";
	}

	@Override
	public void resetSettings() {
		
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		c = Double.parseDouble(settings.get("c").toString());
		e = Double.parseDouble(settings.get("e").toString());
		iterations = Integer.parseInt(settings.get("iterations").toString());
		useZeroes = settings.get("useZeroes").toString().equals("1");
		useMC = settings.get("useMC").toString().equals("1");
		stopEarly= settings.get("stopEarly").toString().equals("1");
		removeZeroVariance= settings.get("removeZeroVariance").toString().equals("1");
		removeCorrelatedThreshold= Double.parseDouble(settings.get("removeCorrelatedThreshold").toString());
		
		
		sliderC.setValue(c);
		sliderE.setValue(e);
		sliderIterations.setValue(iterations);
		checkZeroes.setSelected(useZeroes);
		checkMCExpl.setSelected(useMC);
		checkStopEarly.setSelected(stopEarly);
		checkRemoveZeroVariance.setSelected(removeZeroVariance);
		sliderRemoveCorrelated.setValue(1.0D);
	}

	@Override
	public Map<String, Object> getSettings() {
		c = sliderC.getValue();
		e = sliderE.getValue();
		iterations = sliderIterations.getValue();
		useZeroes = checkZeroes.isSelected();
		useMC = checkMCExpl.isSelected();
		stopEarly = checkStopEarly.isSelected();
		removeZeroVariance= checkRemoveZeroVariance.isSelected();
		removeCorrelatedThreshold= sliderRemoveCorrelated.getValue();
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("c", c);
		settings.put("e", e);
		settings.put("iterations", iterations);
		settings.put("useZeroes", useZeroes ? "1" : "0");
		settings.put("useMC", useMC ? "1" : "0");
		settings.put("stopEarly", stopEarly ? "1" : "0");
		settings.put("removeZeroVariance", removeZeroVariance ? "1" : "0");
		settings.put("removeCorrelatedThreshold", removeCorrelatedThreshold);
		return settings;
	}

	@Override
	protected void updateFields() {
		
	}

}
