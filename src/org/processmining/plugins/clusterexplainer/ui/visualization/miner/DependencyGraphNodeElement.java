package org.processmining.plugins.clusterexplainer.ui.visualization.miner;


public class DependencyGraphNodeElement extends DependencyGraphNode {
	public DependencyGraphNodeElement(DependencyGraph net, int code, String label) {
		super(net, code, label);
	}

	public String toString() {
		return getLabel();
	}
}
