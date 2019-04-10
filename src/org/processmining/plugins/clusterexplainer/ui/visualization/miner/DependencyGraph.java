package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;

public class DependencyGraph 
	extends AbstractDirectedGraph<DependencyGraphNode, DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>> {

	protected final Map<Integer, DependencyGraphNode> nodes;
	protected final Set<DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode>> arcs;

	public DependencyGraph() {
		super();
		getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);
		nodes = new HashMap<Integer, DependencyGraphNode>();
		arcs = new LinkedHashSet<DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode>>();
	}

	// Nodes
	public synchronized DependencyGraphNode addNode(int code, String label) {
		DependencyGraphNode node = new DependencyGraphNodeElement(this, code, label);
		nodes.put(code, node);
		graphElementAdded(node);
		return node;
	}
	
	public DependencyGraphNode removeNode(DependencyGraphNode node) {
		removeSurroundingEdges(node);
		if (nodes.containsKey(node.getCode())) {
			nodes.remove(node.getCode());
			return node;
		}
		return null;
	}
	
	public DependencyGraphNode getNodeByCode(int code) {
		return nodes.get(code);
	}
	
	public synchronized Set<DependencyGraphNode> getNodes() {
		Set<DependencyGraphNode> newNodes = new HashSet<DependencyGraphNode>();
		newNodes.addAll(nodes.values());
		return newNodes;
	}

	public void removeNode(DirectedGraphNode cell) {
		if (cell instanceof DependencyGraphNode) {
			removeNode((DependencyGraphNode) cell);
		}
	}
	
	// Edges
	public DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> 
	addArc(DependencyGraphNode source, DependencyGraphNode target) {
		return addArc(source, target, null);
	}

	public synchronized DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> 
	addArc(DependencyGraphNode source, DependencyGraphNode target, String label) {
		checkAddEdge(source, target);
		DependencyGraphEdgeElement a = new DependencyGraphEdgeElement(source, target);
		if (arcs.add(a)) {
			graphElementAdded(a);
			return a;
		} else {
			for (DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> existing : arcs) {
				if (existing.equals(a)) {
					if (label != null) {
						existing.getAttributeMap().put(AttributeMap.LABEL, label);
					}
					return existing;
				}
			}
		}
		assert (false);
		return null;
	}

	public synchronized DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> 
	removeArc(DependencyGraphNode source, DependencyGraphNode target) {
		return removeFromEdges(source, target, arcs);
	}

	public DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> 
	removeArc(DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode> edge) {
		return removeFromEdges(edge.getSource(), edge.getTarget(), arcs);
	}

	@SuppressWarnings({"rawtypes"})
	public void removeEdge(DirectedGraphEdge edge) {
		if (edge instanceof DependencyGraphEdgeElement) {
			arcs.remove(edge);
		} else {
			assert (false);
		}
		graphElementRemoved(edge);
	}

	public synchronized DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> 
	getArc(DependencyGraphNode source, DependencyGraphNode target) {
		Collection<DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode>> set = 
				getEdges(source, target, arcs);
		return (set.isEmpty() ? null : set.iterator().next());
	}

	public synchronized Set<DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>> getEdges() {
		Set<DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>> edges = 
				new HashSet<DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>>();
		edges.addAll(arcs);
		return edges;
	}

	protected AbstractDirectedGraph<DependencyGraphNode, DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>> 
	getEmptyClone() {
		return new DependencyGraph();
	}

	protected Map<? extends DirectedGraphElement, ? extends DirectedGraphElement> 
	cloneFrom(
			DirectedGraph<DependencyGraphNode, DependencyGraphEdge<? extends DependencyGraphNode, ? extends DependencyGraphNode>> graph) {
		Map<DirectedGraphElement, DirectedGraphElement> mapping = 
				new HashMap<DirectedGraphElement, DirectedGraphElement>();

		for (DependencyGraphNode a : graph.getNodes()) {
			mapping.put(a, addNode(a.getCode(), a.getLabel()));
		}

		getAttributeMap().clear();
		AttributeMap map = graph.getAttributeMap();
		for (String key : map.keySet()) {
			getAttributeMap().put(key, map.get(key));
		}
		return mapping;
	}
	
	

}
