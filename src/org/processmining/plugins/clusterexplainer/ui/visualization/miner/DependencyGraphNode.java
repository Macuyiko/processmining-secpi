package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.shapes.Decorated;
import org.processmining.models.shapes.Rectangle;

public class DependencyGraphNode 
	extends AbstractDirectedGraphNode 
	implements Decorated {

	private final int code;
	private final DependencyGraph dependencyGraph;
	
	public DependencyGraphNode(DependencyGraph g, int c, String label) {
		super();
		dependencyGraph = g;
		code = c;
		
		getAttributeMap().put(AttributeMap.SHAPE, new Rectangle());
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.SHOWLABEL, true);
		getAttributeMap().put(AttributeMap.RESIZABLE, false);
		getAttributeMap().put(AttributeMap.SIZE, new Dimension(120, 70));
		getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
		getAttributeMap().put(AttributeMap.SHOWLABEL, true);
	}

	public void setLabel(String newLabel) {
		getAttributeMap().remove(AttributeMap.LABEL);
		getAttributeMap().put(AttributeMap.LABEL, newLabel);
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {

	}

	public AbstractDirectedGraph<?, ?> getGraph() {
		return dependencyGraph;
	}

	public int getCode() {
		return code;
	}
}
