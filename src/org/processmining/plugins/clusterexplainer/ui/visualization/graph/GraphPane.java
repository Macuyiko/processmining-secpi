package org.processmining.plugins.clusterexplainer.ui.visualization.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.distance.AbstractDistance;
import org.processmining.plugins.clusterexplainer.distance.AbstractDistance.Distance;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphPane extends JPanel {
	private static final long serialVersionUID = 3643382181562717361L;
	private TraceGraphVisualization parent;
	private Set<Integer> clusters;
	private Map<Integer, Color> clusterColors;
	private Map<Integer, GroupedXLog> clusterLogs;
	private GraphComponent graphComponent;
	private AbstractDistance distance;
	private double cutoffThreshold;
	
	private List<GraphVertex> graphVertices;
	private Set<GraphEdge> graphEdges;
	
	public GraphPane(TraceGraphVisualization parent) {
		this.parent = parent;
		this.distance = AbstractDistance.create(parent.getExplanator(), Distance.SHARED_ATTRIBUTES);
		this.cutoffThreshold = 0.9;
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		clusters = parent.getExplanator().getClusterIndices();
		clusterColors = new HashMap<Integer, Color>();
		clusterLogs = new HashMap<Integer, GroupedXLog>();
		
		for (int cluster : clusters) {
			clusterColors.put(cluster, getRandomColor());
			clusterLogs.put(cluster, new GroupedXLog(parent.getExplanator().getLog(cluster)));
		}
		
		cacheGraphVertices();
		cacheGraphEdges();
	    redrawGraph();
	}
	
	public void setDistance(Distance type) {
		if (!distance.getType().equals(type)) {
			cacheGraphEdges();
		}
		this.distance = AbstractDistance.create(parent.getExplanator(), type);
	}
	
	public void setCutoff(double value) {
		this.cutoffThreshold = value;
	}
	
	public void cacheGraphVertices() {
		graphVertices = new ArrayList<GraphVertex>();
		
		for (int cluster : clusters) {
	    	for (int traceCounter = 0; traceCounter < clusterLogs.get(cluster).size(); traceCounter++) {
	    		XTrace trace = clusterLogs.get(cluster).get(traceCounter).get(0);
	    		String traceName = XConceptExtension.instance().extractName(trace);
	    		GraphVertex vertex = new GraphVertex();
	    		vertex.put("fillcolor", clusterColors.get(cluster));
	    		vertex.put("label", traceName+" ("+cluster+")");
	    		vertex.put("_cluster", cluster);
	    		vertex.put("_traceIndex", traceCounter);
	    		vertex.put("_trace", trace);
	    		vertex.put("_instance", parent.getExplanator().getInstance(trace));
	    		graphVertices.add(vertex);
	    	}
	    }
	}
	
	public void cacheGraphEdges() {
		graphEdges = new HashSet<GraphEdge>();
		
		for (int i = 0; i < graphVertices.size(); i++) {
			for (int j = i+1; j < graphVertices.size(); j++) {
				GraphVertex v1 = graphVertices.get(i);
				GraphVertex v2 = graphVertices.get(j);
				XTrace t1 = (XTrace) v1.get("_trace");
				XTrace t2 = (XTrace) v2.get("_trace");
	    		int cluster1 = parent.getExplanator().getDataset().getOriginalLabel(parent.getExplanator().getInstance(t1));
	    		int cluster2 = parent.getExplanator().getDataset().getOriginalLabel(parent.getExplanator().getInstance(t2));
	    		
	    		double d = distance.getDistance(t1, t2, true);
	    		GraphEdge edge = new GraphEdge();
    			//edge.put("fillcolor", blendColors(clusterColors.get(cluster1), clusterColors.get(cluster2)));
    			edge.put("color", blendColors(clusterColors.get(cluster1), clusterColors.get(cluster2)));
    			//edge.put("label", ""+d);
    			edge.put("weight", (float) d);
    			edge.put("stroke", new BasicStroke(2.5f));
    			edge.put("_distance", d);
    			edge.put("_startVertex", v1);
    			edge.put("_endVertex", v2);
    			graphEdges.add(edge);
			}
		}
	}
	
	public void redrawGraph() {
		if (graphComponent != null)
			this.remove(graphComponent);
		
		Graph<GraphVertex, GraphEdge> graph = new UndirectedSparseGraph<GraphVertex, GraphEdge>();
		
		for (GraphVertex vertex : graphVertices) {
			graph.addVertex(vertex);
		}
		
		for (GraphEdge edge : graphEdges) {
    		if ((Double) edge.get("_distance") >= this.cutoffThreshold) {
    			graph.addEdge(edge, (GraphVertex) edge.get("_startVertex"), (GraphVertex) edge.get("_endVertex"));
    		}
		}
		 
		graphComponent = new GraphComponent(graph);
		
        this.add(graphComponent, BorderLayout.CENTER);	
        this.revalidate();
        this.repaint();
        graphComponent.repaint();
	}
	
	public XLog logFromSelectedNodes() {
		XFactory factory = new XFactoryBufferedImpl();
		XLog log = factory.createLog();
		for (GraphVertex v : graphComponent.getSelectedVertices()) {
			log.addAll(getTraces((Integer) v.get("_cluster"), (Integer) v.get("_traceIndex")));
		}
		return log;
	}
	
	public Set<Integer> instanceSetFromSelectedNodes() {
		Set<Integer> set = new HashSet<Integer>();
		for (GraphVertex v : graphComponent.getSelectedVertices()) {
			set.add((Integer) v.get("_instance"));
		}
		return set;
	}
	
	private List<XTrace> getTraces(int cluster, int traceIndex) {
		return clusterLogs.get(cluster).get(traceIndex);
	}
	
	private Color getRandomColor() {
		Random random = new Random();
		float hue = random.nextFloat();
		float saturation = (random.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color hsb = Color.getHSBColor(hue, saturation, luminance);
		return new Color((float)hsb.getRed()/255f,
				(float)hsb.getGreen()/255f,
				(float)hsb.getBlue()/255f,
				.8f);
				
	}
	
	private Color blendColors(Color color1, Color color2) {
		float r = (float) .5;
		float ir = (float) 1.0 - r;

		float rgb1[] = new float[3];
		float rgb2[] = new float[3];

		color1.getColorComponents(rgb1);
		color2.getColorComponents(rgb2);

		Color color = new Color(
				rgb1[0] * r + rgb2[0] * ir, 
				rgb1[1] * r + rgb2[1] * ir,
				rgb1[2] * r + rgb2[2] * ir,
				.5f);
		
		return color;
	}
}
