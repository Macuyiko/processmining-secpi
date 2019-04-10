package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

import java.awt.Graphics2D;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMap.ArrowType;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;
import org.processmining.models.shapes.Decorated;

public class DependencyGraphEdge<S extends DependencyGraphNode, T extends DependencyGraphNode> 
	extends AbstractDirectedGraphEdge<S, T> 
	implements Decorated {
	
	public DependencyGraphEdge(S source, T target) {
		super(source, target);
		getAttributeMap().put(AttributeMap.SHOWLABEL, false);
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_SIMPLE);
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		
	}
}
