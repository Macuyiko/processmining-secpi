package org.processmining.plugins.clusterexplainer.experiments;
import java.util.Collection;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.plugins.ClusterExplainerPlugin;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.kutoolbox.utils.LogUtils;


public abstract class ExplainerModel {

	protected final XLog clusteredLog;
	protected RuleDataSet dataset;
	protected ClusterExplainerSettings explainerSettings;
	
	public ExplainerModel(XLog clusteredLog, ClusterExplainerSettings explainerSettings) {
		this.clusteredLog = clusteredLog;
		this.explainerSettings = explainerSettings;
		
		if (!ClusterExplainerPlugin.checkLog(clusteredLog)) {
			System.err.println("This log does not seem to be clustered!");
		}
	}
	
	private void constructDataset() {
		try {
			Collection<String> activities = LogUtils.getEventClassesAsString(
					clusteredLog, XLogInfoImpl.STANDARD_CLASSIFIER);
			RuleDataSet dataset = ClusterExplainerPlugin.getDataSet(
					null, this.clusteredLog, activities, this.explainerSettings);
			constructDataset(dataset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void constructDataset(RuleDataSet givenDataSet) {
		dataset = givenDataSet;
		if (givenDataSet == null)
			constructDataset();
	}
	
	public abstract double getAccuracy();
	public abstract double getManualAccuracy();
	public abstract double getInstanceSize(int traceIndex);
	
	public abstract boolean isCorrectlyClassified(int instance);
	
	protected int getFeatureIndex(String attribute) {
		for (int rh = 0; rh < dataset.getRules().size(); rh++)
			if (dataset.getRule(rh).toShortString().equals(attribute))
				return rh;
		return -1;
	}
	
	public XLog getClusteredLog() {
		return clusteredLog;
	}

	public double[] getExtraInfo() {
		return new double[] {0,0,0};
		
	}

	
}
