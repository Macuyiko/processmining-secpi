package org.processmining.plugins.clusterexplainer.distance;

import java.util.Set;

import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;

public class SharedRulesDistance extends AbstractDistance {

	public SharedRulesDistance(ClusterExplanator explanator) {
		super(explanator);
	}

	protected double getDistance(Set<Integer> atts1, Set<Integer> atts2) {
		double dist = 0;
		for (int a : atts1)
			if (atts2.contains(a))
				dist++;
		return dist;
	}

	public Distance getType() {
		return Distance.NUMBER_SHARED;
	}
}
