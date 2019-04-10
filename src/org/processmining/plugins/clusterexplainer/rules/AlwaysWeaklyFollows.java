package org.processmining.plugins.clusterexplainer.rules;

import org.deckfour.xes.model.XTrace;

public class AlwaysWeaklyFollows extends AbstractRule {
	
	public AlwaysWeaklyFollows() {

	}
	
	public String toString() {
		return "AlwaysWeaklyFollows: "+parameters.get(0) + " and " + parameters.get(1) +
				" always eventually follow each other somewhere in the trace. " + 
				"If the activities are not present in the trace, " +
				"the rule will evaluate as false";
	}
	
	public String toShortString() {
		return "AlwaysWeaklyFollows("+parameters.get(0)+","+parameters.get(1)+")";
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
		for (int i = 0; i < trace.size(); i++) {
			if (!classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0)))
				continue;
			boolean found = false;
			for (int j = i+1; j < trace.size(); j++) {
				if (classifier.getClassIdentity(trace.get(j)).equals(parameters.get(1))) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}
	
	public int nrParameters() {
		return 2;
	}
	
}
