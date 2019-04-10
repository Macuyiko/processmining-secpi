package org.processmining.plugins.clusterexplainer.rules;

import java.util.List;

import org.deckfour.xes.model.XTrace;

public interface Rule {
	public String toString();
	public boolean getValue(XTrace trace);
	public List<Object> getParameters();
	public int nrParameters();
	public void setParameters(List<Object> parameters);
	public void setParameters(Object[] parameters);
	public String toShortString();
}
