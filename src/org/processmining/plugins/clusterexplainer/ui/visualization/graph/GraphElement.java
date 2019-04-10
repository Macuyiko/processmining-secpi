package org.processmining.plugins.clusterexplainer.ui.visualization.graph;

import java.util.HashMap;

public abstract class GraphElement extends HashMap<String, Object> {

	private static final long serialVersionUID = 7295012982191058576L;
	private static int idCounter = 0;

	private int id;
	
	public int getId() {
		return id;
	}
	
	public GraphElement() {
		this.id = idCounter++;
	}
	
	public int hashCode() {
		return getId();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphElement other = (GraphElement) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public Object getProperty(String key, Object def) {
		if (this.containsKey(key))
			return this.get(key);
		return def;
	}

}
