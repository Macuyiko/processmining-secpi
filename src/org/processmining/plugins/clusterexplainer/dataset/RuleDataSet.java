package org.processmining.plugins.clusterexplainer.dataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.clusterexplainer.rules.Rule;
import au.com.bytecode.opencsv.CSVWriter;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

/*
 * Important: Feature indexes start counting from 1, whereas our attribute
 * counter starts from 0, as is common. Therefore, do NOT forget to perform
 * -/+ 1 translation when comparing an attribute index with getIndex()! 
 */
public class RuleDataSet {
	private List<Rule> headers;
	private List<Integer> originalLabels;
	private List<Integer> labels;
	private List<Feature[]> features;
	
	public RuleDataSet(List<Rule> headers) {
		this.headers = headers;
		this.labels = new ArrayList<Integer>();
		this.originalLabels = new ArrayList<Integer>();
		this.features = new ArrayList<Feature[]>();
	}
	
	public Rule getRule(int feature) {
		return headers.get(feature);
	}
	
	public int getRuleIndex(Rule rule) {
		return headers.indexOf(rule);
	}
	
	public Feature[] getSwappedLine(Feature[] line, Set<Integer> attributes) {
		Set<Integer> atts = new HashSet<Integer>(attributes);
		List<Feature> newFeature = new ArrayList<Feature>();
		
		for (int i = 0; i < line.length; i++) {
			if (atts.contains(line[i].getIndex()-1)) {
				atts.remove(line[i].getIndex()-1);
				if (line[i].getValue() == 0D)
					newFeature.add(new FeatureNode(line[i].getIndex(), 1D));
				//else
				//	newFeature.add(new FeatureNode(line[i].getIndex(), 0D));
			} else {
				newFeature.add(new FeatureNode(line[i].getIndex(), line[i].getValue()));
			}
		}
		
		for (int r : atts) {
			newFeature.add(new FeatureNode(r+1, 1D));
		}
		
		return newFeature.toArray(new Feature[]{});
	}
	
	public Feature getFeature(int index, Feature[] list) {
		for (int i = 0; i < list.length; i++) {
			if (list[i].getIndex()-1 == index)
				return list[i];
		}
		return new FeatureNode(index+1, 0D);
	}
	
	public Feature getFeature(int index, int instance) {
		if (index < 0)
			throw new IllegalArgumentException();
		return getFeature(index, getInstance(instance));
	}
	
	public Feature[] getInstance(int nr) {
		return features.get(nr);
	}
	
	public int getOriginalLabel(int nr) {
		return originalLabels.get(nr);
	}
	
	public int getLabel(int nr) {
		return labels.get(nr);
	}
	
	public void setLabel(int nr, int label) {
		labels.set(nr, label);
	}
	
	public void addLine(Feature[] attributes, int label) {
		int lastIndex = -1;
		//Feature[] attributesIndexed = new Feature[attributes.length+1];
		// Dummy attribute with index 0 and incremening id -- may be necessary?
		//attributesIndexed[0] = new FeatureNode(0, features.size()+1);
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].getIndex() <= 0)
				throw new IllegalArgumentException(attributes[i].getIndex() + " <= 0");
			if (attributes[i].getIndex() > nrFeatures())
				throw new IllegalArgumentException(attributes[i].getIndex() + " > " + nrFeatures());
			if (attributes[i].getValue() != 0D && attributes[i].getValue() != 1D)
				throw new IllegalArgumentException(attributes[i].getValue() + " != 0 | 1");
			if (attributes[i].getIndex() <= lastIndex)
				throw new IllegalArgumentException(attributes[i].getIndex() + " <= " + lastIndex);
			lastIndex = attributes[i].getIndex();
			//attributesIndexed[i+1] = attributes[i];
		}
		this.labels.add(label);
		this.originalLabels.add(label);
		this.features.add(attributes);
	}
	
	public void addLine(List<Double> attributes, int label) {
		List<Feature> atts = new ArrayList<Feature>();
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i) == 0D) continue;
			atts.add(new FeatureNode(i+1, attributes.get(i)));
		}
		addLine(atts.toArray(new Feature[]{}), label);
	}

	public void addLine(double[] attributes, int label) {
		List<Feature> atts = new ArrayList<Feature>();
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i] == 0D) continue;
			atts.add(new FeatureNode(i+1, attributes[i]));
		}
		addLine(atts.toArray(new Feature[]{}), label);
	}

	public void removeLine(int index) {
		this.labels.remove(index);
		this.originalLabels.remove(index);
		this.features.remove(index);
	}
	
	public int nrInstances() {
		return features.size();
	}
	
	public int nrFeatures() {
		return headers.size();
	}

	public List<Rule> getRules() {
		return headers;
	}

	public Feature[][] getFeatures() {
		Feature[][] features = new Feature[nrInstances()][];
		for (int i = 0; i < nrInstances(); i++)
			features[i] = getInstance(i);
		return features;
	}
	
	public double[] getLabels() {
		double[] labels = new double[nrInstances()];
		for (int i = 0; i < nrInstances(); i++)
			labels[i] = getLabel(i);
		return labels;
	}
	
	public void binarizeLabels(int trueOriginalLabel) {
		for (int j = 0; j < nrInstances(); j++) {
			setLabel(j, originalLabels.get(j) == trueOriginalLabel ? 1 : 0);
		}
	}
	
	public void resetLabels() {
		for (int j = 0; j < nrInstances(); j++) {
			setLabel(j, originalLabels.get(j));
		}
	}
	
	public void writeCSV(File destination) throws IOException {
		resetLabels();
		
		CSVWriter writer = new CSVWriter(new FileWriter(destination));
		String[] line = new String[nrFeatures()+1];
		line[0] = "label";
		for (int i = 0; i < nrFeatures(); i++)
			line[i+1] = getRule(i).toShortString();
		writer.writeNext(line);
		for (int j = 0; j < nrInstances(); j++) {
			line[0] = ""+getOriginalLabel(j);
			for (int i = 0; i < nrFeatures(); i++)
				line[i+1] = ""+getFeature(i, j).getValue();
			writer.writeNext(line);
		}
		writer.close();
	}
	
}
