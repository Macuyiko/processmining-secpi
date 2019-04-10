package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.result.ClusterExplanator;
import org.processmining.plugins.clusterexplainer.rules.Rule;

public class ClusterExplainerVisualization extends JPanel {
	private static final long serialVersionUID = 5942978755812500062L;
	private ClusterExplanator explanator;
	private ModelTabsPane modelPane;
	private SelectorPane selectorPane;
	private InformationPane infoPane;
	
	public ClusterExplainerVisualization(ClusterExplanator explanator) {
		this.explanator = explanator;
		this.selectorPane = new SelectorPane(this);
		this.modelPane = new ModelTabsPane(this);
		this.infoPane = new InformationPane(this);
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		JSplitPane lowerSplitPlane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				selectorPane, modelPane);
		lowerSplitPlane.setDividerLocation(150);
		
		JSplitPane upperSplitPlane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				lowerSplitPlane, infoPane);
		upperSplitPlane.setDividerLocation(1250);
		
		this.add(upperSplitPlane, BorderLayout.CENTER);
	}

	public ClusterExplanator getExplanator() {
		return explanator;
	}

	public void notifyRuleSelection(int cluster, XTrace trace, Rule[] selectedValue, int newCluster) {
		modelPane.notifyRuleSelection(cluster, trace, selectedValue, newCluster);
		infoPane.notifyRuleSelection(cluster, trace, selectedValue, newCluster);
	}

	public void notifyTraceFilter(List<XTrace> filteredTraces, int filterOperation) {
		selectorPane.notifyTraceFilter(filteredTraces, filterOperation);
	}	

}
