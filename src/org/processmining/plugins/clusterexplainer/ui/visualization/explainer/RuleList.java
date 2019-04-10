package org.processmining.plugins.clusterexplainer.ui.visualization.explainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.plugins.clusterexplainer.rules.Rule;

public class RuleList extends JPanel {
	private static final long serialVersionUID = 2910872026710950682L;
	private SelectorPane parent;
	private final JList<Rule[]> ruleList;
	private JScrollPane scrollPane;
	
	public RuleList(SelectorPane selectorPane) {
		this.ruleList = new JList<Rule[]>();
		this.parent = selectorPane;
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		ruleList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 5411449690556296545L;
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {  
	            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
	            Rule[] rule = (Rule[]) value;
	            String ruleText = "<html>"+""+rule.length+" rule(s):<br>";
	            for (Rule r : rule) ruleText += "&nbsp;- " + r.toShortString()+"<br>";
	            ruleText += "</html>";
	            setText(ruleText);
	            c.setBackground(index % 2 == 0 ? Color.lightGray : Color.gray);
		        return c;  
	        }  
		});
		ruleList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				parent.notifyRuleSelection((Rule[]) ruleList.getSelectedValue());
			}
			
		});
		scrollPane = new JScrollPane(ruleList);
		populateList(null);
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
	
	public void populateList(List<Rule[]> rules) {
		if (rules == null)
			ruleList.setListData(new Rule[][]{});
		else
			ruleList.setListData(rules.toArray(new Rule[][]{}));
	}
}
