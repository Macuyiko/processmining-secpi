package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingConstants;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.AttributeMap;

@Plugin(name = "Convert Dependency Net to Dependency Graph", 
		parameterLabels = { "Dependency Net" }, 
		returnLabels = {"Dependency Graph" }, 
		returnTypes = { DependencyGraph.class }, 
		userAccessible = true, 
		help = "Converts Dependency Net into Dependency Graph")

public class DependencyNetToDependencyGraph {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, 
			author = "Seppe K.L.M. vanden Broucke", 
			email = "seppe.vandenbroucke@econ.kuleuven.be", 
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Default Settings", requiredParameterLabels = { 0 })
	
	public static DependencyGraph convert(PluginContext context, DependencyNet dn) {
		DependencyGraph returns = toBasicDependencyGraph(dn);
		return returns;
	}

	@SuppressWarnings("unused")
	public static DependencyGraph toBasicDependencyGraph(DependencyNet dependencyNet) {
		DependencyGraph graph = new DependencyGraph();
		graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
		
		for (int t : dependencyNet.getTasks()) {
			String label = "#" + t;
			if (dependencyNet.getLabel(t) != null)
				label += "\n" + dependencyNet.getLabel(t);
			DependencyGraphNode node = graph.addNode(t, label);
		}
		
		int maxTotalInputCount = 0;
		int maxTotalOutputCount = 0;
		int totalInputCount = 0;
		int totalOutputCount = 0;
		Map<Integer, Integer> taskInputCount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> taskOutputCount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> maxTaskInputCount = new HashMap<Integer, Integer>();
		Map<Integer, Integer> maxTaskOutputCount = new HashMap<Integer, Integer>();
		
		for (int t : dependencyNet.getTasks()) {
			taskInputCount.put(t, 0);
			taskOutputCount.put(t, 0);
			maxTaskInputCount.put(t, 0);
			maxTaskOutputCount.put(t, 0);
			for (int s : dependencyNet.getTaskInputs(t)) {
				int addIn = 1;
				taskInputCount.put(t, taskInputCount.get(t) + addIn);
				if (addIn > maxTaskInputCount.get(t))
					maxTaskInputCount.put(t, addIn);
				if (addIn > maxTotalInputCount)
					maxTotalInputCount = addIn;
				totalInputCount += addIn;
				graph.addArc(graph.getNodeByCode(s), graph.getNodeByCode(t));
			}
			for (int u : dependencyNet.getTaskOutputs(t)) {
				int addOut = 1;
				taskOutputCount.put(t, taskOutputCount.get(t) + addOut);
				if (addOut > maxTaskOutputCount.get(t))
					maxTaskOutputCount.put(t, addOut);
				if (addOut > maxTotalOutputCount)
					maxTotalOutputCount = addOut;
				totalOutputCount += addOut;
				graph.addArc(graph.getNodeByCode(t), graph.getNodeByCode(u));
			}
		}
		
		
		
		return graph;
	}

	

}