package org.processmining.plugins.clusterexplainer.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XTrace;

public abstract class AbstractRule implements Rule {
	protected List<Object> parameters;
	protected XEventClassifier classifier;
	
	public static enum Rule {
		EXISTS,
		FOLLOWS_DIRECT_SOMETIMES,
		FOLLOWS_DIRECT_ALWAYS,
		FOLLOWS_WEAK_SOMETIMES,
		FOLLOWS_WEAK_ALWAYS,
	};
	
	public static AbstractRule create(Rule type) {
		switch (type) {
		case EXISTS:
			return new Exists();
		case FOLLOWS_DIRECT_SOMETIMES:
			return new SometimesDirectlyFollows();
		case FOLLOWS_DIRECT_ALWAYS:
			return new AlwaysDirectlyFollows();
		case FOLLOWS_WEAK_SOMETIMES:
			return new SometimesWeaklyFollows();
		case FOLLOWS_WEAK_ALWAYS:
			return new AlwaysWeaklyFollows();
		default:
			break;
		}
		return null;
	}
	
	public AbstractRule() {
		classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		AbstractRule other = (AbstractRule) obj;
		if (classifier == null && other.classifier != null)
			return false;
		else if (!classifier.equals(other.classifier))
			return false;
		
		if (parameters == null && other.parameters != null)
			return false;
		else if (!parameters.equals(other.parameters))
			return false;
		
		return true;
	}

	public String toString() {
		return "Abstract rule";
	}
	
	public String toShortString() {
		return "Rule()";
	}
	
	public abstract boolean getValue(XTrace trace);
	
	public List<Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Object[] parameters) {
		this.parameters = new ArrayList<Object>(Arrays.asList(parameters));;
	}

	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	
}
