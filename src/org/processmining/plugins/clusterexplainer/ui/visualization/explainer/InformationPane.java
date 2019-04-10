package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.rules.Rule;

public class InformationPane extends JPanel {
	private static final long serialVersionUID = 1244559818498915482L;
	private ClusterExplainerVisualization parent;
	
	private List<XTrace> explainedTraces;
	
	private JLabel accuracyLabel, traceHeaderLabel;
	private JList<XTrace> traceList;
	private JButton showAllButton, removeTracesButton, showOnlyButton;
	
	public InformationPane(ClusterExplainerVisualization parent) {
		this.parent = parent;
		this.explainedTraces = new ArrayList<XTrace>();
		init();
	}
	
	private void init() {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		accuracyLabel = new JLabel("Accuracy: "+getParent().getExplanator().getAccuracy());
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		this.add(accuracyLabel, c);
		
		traceHeaderLabel = new JLabel();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		this.add(traceHeaderLabel, c);
		
		traceList = new JList<XTrace>();
		traceList.setCellRenderer( new DefaultListCellRenderer() {
			private static final long serialVersionUID = 433444893958243349L;
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {  
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
				int cluster = parent.getExplanator().getDataset().getOriginalLabel(parent.getExplanator().getInstance((XTrace) value));
				String text = XConceptExtension.instance().extractName((XTrace) value);
				text += " ("+cluster+")";
				setText(text);
				return c;  
	        }  
		});
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1.0;
		JScrollPane scrollPane = new JScrollPane(traceList);
		this.add(scrollPane, c);
		
		c.weightx = c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		
		showAllButton = new JButton("List All Traces");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		this.add(showAllButton, c);
		
		removeTracesButton = new JButton("Remove These Traces");
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		this.add(removeTracesButton, c);
		
		showOnlyButton = new JButton("Show Only These Traces");
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		this.add(showOnlyButton, c);
		
		showAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyTraceFilter(explainedTraces, 0);
			}
		});
		removeTracesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyTraceFilter(explainedTraces, 1);
			}
		});
		showOnlyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyTraceFilter(explainedTraces, 2);
			}
		});
		
		
	}
	
	public ClusterExplainerVisualization getParent() {
		return parent;
	}

	public void notifyRuleSelection(int cluster, XTrace trace, Rule[] selectedValue, int newCluster) {
		explainedTraces.clear();
		
		for (int instance = 0; instance < parent.getExplanator().getDataset().nrInstances(); instance++) {
			List<Rule[]> explanations = parent.getExplanator().getExplanations(instance);
			if (explanationsHasRule(explanations, selectedValue)) {
				XTrace otherTrace = parent.getExplanator().getLog().get(instance);
				explainedTraces.add(otherTrace);
			}
		}
		
		String text = "Other traces explained by this rule";
		text += " (" + explainedTraces.size() + " / " + parent.getExplanator().getDataset().nrInstances() + ")";
		
		traceHeaderLabel.setText(text);
		traceList.setListData(explainedTraces.toArray(new XTrace[]{}));
	}

	private boolean explanationsHasRule(List<Rule[]> explanations, Rule[] toFind) {
		Set<Integer> thisAttributes = parent.getExplanator().getAttributeSet(toFind);
		for (Rule[] other : explanations) {
			Set<Integer> otherAttributes = parent.getExplanator().getAttributeSet(other);
			if (thisAttributes.equals(otherAttributes))
				return true;
		}
		return false;
	}
}
