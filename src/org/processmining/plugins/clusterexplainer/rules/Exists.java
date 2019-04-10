package org.processmining.plugins.clusterexplainer.rules;

import org.deckfour.xes.model.XTrace;

public class Exists extends AbstractRule {
	
	public Exists() {

	}
	
	public String toString() {
		return "Exists: "+parameters.get(0)+" appears somewhere in the trace";
	}
	
	public String toShortString() {
		return "Exists("+parameters.get(0)+")";
	}
	
	public boolean getValue(XTrace trace) {
		for (int i = 0; i < trace.size(); i++)
			if (classifier.getClassIdentity(trace.get(i)).equals(parameters.get(0)))
				return true;
		return false;
	}
	
	public int nrParameters() {
		return 1;
	}
	
}
