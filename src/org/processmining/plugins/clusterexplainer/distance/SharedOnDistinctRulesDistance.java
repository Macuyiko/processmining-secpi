package org.processmining.plugins.clusterexplainer.distance;

import java.util.HashSet;
import java.util.Set;

import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;

public class SharedOnDistinctRulesDistance extends AbstractDistance {

	public SharedOnDistinctRulesDistance(ClusterExplanator explanator) {
		super(explanator);
	}

	protected double getDistance(Set<Integer> atts1, Set<Integer> atts2) {
		double dist = 0;
		for (int a : atts1)
			if (atts2.contains(a))
				dist++;
		Set<Integer> newSet = new HashSet<Integer>();
		newSet.addAll(atts1);
		newSet.addAll(atts2);
		double denom = newSet.size();
		return dist / denom;
	}

	public Distance getType() {
		return Distance.SHARED_ON_DISTINCT;
	}
}
