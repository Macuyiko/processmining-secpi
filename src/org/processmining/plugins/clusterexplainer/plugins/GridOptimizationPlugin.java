package org.processmining.plugins.clusterexplainer.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.clusterexplainer.ui.plotting.ColoredDotPlot;
import org.processmining.plugins.clusterexplainer.ui.wizard.WizardGenerationStrategyPanel;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.kutoolbox.ui.ParametersWizard;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;
import org.processmining.plugins.kutoolbox.utils.LogUtils;


@Plugin(name = "Perform SVM Grid Optimization", 
		parameterLabels = {"Log", "Settings"},
		returnLabels = {"Best Settings"},
		returnTypes = {
			ClusterExplainerSettings.class,
		},
		userAccessible = true,
		help = "Perform accuracy optimization routine to find best c and eps values for cluster explainer run")

public class GridOptimizationPlugin {
	
	@UITopiaVariant(uiLabel = "Perform SVM Grid Optimization (Wizard)",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	public static ClusterExplainerSettings optimizeUIWiz(UIPluginContext context, XLog log) {
		UIAttributeConfigurator[] panels = new UIAttributeConfigurator[] {
			new WizardGenerationStrategyPanel(),
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
		settings.c = 4.0;
		settings.e = 0.1;
		settings.useZeroes = false;
		
		settings.rulesToGenerate = ((WizardGenerationStrategyPanel) panels[0]).getRulesToGenerate();
		
		return optimizeUI(context, log, settings); 
	}
	
	
	public static ClusterExplainerSettings optimizeUI(UIPluginContext context, XLog log, ClusterExplainerSettings settings) {
		// Make rule data set headers
		Collection<String> activities = LogUtils.getEventClassesAsString(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		RuleDataSet dataset;
		try {
			dataset = ClusterExplainerPlugin.getDataSet(context, log, activities, settings);
		} catch (InstantiationException | IllegalAccessException e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		Set<Double> cValues = new HashSet<Double>();
		for (double c = 1; c <= 10; c += .5)
			cValues.add(c);
		
		Set<Double> eValues = new HashSet<Double>();
		for (double e = 0.1; e <= 10; e += .5)
			eValues.add(e);
		
		Map<double[],Double> accuracies = new HashMap<double[],Double>();
		double bestA = 0;
		double bestC = 0;
		double bestE = 0;
		
		// Create frame
		ColoredDotPlot plotter = new ColoredDotPlot();
		JFrame frame = new JFrame("Grid Optimization");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(plotter, BorderLayout.CENTER);
		frame.setSize(new Dimension(400,400));
		frame.pack();
		frame.setVisible(true);
		plotter.resetPlot(null, 0, 10, 0, 10);
		
		for (double c : cValues) {
			for (double e : eValues) {
				settings.c = c;
				settings.e = e;
				ClusterExplanator explanator = new ClusterExplanator(log, dataset, settings);
				ClusterExplainerPlugin.constructModels(context, log, explanator);
				double acc =  explanator.getAccuracy();
				accuracies.put(new double[]{c, e}, acc);
				plotter.addPoint(c, e, getColor(acc), "");
				if (acc >= bestA) {
					System.out.println("Got better accuracy: "+acc+" with c = "+c+" and e = "+e);
					frame.setTitle("Best = "+acc+" | c = "+c+" | e = "+e);
					bestA = acc;
					bestC = c;
					bestE = e;
				}
			}
		}
		
		settings.c = bestC;
		settings.e = bestE;
		return settings;
		
	}
		
	private static Color getColor(double normalizedValue) {
	    double H = normalizedValue * 0.4;
	    double S = 0.9;
	    double B = 0.7;

	    return Color.getHSBColor((float)H, (float)S, (float)B);
	}
}
