package org.processmining.plugins.clusterexplainer.plugins;

import java.io.File;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.rules.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;

import de.bwaldvogel.liblinear.Feature;

public class TestExplainer {
	public static void main(String[] args) {
		String logFile = "C:\\Users\\n11093\\Desktop\\dlclustered.xes";
		XLog log = ImportUtils.openLog(new File(logFile));
		ClusterExplainerSettings settings = new ClusterExplainerSettings();
		settings.iterations = 10;
		
		Object[] result = ClusterExplainerPlugin.explainUI(null, log, settings);
		ClusterExplanator explanator = (ClusterExplanator) result[0];
		
		System.out.println("Number of instances: "+explanator.getDataset().nrInstances());
		System.out.println("Number of features: "+explanator.getDataset().nrFeatures());
		System.out.println("First rule: "+explanator.getDataset().getRule(0));
		Feature[] fInstance = explanator.getDataset().getInstance(0);
		System.out.println("First instance: ");
		for (Feature f : fInstance) System.out.println("    "+f);
		System.out.println("First instance first feature: "+fInstance[0]);
		System.out.println("First instance first index feature: "+explanator.getDataset().getFeature(0, fInstance));
		
		double[][] predict = explanator.predict(explanator.getDataset().getOriginalLabel(0), explanator.getDataset().getInstance(0));
		System.out.println("Explaining first instance: "+trace2String(log.get(0)));
		System.out.println("Has label: "+predict[0][0]+" with p: "+predict[0][1]);
		
		List<Rule[]> expl = explanator.getExplanations(0);
		for (Rule[] rule : expl){
			System.out.println("RULE");
			for (Rule r : rule){
				System.out.println("  "+r.toShortString());
			}
		}
	}
	
	public static String trace2String(XTrace trace) {
		String t = "";
		for (XEvent event : trace)
			t+=XConceptExtension.instance().extractName(event)+"   ";
		return t;
	}
	
	
	
}
