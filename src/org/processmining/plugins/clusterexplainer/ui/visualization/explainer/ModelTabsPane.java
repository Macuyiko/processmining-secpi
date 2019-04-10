package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.clusterexplainer.rules.AlwaysDirectlyFollows;
import org.processmining.plugins.clusterexplainer.rules.AlwaysWeaklyFollows;
import org.processmining.plugins.clusterexplainer.rules.Exists;
import org.processmining.plugins.clusterexplainer.rules.Rule;
import org.processmining.plugins.clusterexplainer.rules.SometimesDirectlyFollows;
import org.processmining.plugins.clusterexplainer.rules.SometimesWeaklyFollows;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyGraph;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyGraphEdge;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyGraphNode;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyNet;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyNetToDependencyGraph;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.NaieveMiner;
import org.processmining.plugins.kutoolbox.visualizators.GraphViewPanel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class ModelTabsPane extends JPanel {
	private static final long serialVersionUID = 3698554778841260682L;
	private ClusterExplainerVisualization parent;
	private Set<Integer> clusters;
	private Map<Integer, DependencyNet> clusterModels;
	private Map<Integer, JComponent> clusterPanels;
	private JTabbedPane tabbedPane;
	
	public ModelTabsPane(ClusterExplainerVisualization parent) {
		this.parent = parent;
		this.clusters = new HashSet<Integer>();
		this.tabbedPane = new JTabbedPane();
		this.clusterModels = new HashMap<Integer, DependencyNet>();
		this.clusterPanels = new HashMap<Integer, JComponent>();
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		for (XTrace t : parent.getExplanator().getLog()) {
			int c = Integer.parseInt(t.getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
			clusters.add(c);
		}
		
		for (int c : clusters) {
			clusterModels.put(c, mineModel(parent.getExplanator().getLog(c)));
			clusterPanels.put(c, new GraphViewPanel(
					buildJGraph(makeGraph(
									0, 
									null,
									null,
									c,
									clusterModels.get(c)
								))));
			tabbedPane.addTab("Cluster: "+c, clusterPanels.get(c));
			
		}
		System.out.println("All cluster models made");
		
		this.add(tabbedPane);	
	}
	
	public ClusterExplainerVisualization getParent() {
		return parent;
	}
	
	public void notifyRuleSelection(int cluster, XTrace trace, Rule[] selectedValue, int newCluster) {
		tabbedPane.removeAll();
		for (int c : clusters) {
			clusterPanels.put(c, new GraphViewPanel(
					buildJGraph(
							makeGraph(
									cluster, 
									trace,
									selectedValue,
									c,
									clusterModels.get(c)
									))));
			String title = "Cluster: "+c;
			if (c == cluster) title += " *current cluster*";
			if (c == newCluster) title += " *new cluster*";
			tabbedPane.addTab(title, clusterPanels.get(c));
		}
		tabbedPane.setSelectedComponent(clusterPanels.get(cluster));
	}

	private DependencyGraph makeGraph(int cluster, XTrace trace,
			Rule[] rulesToShow, int forcluster, DependencyNet net) {
		DependencyGraph graph = DependencyNetToDependencyGraph.toBasicDependencyGraph(net);
		
		if (rulesToShow == null || trace == null)
			return graph;
		
		Map<String, Set<DependencyGraphNode>> labelsToNode = new HashMap<String, Set<DependencyGraphNode>>();
		for (DependencyGraphNode node : graph.getNodes()) {
			String label = net.getLabel(node.getCode());
			if (!labelsToNode.containsKey(label))
				labelsToNode.put(label, new HashSet<DependencyGraphNode>());
			labelsToNode.get(label).add(node);
		}
		
		// Show trace
		boolean flowFitting = true;
		for (int i = 0; i < trace.size() - 1; i++) {
			String a = XLogInfoImpl.STANDARD_CLASSIFIER.getClassIdentity(trace.get(i));
			String b = XLogInfoImpl.STANDARD_CLASSIFIER.getClassIdentity(trace.get(i+1));
			if (!labelsToNode.containsKey(a) || !labelsToNode.containsKey(b))
				flowFitting = false;
		}
		Color edgeColor = flowFitting ? Color.orange : Color.cyan;
				
		for (int i = 0; i < trace.size() - 1; i++) {
			String a = XLogInfoImpl.STANDARD_CLASSIFIER.getClassIdentity(trace.get(i));
			String b = XLogInfoImpl.STANDARD_CLASSIFIER.getClassIdentity(trace.get(i+1));
			if (!labelsToNode.containsKey(a) || !labelsToNode.containsKey(b))
				continue;
			for (DependencyGraphNode start : labelsToNode.get(a)) {
				for (DependencyGraphNode end : labelsToNode.get(b)) {
					// Check if edge is present
					DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> arc = graph.getArc(start, end);
					if (arc != null) {
						arc.getAttributeMap().put(AttributeMap.EDGECOLOR, edgeColor);
					}
				}
			}
		}
		
		// Show rules
		for (Rule rule : rulesToShow) {
			List<Object> params = rule.getParameters();
			if (rule.getClass().equals(Exists.class))  {
				if (!labelsToNode.containsKey(params.get(0).toString())) {
					DependencyGraphNode node = graph.addNode(999, params.get(0).toString());
					node.getAttributeMap().put(AttributeMap.LINEWIDTH, 2f);
					node.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
					node.getAttributeMap().put(AttributeMap.LABEL, rule.toShortString());
					node.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.red);
				} else {
					for (DependencyGraphNode node : labelsToNode.get(params.get(0).toString())) {
						node.getAttributeMap().put(AttributeMap.LINEWIDTH, 2f);
						node.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
						node.getAttributeMap().put(AttributeMap.LABEL, rule.toShortString());
						node.getAttributeMap().put(AttributeMap.STROKECOLOR, Color.green);
					}
				}
			}
			
			if (	   rule.getClass().equals(SometimesDirectlyFollows.class) 
					|| rule.getClass().equals(AlwaysDirectlyFollows.class)
					|| rule.getClass().equals(AlwaysWeaklyFollows.class)
					|| rule.getClass().equals(SometimesWeaklyFollows.class)) {
				if (!labelsToNode.containsKey(params.get(0).toString()))
					continue;
				if (!labelsToNode.containsKey(params.get(1).toString()))
					continue;
				for (DependencyGraphNode start : labelsToNode.get(params.get(0).toString())) {
					for (DependencyGraphNode end : labelsToNode.get(params.get(1).toString())) {
						// Check if edge is present
						DependencyGraphEdge<DependencyGraphNode, DependencyGraphNode> arc = graph.getArc(start, end);
						if (arc == null) { // Green is not present
							arc = graph.addArc(start, end);
							arc.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.green);	
						} else {
							arc.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.red);
						}
						arc.getAttributeMap().put(AttributeMap.LINEWIDTH, 2f);
						arc.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
						arc.getAttributeMap().put(AttributeMap.LABEL, rule.toShortString());
						if (rule.getClass().equals(AlwaysWeaklyFollows.class)
							|| rule.getClass().equals(SometimesWeaklyFollows.class))
							arc.getAttributeMap().put(AttributeMap.DASHPATTERN, new float[] { (float)3.0, (float)3.0 });
					}
				}
			}
		}
		
		return graph;
	}
	
	private DependencyNet mineModel(XLog log) {		
		
		NaieveMiner miner = new NaieveMiner(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		
		return miner.mine();
	}

	private ProMJGraph buildJGraph(@SuppressWarnings("rawtypes") DirectedGraph graph){
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		GraphLayoutConnection layoutConnection = new GraphLayoutConnection(graph);
		@SuppressWarnings("unchecked")
		ProMGraphModel model = new ProMGraphModel(graph);
		ProMJGraph jGraph = new ProMJGraph(model, map, layoutConnection);
		jGraph.setAntiAliased(true);
		jGraph.setDisconnectable(false);
		jGraph.setConnectable(false);
		jGraph.setGridEnabled(false);
		jGraph.setDoubleBuffered(true);
		jGraph.setSelectionEnabled(false);
		jGraph.setMoveBelowZero(false);
		jGraph.setPortsVisible(true);
		jGraph.setPortsScaled(true);
		jGraph.setDragEnabled(false);
		jGraph.setDropEnabled(false);
		for (MouseListener l : jGraph.getMouseListeners()) jGraph.removeMouseListener(l);
		for (MouseMotionListener l : jGraph.getMouseMotionListeners()) jGraph.removeMouseMotionListener(l);
		for (MouseWheelListener l : jGraph.getMouseWheelListeners()) jGraph.removeMouseWheelListener(l);
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);
		layout.setOrientation(map.get(graph, AttributeMap.PREF_ORIENTATION, SwingConstants.WEST));
		if(!layoutConnection.isLayedOut()){
			JGraphFacade facade = new JGraphFacade(jGraph);
			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);
			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
			jGraph.getGraphLayoutCache().edit(nested);
			layoutConnection.setLayedOut(true);
		}
		jGraph.setUpdateLayout(layout);
		layoutConnection.updated();
		return jGraph;
	}
	
}
