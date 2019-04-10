package org.processmining.plugins.clusterexplainer.rules;

import org.deckfour.xes.model.XTrace;

public class AlwaysDirectlyFollows extends AbstractRule {
	
	public AlwaysDirectlyFollows() {

	}
	
	public String toString() {
		return "AlwaysDirectlyFollows: whenever "+parameters.get(0)+" appears," + 
				parameters.get(1)+" always directly follows. " +
				"If the activities are not present in the trace, " +
				"the rule will evaluate as false";
	}
	
	public String toShortString() {
		return "AlwaysDirectlyFollows("+parameters.get(0)+","+parameters.get(1)+")";
	}
	
	public boolean getValue(XTrace trace) {
		boolean firstPresent = false;
		boolean secondPresent = false;
		for (int i = 0; i < trace.size(); i++) {
			if (classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0)))
				firstPresent = true;
			if (classifier.getClassIdentity(trace.get(i)).equals(parameters.get(1)))
				secondPresent = true;
			if (firstPresent && secondPresent) break;
		}
		if (!(firstPresent && secondPresent)) return false;
		for (int i = 0; i < trace.size()-1; i++) {
			if ((  classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0))) 
			&&  (!classifier.getClassIdentity(trace.get(i+1)).equals(parameters.get(1))))
				return false;
		}
		return true;
	}

	public int nrParameters() {
		return 2;
	}
	
}
