package org.processmining.plugins.clusterexplainer.experiments;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.plugins.ClusterExplainerPlugin;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.rules.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;


public class SVMExplainerModel extends ExplainerModel {

	protected ClusterExplanator explanator;
	
	public ClusterExplanator getExplanator() {
		return explanator;
	}

	public SVMExplainerModel(XLog clusteredLog, ClusterExplainerSettings explainerSettings) {
		super(clusteredLog, explainerSettings);
	}
	
	public ClusterExplainerSettings getExplainerParameters() {
		return explainerSettings;
	}

	@Override
	public void constructDataset(RuleDataSet dataset) {
		super.constructDataset(dataset);
		
		explanator = new ClusterExplanator(clusteredLog, dataset, explainerSettings);
		
		ClusterExplainerPlugin.constructModels(null, clusteredLog, explanator);
	}

	@Override
	public double getAccuracy() {
		//return explanator.getAccuracy();
		return explanator.getCVAccuracy();
	}
	
	
	
	@Override
	public double getManualAccuracy() {
		double total = 0;
		for (int i = 0; i < clusteredLog.size(); i++) {
			if (isCorrectlyClassified(i))
				total+=1;
		}
		return total / (double) clusteredLog.size();
	}

	@Override
	public boolean isCorrectlyClassified(int instance) {
		if (explainerSettings.useMultiClassExplanation) {
			int prediction = (int) explanator.predictMulti(instance)[0][0];
			return (prediction == dataset.getOriginalLabel(instance));
		} else {
			int prediction = (int) explanator.predict(instance)[0][0];
			return (prediction == 1);
		}
	}
	

	@Override
	public double getInstanceSize(int traceIndex) {
		List<Rule[]> explanations = explanator.getExplanations(traceIndex);
		if (explanations.size() == 0) {
			System.err.println("No explanations were found for: "+traceIndex);
			return -1;
		}
		
		return explanations.get(0).length;
	}

}
