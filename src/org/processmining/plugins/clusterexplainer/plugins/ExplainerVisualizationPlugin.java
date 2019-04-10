package org.processmining.plugins.clusterexplainer.plugins;

import javax.swing.JComponent;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.ui.visualization.explainer.ClusterExplainerVisualization;

@Plugin(name = "Visualize Cluster Explanations",
		parameterLabels = { "Cluster Explanator" }, 
		returnLabels = { "Cluster Explanation Visualization" },
		returnTypes = { JComponent.class })

@Visualizer
public class ExplainerVisualizationPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(PluginContext context, ClusterExplanator explanator) {
		return new ClusterExplainerVisualization(explanator);
	}
	
}

