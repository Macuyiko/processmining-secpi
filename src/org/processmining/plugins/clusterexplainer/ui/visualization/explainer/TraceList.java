package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;

public class TraceList extends JPanel {
	private static final long serialVersionUID = 2910872026710950682L;
	private SelectorPane parent;
	private final JList<XTrace> traceList;
	
	private List<XTrace> shownTraces;
	
	public TraceList(SelectorPane selectorPane) {
		this.traceList = new JList<XTrace>();
		this.parent = selectorPane;
		this.shownTraces = new ArrayList<XTrace>();
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		final JTextField traceName = new JTextField();
		this.add(traceName, BorderLayout.NORTH);
		
		final Map<Integer, Color> clusterColors = new HashMap<Integer, Color>();
		
		traceList.setCellRenderer( new DefaultListCellRenderer() {
			private static final long serialVersionUID = 8533444893958243349L;
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {  
	            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
	            int cluster = parent.getParent().getExplanator().getDataset().getOriginalLabel(parent.getParent().getExplanator().getInstance((XTrace) value));
	            if (!clusterColors.containsKey(cluster))
	            	clusterColors.put(cluster, getRandomColor());
	            c.setBackground(clusterColors.get(cluster));
	            String text = XConceptExtension.instance().extractName((XTrace) value);
	            double[][] predict = parent.getParent().getExplanator().predict(parent.getParent().getExplanator().getInstance((XTrace) value));
	            text += " ("+predict[0][0]+" , "+predict[0][1]+")";
	            if (predict[0][0] != 1)
	            	text += " (classification error)";
	            setText(text);
	            return c;  
	        }  
		});
		traceList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				parent.notifyTraceSelection((XTrace) traceList.getSelectedValue());
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane(traceList);
		this.add(scrollPane, BorderLayout.CENTER);
		populateList("");
		
		traceName.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {
				populateList(traceName.getText());
			}
		});
		
	}
	
	private void populateList(final XTrace[] traces) {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	traceList.setListData(traces);
		    }
		});
	}
	
	private void populateList(String filter) {
		List<XTrace> traces = new ArrayList<XTrace>();
		for (XTrace t : parent.getParent().getExplanator().getLog()) {
			String name = XConceptExtension.instance().extractName(t);
			if (name.contains(filter) || filter.equals(""))
				traces.add(t);
		}
		shownTraces = traces;
		populateList(traces.toArray(new XTrace[]{}));
	}
	
	public void populateList(List<XTrace> tracesToFilter, int filterOperation) {
		List<XTrace> traces = new ArrayList<XTrace>();
		for (XTrace t : parent.getParent().getExplanator().getLog()) {
			if (filterOperation == 0 
					|| (filterOperation == 1 && !tracesToFilter.contains(t) && shownTraces.contains(t))
					|| (filterOperation == 2 && tracesToFilter.contains(t)))
				traces.add(t);
		}
		shownTraces = traces;
		populateList(traces.toArray(new XTrace[]{}));
	}
	
	private Color getRandomColor() {
		Random random = new Random();
		float hue = random.nextFloat();
		float saturation = (random.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		return Color.getHSBColor(hue, saturation, luminance);
	}
	
}
