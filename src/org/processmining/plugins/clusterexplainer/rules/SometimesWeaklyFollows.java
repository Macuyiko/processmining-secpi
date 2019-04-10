package org.processmining.plugins.clusterexplainer.rules;

import org.deckfour.xes.model.XTrace;

public class SometimesWeaklyFollows extends AbstractRule {
	
	public SometimesWeaklyFollows() {

	}
	
	public String toString() {
		return "SometimesWeaklyFollows: "+parameters.get(0)+" and "+parameters.get(1) +
				" follow each other somewhere in the trace" + 
				"If this is not the case or the activities do not appear, the rule evaluates as false";
	}
	
	public String toShortString() {
		return "SometimesWeaklyFollows("+parameters.get(0)+","+parameters.get(1)+")";
	}
	
	public boolean getValue(XTrace trace) {
		for (int i = 0; i < trace.size(); i++) {
			for (int j = i+1; j < trace.size(); j++) {
				if ((    classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0))) 
					&&  (classifier.getClassIdentity(trace.get(j)).equals(parameters.get(1))))
							return true;
			}
		}
		return false;
	}
	
	public int nrParameters() {
		return 2;
	}
}
