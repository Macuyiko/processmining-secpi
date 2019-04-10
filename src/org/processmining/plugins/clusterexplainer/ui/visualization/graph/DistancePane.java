package org.processmining.plugins.clusterexplainer.ui.visualization.graph;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.processmining.plugins.clusterexplainer.distance.AbstractDistance.Distance;

public class DistancePane extends JPanel {
	private static final long serialVersionUID = 489799461545721414L;
	private TraceGraphVisualization parent;
	private JRadioButton shared;
	private JRadioButton sharedMin;
	private JRadioButton sharedMax;
	private JRadioButton sharedDistinct;
	private JRadioButton sharedAtts;
	private JTextField cutoff;
	private JTextArea textbox;
	
	public DistancePane(TraceGraphVisualization parent) {
		this.parent = parent;
		init();
	}

	protected void init() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		shared = new JRadioButton("Shared Rules");
		sharedMin = new JRadioButton("Shared Rules / Min");
		sharedMax = new JRadioButton("Shared Rules / Max");
		sharedDistinct = new JRadioButton("Shared Rules / Distinct");
		sharedAtts = new JRadioButton("Shared Attributes Ratio");
		
		ButtonGroup group = new ButtonGroup();
		group.add(shared);
		group.add(sharedMin);
		group.add(sharedMax);
		group.add(sharedDistinct);
		group.add(sharedAtts);
		
		cutoff = new JTextField("0.9");
		cutoff.setMinimumSize(new Dimension(50, 25));
		cutoff.setPreferredSize(new Dimension(50, 25));
		cutoff.setMaximumSize(new Dimension(50, 25));
		
		this.add(new JLabel("Distance metric:"), Component.LEFT_ALIGNMENT);
		this.add(shared, Component.LEFT_ALIGNMENT);
		this.add(sharedMin, Component.LEFT_ALIGNMENT);
		this.add(sharedMax, Component.LEFT_ALIGNMENT);
		this.add(sharedDistinct, Component.LEFT_ALIGNMENT);
		this.add(sharedAtts, Component.LEFT_ALIGNMENT);
		
		this.add(new JLabel("Edge cutoff:"), Component.LEFT_ALIGNMENT);
		this.add(cutoff, Component.LEFT_ALIGNMENT);
		
		JButton buttonRedraw = new JButton("Redraw");
		this.add(buttonRedraw, Component.LEFT_ALIGNMENT);
		
		buttonRedraw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyRedraw(getDistance(), getCutoff());
			}
		});
		
		JButton buttonMine = new JButton("Mine");
		this.add(buttonMine, Component.LEFT_ALIGNMENT);
		
		buttonMine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyMine();
			}
		});
		
		textbox = new JTextArea();
		this.add(new JScrollPane(textbox));
		
		JButton buttonShowSameAttributes = new JButton("Show Shared Attributes");
		this.add(buttonShowSameAttributes, Component.LEFT_ALIGNMENT);
		
		buttonShowSameAttributes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyShowShared();
			}
		});
		
		JButton buttonShowDifferentiatingAttributes = new JButton("Show Differentiating Attributes");
		this.add(buttonShowDifferentiatingAttributes, Component.LEFT_ALIGNMENT);
		
		buttonShowDifferentiatingAttributes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyShowDifferent();
			}
		});
		
		this.add(Box.createVerticalGlue());
		
		
	}

	public void setText(String text) {
		this.textbox.setText(text);
	}
	
	public Distance getDistance() {
		if (shared.isSelected()) {
			return Distance.NUMBER_SHARED;
		} else if (sharedMin.isSelected()) {
			return Distance.SHARED_ON_MIN;
		} else if (sharedMax.isSelected()) {
			return Distance.SHARED_ON_MAX;
		} else if (sharedDistinct.isSelected()) {
			return Distance.SHARED_ON_DISTINCT;
		} else if (sharedAtts.isSelected()) {
			return Distance.SHARED_ATTRIBUTES;
		}
		return Distance.SHARED_ATTRIBUTES;
	}
	
	public double getCutoff() {
		double c;
		try {
			c = Double.parseDouble(cutoff.getText());
		} catch (NumberFormatException e) {
			c = 0;
			cutoff.setText("0.0");
		}
		return c;
	}


}
