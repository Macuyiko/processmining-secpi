package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;


public class J48ExplainerModel extends ExplainerModel {

	protected String[] j48Settings;
	protected J48 j48;
	protected Evaluation evaluation;
	protected Evaluation crossEvaluation;
	private Map<Integer, Map<Pair<String,Integer>,Integer>> modelRules;
	private Map<Integer, Integer> modelOutcomes;
	
	
	
	
	public J48ExplainerModel(XLog clusteredLog, ClusterExplainerSettings explainerSettings) {
		super(clusteredLog, explainerSettings);
		j48 = new J48();
		j48Settings = j48.getOptions();
	}
	
	@Override
	public void constructDataset(RuleDataSet dataset) {
		super.constructDataset(dataset);
		
		
		File temp;
		try {
			temp = File.createTempFile("temp",".csv");
			dataset.writeCSV(temp);
			DataSource source = new DataSource(temp.getAbsolutePath());
			Instances data = source.getDataSet();
			data.setClassIndex(0);
			
			NumericToNominal ntn = new NumericToNominal();
			ntn.setAttributeIndices("first");
			ntn.setInputFormat(data);
			Instances newData = Filter.useFilter(data, ntn);
			newData.setClassIndex(0);
			
			j48 = new J48();
			j48.setOptions(j48Settings);
			
			
			crossEvaluation = new Evaluation(newData);
			crossEvaluation.crossValidateModel(j48, newData,10,new Random(123456789));
			
			
			j48.buildClassifier(newData);
			evaluation = new Evaluation(newData);
			evaluation.evaluateModel(j48, newData);
			
			System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
			System.out.println(evaluation.correct());
			
			System.out.println(j48.toString());
			
			String output = j48.toString();
			String[] outputLines = output.split("\n");
			
			modelRules = new HashMap<Integer, Map<Pair<String,Integer>,Integer> >();
			modelOutcomes = new HashMap<Integer, Integer>();
			Map<Integer, Integer> levelsToId = new HashMap<Integer, Integer>();
			levelsToId.put(0, -1);
			int newId = 0;
			
			for (String line : outputLines) {
				if (!line.contains(" <= ") && !line.contains(" > "))
					continue;
				String[] ruleParts = line.split(": ");
				int depth = ruleParts[0].split("\\|", -1).length-1;
				String conjunction = ruleParts[0].replace("<= 0", "== 0").replace("> 0", "== 1");
				conjunction = conjunction.replace("|", "").trim();
				int value = conjunction.contains("== 0") ? 0 : 1;
				String att = conjunction.replace("== 0", "").replace("== 1", "").trim();
				int parent = levelsToId.get(depth);
				if (!modelRules.containsKey(parent))
					modelRules.put(parent, new HashMap<Pair<String,Integer>,Integer>());
				modelRules.get(parent).put(new Pair<String,Integer>(att, value), newId);

				if (ruleParts.length == 2) {
					int label = Integer.parseInt(ruleParts[1].trim().split(" ", -1)[0].trim());
					modelOutcomes.put(newId, label);
				}
				
				levelsToId.put(depth+1, newId);
				newId++;
			}
			
			for (Entry<Integer, Map<Pair<String, Integer>, Integer>> e : modelRules.entrySet()) {
				for (Entry<Pair<String, Integer>, Integer> sr : e.getValue().entrySet()) {
					System.out.print(e.getKey()+" : "+sr.getKey().getFirst()+" == "+sr.getKey().getSecond()+" ("+sr.getValue()+") ");
					if (modelOutcomes.containsKey(sr.getValue()))
						System.out.print(" ==> "+modelOutcomes.get(sr.getValue()));
					System.out.println("");
				}
				System.out.println("");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public double getAccuracy() {
		//return evaluation.correct() / (double) clusteredLog.size();
		return crossEvaluation.correct() / (double) clusteredLog.size();

		

	}
	
	@Override
	public double getManualAccuracy() {
		double total = 0;
		for (int i = 0; i < clusteredLog.size(); i++) {
			if (isCorrectlyClassified(i))
				total+=1;
		}
		return total / (double) clusteredLog.size();
	}
	
	@Override
	public boolean isCorrectlyClassified(int instance) {
		Pair<List<Integer>, Integer> explanation = getExplanation(instance);
		if (explanation== null) return false;
		int prediction = explanation.getSecond();
		return (prediction == dataset.getOriginalLabel(instance));
	}

	private Pair<List<Integer>,Integer> getExplanation(int inst) {
		List<Integer> trail = new ArrayList<Integer>();
		trail.add(-1);
		
		while (true) {
			Map<Pair<String, Integer>, Integer> rulesToCheck = modelRules.get(trail.get(trail.size()-1));
			if (rulesToCheck==null) return null;
			for (Entry<Pair<String, Integer>, Integer> entry : rulesToCheck.entrySet()) {
				int findex = getFeatureIndex(entry.getKey().getFirst());
				int fvalue = (int) dataset.getFeature(findex, inst).getValue();
				if (fvalue == entry.getKey().getSecond()) {
					trail.add(entry.getValue());
					if (modelOutcomes.containsKey(entry.getValue()))
						return new Pair<List<Integer>,Integer>(trail, modelOutcomes.get(entry.getValue()));
					break;
				}
			}
		}
	}
	
	@Override
	public double getInstanceSize(int traceIndex) {
		Pair<List<Integer>, Integer> explanation = getExplanation(traceIndex);
		return explanation.getFirst().size();
	}

	public ClusterExplainerSettings getExplainerParameters() {
		return explainerSettings;
	}

	public String[] getj48Parameters() {
		return j48Settings;
	}

	public void setj48Parameterss(String[] j48Settings) {
		this.j48Settings = j48Settings;
	}

	@Override
	public double[] getExtraInfo() {
		double[] result={0,0,0};
		result[0]=j48.measureNumLeaves();
		result[1]= j48.measureTreeSize();
return result;
	}

}
