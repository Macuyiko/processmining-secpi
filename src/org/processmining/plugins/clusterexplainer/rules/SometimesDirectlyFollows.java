package org.processmining.plugins.clusterexplainer.rules;

import org.deckfour.xes.model.XTrace;

public class SometimesDirectlyFollows extends AbstractRule {
	
	public SometimesDirectlyFollows() {

	}
	
	public String toString() {
		return "SometimesDirectlyFollows: "+parameters.get(0)+" and "+parameters.get(1) + 
				" follow each other directly somewhere in the trace. " + 
				"If this is not the case or the activities do not appear, the rule evaluates as false";
	}
	
	public String toShortString() {
		return "SometimesDirectlyFollows("+parameters.get(0)+","+parameters.get(1)+")";
	}
	
	public boolean getValue(XTrace trace) {
		for (int i = 0; i < trace.size()-1; i++)
			if ((  classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0))) 
			&&  (classifier.getClassIdentity(trace.get(i+1)).equals(parameters.get(1))))
				return true;
		return false;
	}
	
	public int nrParameters() {
		return 2;
	}
	
}
