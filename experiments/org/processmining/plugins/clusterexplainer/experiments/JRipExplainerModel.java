package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.rules.JRip;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;


public class JRipExplainerModel extends ExplainerModel {

	protected String[] jRipSettings;
	protected JRip jRipper;
	protected Evaluation evaluation;
	protected Evaluation crossEvaluation;
	protected List<List<Pair<String, Integer>>> modelRules;
	protected List<Integer> modelOutcomes;
	protected int modelDefault;
	
	
	public JRipExplainerModel(XLog clusteredLog, ClusterExplainerSettings explainerSettings) {
		super(clusteredLog, explainerSettings);
		jRipper = new JRip();
		jRipSettings = jRipper.getOptions();
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
			
			jRipper = new JRip();
			jRipper.setOptions(jRipSettings);
			
			crossEvaluation = new Evaluation(newData);
			crossEvaluation.crossValidateModel(jRipper, newData,10,new Random(123456789));			
			jRipper.buildClassifier(newData);
			
			evaluation = new Evaluation(newData);
			evaluation.evaluateModel(jRipper, newData);
			System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
			System.out.println(evaluation.correct());
			
			System.out.println(jRipper.toString());
			
			String output = jRipper.toString();
			String[] outputLines = output.split("\n");
			
			modelRules = new ArrayList<List<Pair<String,Integer>>>();
			modelOutcomes = new ArrayList<Integer>();
			
			Pattern pLabel = Pattern.compile("label=(\\d+)", Pattern.DOTALL);
			
			for (String line : outputLines) {
				try {
					if (!line.contains("label"))
						continue;
					List<Pair<String, Integer>> rule = new ArrayList<Pair<String, Integer>>();
					String[] ruleParts = line.split(" => ");

					Matcher mLabel = pLabel.matcher(ruleParts[1].trim());
					mLabel.find();
					int label = Integer.parseInt(mLabel.group(1));

					if (ruleParts[0].trim().equals("")) {
						modelDefault = label;
						continue;
					}

					String[] conjunctions = ruleParts[0].split("\\) and \\(");
					for (int l = 0; l < conjunctions.length; l++) {
						String conjunction = conjunctions[l];
						if (l > 0)
							conjunction = "("+conjunction;
						if (l < conjunctions.length-1)
							conjunction = conjunction+")";
						conjunction = conjunction.trim().substring(1,
								conjunction.trim().length() - 1);
						conjunction = conjunction
								.replace(" >= ", " == ")
								.replace(" <= ", " == ");
						String[] conjunctionPieces = conjunction.split("==");
						int value = Integer.parseInt(conjunctionPieces[1]
								.trim());
						String att = conjunctionPieces[0].trim();
						rule.add(new Pair<String, Integer>(att, value));
					}
					modelRules.add(rule);
					modelOutcomes.add(label);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(line);
					System.exit(1);

				}
			}
			
			
			for (int i = 0; i < modelRules.size(); i++) {
				for (Pair<String, Integer> p : modelRules.get(i)) {
					System.out.print(p.getFirst()+" == "+p.getSecond()+" AND ");
				}
				System.out.println(" ==> "+modelOutcomes.get(i));
			}
			System.out.println(" ==> "+modelDefault);
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
		int explanation = getExplanation(instance);
		int prediction = (explanation == -1) ? modelDefault : modelOutcomes.get(explanation);
		return (prediction == dataset.getOriginalLabel(instance));
	}
	
	private int getExplanation(int inst) {
		for (int i = 0; i < modelRules.size(); i++) {
			boolean match = true;
			for (Pair<String, Integer> p : modelRules.get(i)) {
				int findex = getFeatureIndex(p.getFirst());
				if ((int) dataset.getFeature(findex, inst).getValue() != p.getSecond()) {
					match = false;
					break;
				}

			}
			if (match) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public double getInstanceSize(int traceIndex) {
		int explanation = getExplanation(traceIndex);
		int countUpAndWith = (explanation == -1) ? modelRules.size()-1 : explanation;
		Set<String> attributesChecked = new HashSet<String>();
		for (int i = 0; i <= countUpAndWith; i++) {
			List<Pair<String, Integer>> rule = modelRules.get(i);
			for (Pair<String, Integer> r : rule)
				attributesChecked.add(r.getFirst());
		}
		return attributesChecked.size();
	}

	public ClusterExplainerSettings getExplainerParameters() {
		return explainerSettings;
	}

	public String[] getjRipParameters() {
		return jRipSettings;
	}

	public void setjRipParameterss(String[] jRipSettings) {
		this.jRipSettings = jRipSettings;
	}

	@Override
	public double[] getExtraInfo() {
double[] result={0,0,0};
result[2]=jRipper.getRuleset().size();
return result;
	}

}
