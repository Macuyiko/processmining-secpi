package org.processmining.plugins.clusterexplainer.plugins;

import javax.swing.JComponent;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.ui.visualization.graph.TraceGraphVisualization;

@Plugin(name = "Visualize Trace Graph",
		parameterLabels = { "Cluster Explanator" }, 
		returnLabels = { "Trace Graph Visualization" },
		returnTypes = { JComponent.class })

@Visualizer
public class GraphVisualizationPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, ClusterExplanator explanator) {
		return new TraceGraphVisualization(explanator);
	}
	
}

