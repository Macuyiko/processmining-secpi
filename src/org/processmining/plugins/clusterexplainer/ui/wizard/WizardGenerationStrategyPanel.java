package org.processmining.plugins.clusterexplainer.ui.wizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import org.processmining.plugins.clusterexplainer.rules.AbstractRule.Rule;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardGenerationStrategyPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	private static final long serialVersionUID = 5401447749409252641L;

	private JCheckBox 
		sometimesdirectlyfollows, 
		alwaysdirectlyfollows,
		sometimesweaklyfollows,
		alwaysweaklyfollows,
		exists;
	
	public WizardGenerationStrategyPanel() {
		super(10);
		this.init();
	}

	protected void init() {
		this.addDoubleLabel("Rules to generate:", 1);
		
		sometimesdirectlyfollows = this.addCheckbox("SometimesDirectlyFollows"
				, true, 2, false);
		alwaysdirectlyfollows = this.addCheckbox("AlwaysDirectlyFollows"
				, false, 3, false);
		sometimesweaklyfollows = this.addCheckbox("SometimesWeaklyFollows"
				, false, 4, false);
		alwaysweaklyfollows = this.addCheckbox("AlwaysWeaklyFollows"
				, false, 5, false);
		exists = this.addCheckbox("Exists"
				, false, 6, false);
	}

	@Override
	public String getTitle() {
		return "Rule generation strategy";
	}

	@Override
	public void resetSettings() {
		
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		sometimesdirectlyfollows.setSelected(settings.get("rule_sometimesdirectlyfollows").equals("1"));
		alwaysdirectlyfollows.setSelected(settings.get("rule_alwaysdirectlyfollows").equals("1"));
		sometimesweaklyfollows.setSelected(settings.get("rule_sometimesweaklyfollows").equals("1"));
		alwaysweaklyfollows.setSelected(settings.get("rule_alwaysweaklyfollows").equals("1"));
		exists.setSelected(settings.get("rule_exists").equals("1"));

	}

	@Override
	public Map<String, Object> getSettings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("rule_sometimesdirectlyfollows", sometimesdirectlyfollows.isSelected() ? "1" : "0");
		settings.put("rule_alwaysdirectlyfollows", alwaysdirectlyfollows.isSelected() ? "1" : "0");
		settings.put("rule_sometimesweaklyfollows", sometimesweaklyfollows.isSelected() ? "1" : "0");
		settings.put("rule_alwaysweaklyfollows", alwaysweaklyfollows.isSelected() ? "1" : "0");
		settings.put("rule_exists", exists.isSelected() ? "1" : "0");
		return settings;
	}
	
	public Set<Rule> getRulesToGenerate() {
		Set<Rule> rules = new HashSet<Rule>();
		if (sometimesdirectlyfollows.isSelected())
			rules.add(Rule.FOLLOWS_DIRECT_SOMETIMES);
		if (alwaysdirectlyfollows.isSelected())
			rules.add(Rule.FOLLOWS_DIRECT_ALWAYS);
		if (sometimesweaklyfollows.isSelected())
			rules.add(Rule.FOLLOWS_WEAK_SOMETIMES);
		if (alwaysweaklyfollows.isSelected())
			rules.add(Rule.FOLLOWS_WEAK_ALWAYS);
		if (exists.isSelected())
			rules.add(Rule.EXISTS);
		return rules;
	}

	@Override
	protected void updateFields() {
		
	}

}
