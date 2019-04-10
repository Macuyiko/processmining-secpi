package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

public class DependencyGraphEdgeElement extends DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> {
	public DependencyGraphEdgeElement(DependencyGraphNode source, DependencyGraphNode target) {
		super(source, target);
	}

	public String toString() {
		return getSource().getLabel() + " -> " + getTarget().getLabel();
	}
}