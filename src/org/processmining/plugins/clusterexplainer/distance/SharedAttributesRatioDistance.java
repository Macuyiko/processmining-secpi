package org.processmining.plugins.clusterexplainer.distance;

import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;

public class SharedAttributesRatioDistance extends AbstractDistance {

	public SharedAttributesRatioDistance(ClusterExplanator explanator) {
		super(explanator);
	}
	
	public double getDistance(XTrace trace1, XTrace trace2, boolean maximize) {
		return this.getDistance(explanator.getInstance(trace1), explanator.getInstance(trace2), maximize);
	}

	public double getDistance(int instance1, int instance2, boolean maximize) {
		double shared = 0;
		double nrFeatures = explanator.getDataset().nrFeatures();
		
		for (int i = 0; i < nrFeatures; i++) {
			if (explanator.getDataset().getFeature(i, instance1).getValue() 
					== explanator.getDataset().getFeature(i, instance2).getValue())
				shared += 1d;
		}
		
		return shared / nrFeatures;
		
	}

	protected double getDistance(Set<Integer> atts1, Set<Integer> atts2) {
		assert(false);
		return -1d;
	}

	public Distance getType() {
		return Distance.SHARED_ATTRIBUTES;
	}
}
