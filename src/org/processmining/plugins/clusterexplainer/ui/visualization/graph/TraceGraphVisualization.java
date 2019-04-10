package org.processmining.plugins.clusterexplainer.ui.visualization.graph;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.clusterexplainer.distance.AbstractDistance.Distance;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyGraph;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyNet;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.DependencyNetToDependencyGraph;
import org.processmining.plugins.clusterexplainer.ui.visualization.miner.NaieveMiner;
import org.processmining.plugins.kutoolbox.visualizators.GraphViewPanel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class TraceGraphVisualization extends JPanel {
	private static final long serialVersionUID = 1245676897894547111L;
	private ClusterExplanator explanator;
	private GraphPane graphPane;
	private DistancePane distancePane;
	
	private Set<Integer> temporaryInstanceSet = null;
	
	public TraceGraphVisualization(ClusterExplanator explanator) {
		this.explanator = explanator;
		this.graphPane = new GraphPane(this);
		this.distancePane = new DistancePane(this);
		
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		JSplitPane lowerSplitPlane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				graphPane, distancePane);
		lowerSplitPlane.setDividerLocation(1250);
		
		this.add(lowerSplitPlane, BorderLayout.CENTER);
	}
	

	public ClusterExplanator getExplanator() {
		return explanator;
	}
	
	public void notifyRedraw(Distance distance, double cutoff) {
		graphPane.setDistance(distance);
		graphPane.setCutoff(cutoff);
		graphPane.redrawGraph();
	}

	public void notifyMine() {
		XLog log = this.graphPane.logFromSelectedNodes();
		if (log.size() == 0)
			return;
		
		System.out.println("Mining log with size: "+log.size());
		NaieveMiner miner = new NaieveMiner(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		
		DependencyNet net = miner.mine();
		
		DependencyGraph graph = DependencyNetToDependencyGraph.toBasicDependencyGraph(net);
		GraphViewPanel graphPanel = new GraphViewPanel(buildJGraph(graph));
		JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(graphPanel);
        f.pack();
        f.setVisible(true);
	}
	
	private Set<Integer> getAttributesWithZeroVariance(Set<Integer> instancesToCheck, double desiredValue) {
		Set<Integer> attributes = new HashSet<Integer>();
		for (int a = 0; a < this.getExplanator().getDataset().nrFeatures(); a++) {
			boolean isEverywhere = true;
			for (int i : instancesToCheck) {
				double value = this.getExplanator().getDataset().getFeature(a, i).getValue();
				if (value != desiredValue) {
					isEverywhere = false;
					break;
				}
			}
			if (isEverywhere)
				attributes.add(a);
		}
		return attributes;
	}
	
	public void notifyShowShared() {
		Set<Integer> instanceSet = this.graphPane.instanceSetFromSelectedNodes();
		if (instanceSet.size() == 0) {
			this.distancePane.setText("Please select a group of nodes first");
			return;
		}
		
		Set<Integer> attributesWithVal1 = this.getAttributesWithZeroVariance(instanceSet, 1d);
		Set<Integer> attributesWithVal0 = this.getAttributesWithZeroVariance(instanceSet, 0d);
		
		String text = "";
		text += "Attributes which hold for all:\n";
		for (int a : attributesWithVal1) {
			text += "- "+this.getExplanator().getDataset().getRule(a).toShortString()+"\n";
		}
		text += "\nAttributes which are false for all:\n";
		for (int a : attributesWithVal0) {
			text += "- "+this.getExplanator().getDataset().getRule(a).toShortString()+"\n";
		}
		this.distancePane.setText(text);
	}
	
	public void notifyShowDifferent() {
		Set<Integer> instanceSet = this.graphPane.instanceSetFromSelectedNodes();
		
		if (instanceSet.size() == 0) {
			this.distancePane.setText("Please select a group of nodes first");
			return;
		}
		
		if (this.temporaryInstanceSet == null) {
			temporaryInstanceSet = this.graphPane.instanceSetFromSelectedNodes();
			this.distancePane.setText("Select the second group of nodes you're interested in and press button again");
			return;
		}
		
		Set<Integer> attributesWithVal1First = this.getAttributesWithZeroVariance(temporaryInstanceSet, 1d);
		Set<Integer> attributesWithVal0First = this.getAttributesWithZeroVariance(temporaryInstanceSet, 0d);
		Set<Integer> attributesWithVal1Second = this.getAttributesWithZeroVariance(instanceSet, 1d);
		Set<Integer> attributesWithVal0Second = this.getAttributesWithZeroVariance(instanceSet, 0d);
		
		String text = "";
		text += "Attributes which differ:\n";
		for (int a : attributesWithVal1First) {
			if (!attributesWithVal0Second.contains(a))
				text += "- "+this.getExplanator().getDataset().getRule(a).toShortString()
					+ " holds for all first and none second\n";
		}
		text += "\n";
		for (int a : attributesWithVal0First) {
			if (!attributesWithVal1Second.contains(a))
				text += "- "+this.getExplanator().getDataset().getRule(a).toShortString()
					+ " holds for all second and none first\n";
		}
		
		this.distancePane.setText(text);
		this.temporaryInstanceSet = null;
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
