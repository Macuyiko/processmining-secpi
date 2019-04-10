package org.processmining.plugins.clusterexplainer.settings;

import java.util.HashSet;
import java.util.Set;

import org.processmining.plugins.clusterexplainer.rules.AbstractRule.Rule;

import de.bwaldvogel.liblinear.SolverType;

public class ClusterExplainerSettings {
	public static final String CLUSTER_ATTRIBUTE = "cluster:label";
	
	public Set<Rule> rulesToGenerate;
	
	public SolverType solver;
	public double c, e, p;
	public int iterations;
	public boolean useZeroes;
	public boolean stopEarly;
	public boolean useMultiClassExplanation;
	public boolean removeZeroVariance;
	public double removeCorrelatedThreshold;
	
	public String modelName, outName, predictName;
	
	public boolean doPrecalculation;
	
	public ClusterExplainerSettings() {
		rulesToGenerate = new HashSet<Rule>();
		rulesToGenerate.add(Rule.FOLLOWS_DIRECT_SOMETIMES);
		
		solver = SolverType.L2R_LR;
		c = 4.0D;
		e = 0.1D;
		p = 0.1D;
		useZeroes = false;
		stopEarly = false;
		useMultiClassExplanation = true;
		iterations = 10;
		removeZeroVariance = true;
		removeCorrelatedThreshold = 1.0D;
		
		modelName = "model";
		outName = "out";
		predictName = "predict";
		
		doPrecalculation = true;
		
	}
}
