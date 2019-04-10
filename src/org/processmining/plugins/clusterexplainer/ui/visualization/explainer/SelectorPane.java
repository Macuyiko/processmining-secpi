package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.rules.Rule;

import de.bwaldvogel.liblinear.Feature;

public class SelectorPane extends JPanel {
	private static final long serialVersionUID = 5144878949710950682L;
	private ClusterExplainerVisualization parent;
	private TraceList traceList;
	private RuleList ruleList;
	private XTrace selectedTrace;
	
	public SelectorPane(ClusterExplainerVisualization parent) {
		this.parent = parent;
		this.selectedTrace = null;
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		this.traceList = new TraceList(this);
		this.ruleList = new RuleList(this);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				traceList, ruleList);
		
		this.add(splitPane);
		
	}
	
	public ClusterExplainerVisualization getParent() {
		return parent;
	}

	public void notifyTraceSelection(XTrace selectedValue) {
		if (selectedValue == null)
			return;
		selectedTrace = selectedValue;
		notifyRuleCalculation();
	}

	public void notifyRuleSelection(Rule[] selectedValue) {
		if (selectedValue == null)
			return;
		if (selectedTrace == null)
			return;
		
		Set<Integer> swapAtts = parent.getExplanator().getAttributeSet(selectedValue);
		Feature[] curInstance = parent.getExplanator().getDataset().getInstance(parent.getExplanator().getInstance(selectedTrace));
		Feature[] newInstance = parent.getExplanator().getDataset().getSwappedLine(curInstance, swapAtts);
		double[][] newCluster = parent.getExplanator().predictMulti(newInstance);
		
		this.parent.notifyRuleSelection(
				parent.getExplanator().getDataset().getOriginalLabel(parent.getExplanator().getInstance(selectedTrace)),
				selectedTrace,
				selectedValue,
				(int)newCluster[0][0]);
	}

	public void notifyRuleCalculation() {
		if (selectedTrace == null)
			return;
		
		List<Rule[]> explanations = parent.getExplanator().getExplanations(parent.getExplanator().getInstance(selectedTrace));
		ruleList.populateList(explanations);
	}

	public void notifyTraceFilter(List<XTrace> filteredTraces, int filterOperation) {
		traceList.populateList(filteredTraces, filterOperation);
	}
	
}
