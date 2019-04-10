package org.processmining.plugins.clusterexplainer.distance;

import java.util.Set;

import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;

public class SharedOnMaxRulesDistance extends AbstractDistance {

	public SharedOnMaxRulesDistance(ClusterExplanator explanator) {
		super(explanator);
	}

	protected double getDistance(Set<Integer> atts1, Set<Integer> atts2) {
		double dist = 0;
		for (int a : atts1)
			if (atts2.contains(a))
				dist++;
		double denom = Math.max(atts1.size(), atts2.size());
		return dist / denom;
	}

	public Distance getType() {
		return Distance.SHARED_ON_MAX;
	}
}
