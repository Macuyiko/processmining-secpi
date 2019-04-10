package org.processmining.plugins.clusterexplainer.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.rules.AbstractRule;
import org.processmining.plugins.clusterexplainer.rules.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.clusterexplainer.ui.wizard.WizardGenerationStrategyPanel;
import org.processmining.plugins.clusterexplainer.ui.wizard.WizardParamsPanel;
import org.processmining.plugins.clusterexplainer.ui.wizard.WizardPrecalcPanel;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.kutoolbox.ui.ParametersWizard;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

@Plugin(name = "Explain Clusters with SVM", 
		parameterLabels = {"Log", "Settings"},
		returnLabels = {"Cluster Explainer", "Cluster Explainer Settings"},
		returnTypes = {
			ClusterExplanator.class,
			ClusterExplainerSettings.class
		},
		userAccessible = true,
		help = "Explains class labels based on SVM backed movements")

public class ClusterExplainerPlugin {
	
	@UITopiaVariant(uiLabel = "Explain Clusters with SVM (Wizard)",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	public static Object[] explainUIWiz(UIPluginContext context, XLog log) {
		UIAttributeConfigurator[] panels = new UIAttributeConfigurator[] {
			new WizardParamsPanel(),
			new WizardGenerationStrategyPanel(),
			new WizardPrecalcPanel(),
		};
		ParametersWizard wizard = new ParametersWizard(context, panels);
		try {
			wizard.show();
		} catch (OperationCancelledException e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// Prepare settings...
		ClusterExplainerSettings settings = new ClusterExplainerSettings();
		settings.c = Double.parseDouble(wizard.getSettings(0).get("c").toString());
		settings.e = Double.parseDouble(wizard.getSettings(0).get("e").toString());
		settings.useZeroes = wizard.getSettings(0).get("useZeroes").toString().equals("1");
		settings.removeZeroVariance = wizard.getSettings(0).get("removeZeroVariance").toString().equals("1");
		settings.stopEarly = wizard.getSettings(0).get("stopEarly").toString().equals("1");
		settings.removeCorrelatedThreshold = Double.parseDouble(wizard.getSettings(0).get("removeCorrelatedThreshold").toString());
		settings.doPrecalculation = wizard.getSettings(2).get("precalc").toString().equals("1");
		
		settings.rulesToGenerate = ((WizardGenerationStrategyPanel) panels[1]).getRulesToGenerate();
		
		return explainUI(context, log, settings); 
	}
	
	
	@UITopiaVariant(uiLabel = "Explain Clusters with SVM (Defaults)",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public static Object[] explainUIDef(UIPluginContext context, XLog log) {
		ClusterExplainerSettings settings = new ClusterExplainerSettings();
		return explainUI(context, log, settings);
	}
	
	
	@UITopiaVariant(uiLabel = "Explain Clusters with SVM (Given Settings)",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Given settings", requiredParameterLabels = { 0, 1 })
	public static Object[] explainUI(UIPluginContext context, XLog log, ClusterExplainerSettings settings) {
		// Check the log
		if (!checkLog(log)) {
			JOptionPane.showMessageDialog(null, "Log does not seem to be clustered. " +
					"Make sure all traces have the key: "+ClusterExplainerSettings.CLUSTER_ATTRIBUTE);
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// Make rule data set headers
		Collection<String> activities = LogUtils.getEventClassesAsString(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		
		RuleDataSet dataset;
		try {
			dataset = getDataSet(context, log, activities, settings);
		} catch (InstantiationException | IllegalAccessException e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// Put everything in explainer
		ClusterExplanator explanator = new ClusterExplanator(log, dataset, settings);
		
		if (settings.doPrecalculation) {
			// Premake models
			constructModels(context, log, explanator);
			
			// Precalc explanations
			calculateExplanations(context, log, explanator);
		}
		
		return new Object[]{explanator, settings};
		
	}
	
	public static boolean checkLog(XLog log) {
		for (XTrace t : log) {
			if (!t.getAttributes().containsKey(ClusterExplainerSettings.CLUSTER_ATTRIBUTE))
				return false;
		}
		return true;
	}
	
	public static void calculateExplanations(UIPluginContext context, XLog log, ClusterExplanator explanator) {
		if (context != null){
			context.log("Pre-calculating explanations, this might take some time...");
			context.getProgress().setIndeterminate(false);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(explanator.getDataset().nrInstances()+1);
			context.getProgress().setValue(0);
		}
		
		
		for (int instance = 0; instance < explanator.getDataset().nrInstances(); instance++) {
			if (context != null)
				context.getProgress().setValue(instance);
			
			explanator.getExplanations(instance);
		}
				
	}
	
	public static void constructModels(UIPluginContext context, XLog log, ClusterExplanator explanator) {
		Set<Integer> clusters = new HashSet<Integer>();
		for (int i = 0; i < log.size(); i++){
			int cluster = Integer.parseInt(log.get(i).getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
			clusters.add(cluster);
		}
		
		if (context != null){
			context.log("Generating models, this might take a while...");
			context.getProgress().setIndeterminate(false);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(clusters.size()+1);
			context.getProgress().setValue(0);
		}
		
		
		int modelCounter = 0;
		for (int cluster : clusters) {
			if (context != null)
				context.getProgress().setValue(modelCounter);
			modelCounter++;
			
			explanator.train(cluster);
		}
				
	}
	
	public static RuleDataSet getDataSet(UIPluginContext context, XLog log, 
			Collection<String> activities, ClusterExplainerSettings settings) 
					throws InstantiationException, IllegalAccessException {
		List<Rule> rules = getRuleHeader(activities, settings);
		List<Pair<Integer, List<Double>>> values = getValues(context, log, rules);
		if (settings.removeZeroVariance)
			removeZeroVariance(context, rules, values);
		if (settings.removeCorrelatedThreshold <= 1D)
			removeCorrelated(context, rules, values, settings.removeCorrelatedThreshold);
		RuleDataSet dataset = new RuleDataSet(rules);
		for (Pair<Integer, List<Double>> p : values)
		dataset.addLine(p.getSecond(), p.getFirst());
		return dataset;
	}

	public static List<Rule> getRuleHeader(Collection<String> activities, ClusterExplainerSettings settings) throws InstantiationException, IllegalAccessException {
		List<Rule> rulesInst = new ArrayList<Rule>();
		for (AbstractRule.Rule ruleTemplate : settings.rulesToGenerate) {
			if (AbstractRule.create(ruleTemplate).nrParameters() == 1) {
				for (String a : activities) {
					Rule rule = AbstractRule.create(ruleTemplate);
					rule.setParameters(new Object[]{a});
					rulesInst.add(rule);
				}
			} else {
				for (String a : activities) {
					for (String b : activities) {
						Rule rule = AbstractRule.create(ruleTemplate);
						rule.setParameters(new Object[]{a, b});
						rulesInst.add(rule);
					}
				}
			}
		}
		return rulesInst;
	}


	public static List<Pair<Integer, List<Double>>> getValues(UIPluginContext context, XLog log, List<Rule> rules) {
		GroupedXLog groupedLog = new GroupedXLog(log);
		List<Pair<Integer,List<Double>>> data = new ArrayList<Pair<Integer,List<Double>>>();
				
		if (context != null){
			context.log("Generating dataset, this might take a while...");
			context.getProgress().setIndeterminate(false);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(groupedLog.size()+1);
			context.getProgress().setValue(0);
		}
		
		for (int traceCounter = 0; traceCounter < groupedLog.size(); traceCounter++) {
			if (context != null)
				context.getProgress().setValue(traceCounter);
			List<Double> r = new ArrayList<Double>();
			for (int index = 0; index < rules.size(); index++) {
				double v = rules.get(index).getValue(groupedLog.get(traceCounter).get(0)) ? 1D : 0D;
				r.add(v);
			}
			for (XTrace t : groupedLog.get(traceCounter)) {
				int c = Integer.parseInt(t.getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
				data.add(new Pair<Integer,List<Double>>(c, new ArrayList<Double>(r)));
				assert r.size() == rules.size();
			}
		}
		
		return data;
	}


	public static void removeZeroVariance(UIPluginContext context, 
			List<Rule> rules,
			List<Pair<Integer, List<Double>>> values) {
		
		if (context != null){
			context.log("Preprocessing dataset, this might take a while...");
			context.getProgress().setIndeterminate(true);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(rules.size()+1);
			context.getProgress().setValue(0);
		}
		
		System.err.println("removeZeroVariance #feats before pruning: "+rules.size());
		for (int i = rules.size()-1; i >= 0; i--) {
			if (context != null) context.getProgress().setValue(i);
			// Check if this feature is the same for every instance
			double z = sd(getCol(values, i));
			if (z == 0) {
				rules.remove(i);
				for (Pair<Integer, List<Double>> p : values) p.getSecond().remove(i);
				continue;
			}
		}
		System.err.println("removeZeroVariance #feats after pruning: "+rules.size());
	}
	
	public static void removeCorrelated(UIPluginContext context, 
			List<Rule> rules,
			List<Pair<Integer, List<Double>>> values,
			double threshold) {
		
		if (context != null){
			context.log("Preprocessing dataset, this might take a while...");
			context.getProgress().setIndeterminate(true);
			context.getProgress().setMinimum(0);
			context.getProgress().setMaximum(rules.size()+1);
			context.getProgress().setValue(0);
		}
		
		System.err.println("removeCorrelated #feats before pruning: "+rules.size());
		for (int i = rules.size()-1; i >= 0; i--) {
			if (context != null) context.getProgress().setValue(i);
			// Check if an earlier feature is perfectly correlated
			List<Double> z = getCol(values, i);
			for (int ii = i-1; ii >= 0; ii--) {
				List<Double> zz = getCol(values, ii);
				double cor = cor(z, zz);
				if (cor >= threshold) {
					rules.remove(i);
					for (Pair<Integer, List<Double>> p : values) p.getSecond().remove(i);
					break;
				}
			}
			
		}
		System.err.println("removeCorrelated #feats after pruning: "+rules.size());
	}
	
	public static List<Double> getCol(List<Pair<Integer, List<Double>>> values, int col) {
		List<Double> colv = new ArrayList<Double>();
		for (int j = 0; j < values.size(); j++) {
			colv.add(values.get(j).getSecond().get(col));
		}
		return colv;
	}
	
	public static double sd(List<Double> list) {
		double n = list.size();
		if (n < 2) {
			return 0d;
		}
		double avg = list.get(0);
		double sum = 0;
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i) < 0)
				continue;
			double newavg = avg + (list.get(i) - avg) / (i + 1);
			sum += (list.get(i) - avg) * (list.get(i) - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n /*- 1*/));
	}
	
	public static double cor(List<Double> list, List<Double> list2) {
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;
		int n = list.size();
		for (int i = 0; i < n; ++i) {
			double x = list.get(i);
			double y = list2.get(i);
			sx += x;
			sy += y;
			sxx += x * x;
			syy += y * y;
			sxy += x * y;
		}
		double cov = sxy / n - sx * sy / n / n;
		double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
		double sigmay = Math.sqrt(syy / n - sy * sy / n / n);
		if (sigmax == 0D || sigmay == 0D)
			return 0D;
		return cov / sigmax / sigmay;
	}
	
}
