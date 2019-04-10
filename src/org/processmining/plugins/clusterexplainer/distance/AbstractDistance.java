package org.processmining.plugins.clusterexplainer.distance;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.rules.Rule;

public abstract class AbstractDistance {

	protected ClusterExplanator explanator;
	public static enum Distance {NUMBER_SHARED, SHARED_ON_MIN, SHARED_ON_MAX, SHARED_ON_DISTINCT, SHARED_ATTRIBUTES};
	
	public static AbstractDistance create(ClusterExplanator explanator, Distance type) {
		switch (type) {
		case NUMBER_SHARED:
			return new SharedRulesDistance(explanator);
		case SHARED_ON_DISTINCT:
			return new SharedOnDistinctRulesDistance(explanator);
		case SHARED_ON_MAX:
			return new SharedOnMaxRulesDistance(explanator);
		case SHARED_ON_MIN:
			return new SharedOnMinRulesDistance(explanator);
		case SHARED_ATTRIBUTES:
			return new SharedAttributesRatioDistance(explanator);
		default:
			break;
		}
		return null;
	}
	
	protected AbstractDistance(ClusterExplanator explanator) {
		this.explanator = explanator;
	}
	
	public double getDistance(XTrace trace1, XTrace trace2, boolean maximize) {
		return this.getDistance(explanator.getInstance(trace1), explanator.getInstance(trace2), maximize);
	}

	public double getDistance(int instance1, int instance2, boolean maximize) {
		List<Rule[]> explanations1 = explanator.getExplanations(instance1);
		List<Rule[]> explanations2 = explanator.getExplanations(instance2);
		
		double bestScore = 0;
		boolean firstScoreSet = false;
		
		for (Rule[] rule1 : explanations1) {
			for (Rule[] rule2 : explanations2) {
				Set<Integer> atts1 = explanator.getAttributeSet(rule1);
				Set<Integer> atts2 = explanator.getAttributeSet(rule2);
				double distance = this.getDistance(atts1, atts2);
				if (firstScoreSet == false 
						|| (maximize  && distance >= bestScore)
						|| (!maximize && distance <= bestScore)) {
					bestScore = distance;
					firstScoreSet = true;
				}
			}
		}
		
		return bestScore;
		
	}
	
	protected abstract double getDistance(Set<Integer> atts1, Set<Integer> atts2);
	
	public abstract Distance getType();

}
