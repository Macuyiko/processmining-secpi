package org.processmining.plugins.clusterexplainer.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.rules.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class ClusterExplanator {
	
	private XLog log;
	private RuleDataSet dataset;
	private ClusterExplainerSettings settings;
	private Map<Integer, Model> clusterModel;
	private Map<Integer, List<Rule[]>> cachedRules;
	
	public ClusterExplanator(XLog log, RuleDataSet dataSet,
			ClusterExplainerSettings settings) {
		this.log = log;
		this.dataset = dataSet;
		this.settings = settings;
		this.clusterModel = new HashMap<Integer, Model>();
		this.cachedRules = new HashMap<Integer, List<Rule[]>>();
	}
	
	public XLog getLog() {
		return log;
	}

	public RuleDataSet getDataset() {
		return dataset;
	}

	public ClusterExplainerSettings getSettings() {
		return settings;
	}

	public XLog getLog(int cluster) {
		XLog slicedLog = LogUtils.newLog("Log for cluster: "+cluster);
		for (int i = 0; i < log.size(); i++){
			int nr = Integer.parseInt(log.get(i).getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
			if (nr == cluster) {
				slicedLog.add((XTrace) log.get(i).clone());
			}
		}
		
		return slicedLog;
	}
	
	public void resetLabels() {
		for (int i = 0; i < log.size(); i++){
			int cluster = Integer.parseInt(log.get(i).getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
			dataset.setLabel(i, cluster);
		}
	}
	
	public double getAccuracy() {
		double correct = 0;
		for (int i = 0; i < dataset.nrInstances(); i++) {
			if (!settings.useMultiClassExplanation && (int) predict(i)[0][0] == 1)
				correct++;
			if (settings.useMultiClassExplanation && (int) predictMulti(i)[0][0] == dataset.getOriginalLabel(i))
				correct++;
		}
		
		return correct / (double) dataset.nrInstances();
	}
	
	public double getCVAccuracy() {
		double[] accPerCluster= new double[clusterModel.size()];
		for(int cluster=0; cluster < clusterModel.size(); cluster++ ) {
			//System.out.println("Training model for: "+cluster);
			dataset.binarizeLabels(cluster);
				
			Problem problem = new Problem();
			problem.l = dataset.nrInstances();	// number of training examples
			problem.n = dataset.nrFeatures();	// number of features
			problem.x = dataset.getFeatures();	// feature nodes
			problem.y = dataset.getLabels();	// target values

			SolverType solver = settings.solver; // solver type
			double C = settings.c;		// cost of constraints violation
			double eps = settings.e;	// stopping criteria
			double p = settings.p;		// loss function eps
			Parameter parameter = new Parameter(solver, C, eps, p);
			
			//Model model = Linear.train(problem, parameter);
			double[] target= new double[problem.l];
			 Linear.crossValidation(problem, parameter, 10, target);
			    int total_correct = 0;
			   for (int i = 0; i < problem.l; i++) {
			   if (target[i] == problem.y[i]) total_correct++;
		}
			 accPerCluster[cluster]=  Double.valueOf(1.0D * total_correct / problem.l);
		}
		double result= 0;
		for (double acc:accPerCluster){
			result= result+ acc;
		}
		return result/ (double) clusterModel.size();
	}
	
	public void train(int cluster) {
		//System.out.println("Training model for: "+cluster);
		dataset.binarizeLabels(cluster);
			
		Problem problem = new Problem();
		problem.l = dataset.nrInstances();	// number of training examples
		problem.n = dataset.nrFeatures();	// number of features
		problem.x = dataset.getFeatures();	// feature nodes
		problem.y = dataset.getLabels();	// target values

		SolverType solver = settings.solver; // solver type
		double C = settings.c;		// cost of constraints violation
		double eps = settings.e;	// stopping criteria
		double p = settings.p;		// loss function eps
		Parameter parameter = new Parameter(solver, C, eps, p);
		
		Model model = Linear.train(problem, parameter);
		clusterModel.put(cluster, model);
	}
	
	public Set<Integer> getClusterIndices() {
		return clusterModel.keySet();
	}
	
	public double[][] predictMulti(Feature[] instance) {
		//System.out.println("Performing winner takes all multi class prediction");
		
		double bestp = 0;
		int bestc = 0;
		
		for (int cluster : clusterModel.keySet()) {
			Model model = clusterModel.get(cluster);
			
			double[] decs = new double[model.getNrClass()];
			double label = Linear.predictProbability(model, instance, decs);
			double p1 = max(decs);
			//System.out.println("Cluster "+cluster+" got label "+label+" with prob "+p1);
			if (label == 0)
				p1 = 1d - p1;
			if (p1 > bestp) {
				//System.out.println("New best "+p1);
				bestc = cluster;
				bestp = p1;
			}
			
		}
		return new double[][] {new double[]{bestc, bestp}, new double[]{}};
	}
	
	public double[][] predictMulti(int instance) {
		return predictMulti(dataset.getInstance(instance));
	}
	
	public double[][] predict(int cluster, Feature[] instance) {
		if (!clusterModel.containsKey(cluster))
			train(cluster);
		
		Model model = clusterModel.get(cluster);

		double[] decs = new double[model.getNrClass()];
		double label = Linear.predictProbability(model, instance, decs);
		return new double[][] {new double[]{label, max(decs)}, decs};
	}
	
	public double[][] predict(int instance) {
		return predict(dataset.getOriginalLabel(instance), dataset.getInstance(instance));
	}
	
	private double max(double[] arr) {
		int cur = -1;
		for (int i = 0; i < arr.length; i++) {
			if (cur == -1 || arr[i] > arr[cur])
				cur = i;
		}
		return arr[cur];
	}
	
	public int getInstance(XTrace trace) {
		for (int i = 0; i < log.size(); i++){
			if (log.get(i).equals(trace))
				return i;
		}
		return -1;
	}
	
	public List<Rule[]> getExplanations(int instance) {
		if (cachedRules.containsKey(instance))
			return cachedRules.get(instance);
		
		int iterations = settings.iterations;
		List<Rule[]> rules = new ArrayList<Rule[]>();
		Feature[] originalFeatures = dataset.getInstance(instance);
		int originalCluster = dataset.getOriginalLabel(instance);
		
		List<Set<Integer>> chosenCombinations = new ArrayList<Set<Integer>>();
		List<Set<Integer>> expandCombinations = new ArrayList<Set<Integer>>();
		List<Double> expandProbabilities = new ArrayList<Double>();
		List<Set<Integer>> expandedCombinations = new ArrayList<Set<Integer>>();
		List<Double> getBestComboCache = new ArrayList<Double>();
		
		double[][] predict = settings.useMultiClassExplanation ? 
				predictMulti(originalFeatures) : predict(originalCluster, originalFeatures);
		Integer[] attributes_to_try = getCandidateFeatures(instance);
		
		//System.out.println("Trying to get explanation for instance "+instance+" ("+predict[0][0]+","+predict[0][1]+")");
		//System.out.println("Following attributes can be swapped: "+Arrays.toString(attributes_to_try));
		
		// Perform single attribute swapping
		for (int attr : attributes_to_try) {
			Set<Integer> attributes_to_swap = new HashSet<Integer>();
			attributes_to_swap.add(attr);
			Feature[] newInstance = dataset.getSwappedLine(originalFeatures, attributes_to_swap);
			double[][] newpredict = settings.useMultiClassExplanation ? 
					predictMulti(newInstance) : predict(originalCluster, newInstance);
			if (newpredict[0][0] != predict[0][0]) {
				//System.out.println("Got class change on: "+Arrays.toString(attributes_to_swap.toArray()));
				rules.add(getRuleArray(attributes_to_swap));
				chosenCombinations.add(attributes_to_swap);
			} else {
				expandCombinations.add(attributes_to_swap);
				expandProbabilities.add(newpredict[0][1]);
			}
		}
		
		// Perform iterative swapping
		for (int iteration = 1; iterations == -1 || iteration <= iterations; iteration++) {
			if (settings.stopEarly && rules.size() > 0) break;
			//System.out.println("*** Iteration: "+iteration);
			int nextIndex = getBestCombination(expandCombinations, expandProbabilities, 
					expandedCombinations, predict[0][1], getBestComboCache);
			if (nextIndex == -1)
				break;
			
			// Add to tried expansions
			expandedCombinations.add(new HashSet<Integer>(expandCombinations.get(nextIndex)));
			
			for (int attr : attributes_to_try) {// Expand this combination
				Set<Integer> attributes_to_swap = new HashSet<Integer>(expandCombinations.get(nextIndex));
				// Don't check the same one again
				if (attributes_to_swap.contains(attr))
					continue;
				attributes_to_swap.add(attr);
				
				// Don't check subsumed rules
				if (isCombinationSubsumed(attributes_to_swap, chosenCombinations))
					continue;
				
				// Perform new prediction
				double[][] newpredict = settings.useMultiClassExplanation ? 
						predictMulti(dataset.getSwappedLine(originalFeatures, attributes_to_swap)) :
							predict(originalCluster, dataset.getSwappedLine(originalFeatures, attributes_to_swap));
				if (newpredict[0][0] != predict[0][0]) {
					//System.out.println("Got class change on: "+Arrays.toString(attributes_to_swap.toArray()));
					rules.add(getRuleArray(attributes_to_swap));
					chosenCombinations.add(attributes_to_swap);
				} else {
					expandCombinations.add(attributes_to_swap);
					expandProbabilities.add(newpredict[0][1]);
				}
			}
		}
		
		cachedRules.put(instance, rules);
		
		return rules;
	}
	
	private boolean isCombinationSubsumed(Set<Integer> attributes_to_swap,
			List<Set<Integer>> chosenCombinations) {
		for (Set<Integer> chosenCombo : chosenCombinations) {
			if (chosenCombo.containsAll(attributes_to_swap)) {
				return true;
			}
		}
		return false;
	}

	private int getBestCombination(List<Set<Integer>> expandCombinations, 
			List<Double> expandProbabilities, 
			List<Set<Integer>> expandedCombinations,
			double baseProbability, List<Double> getBestComboCache) {
		int bestIndex = -1;
		double bestDiff = 0;
		
		for (int i = 0; i < expandCombinations.size(); i++) {
			if (getBestComboCache.size() <= i) {
				// Calculate difference first time and add to cache
				double diff = Math.abs(baseProbability - expandProbabilities.get(i));
				getBestComboCache.add(diff);
			}
			double diff = getBestComboCache.get(i);
			if (diff >= 0D) {
				// Still worth to recheck this one?
				if (expandedCombinations.contains(expandCombinations.get(i)))
					// This one has become useless
					getBestComboCache.set(i, -1D);
				else if (diff > bestDiff) {
					bestIndex = i;
					bestDiff = diff;
				}
			}
		}
		
		return bestIndex;
	}
	
	public Rule[] getRuleArray(Set<Integer> attributes) {
		Rule[] array = new Rule[attributes.size()];
		int counter = 0;
		for (int i : attributes) {
			array[counter] = dataset.getRule(i);
			counter++;
		}
		return array;
	}
	
	public Set<Integer> getAttributeSet(Rule[] rules) {
		Set<Integer> attributes = new HashSet<Integer>();
		for (Rule r : rules)
			attributes.add(dataset.getRuleIndex(r));
		return attributes;
	}
	
	private Integer[] getCandidateFeatures(int instance) {
		Feature[] originalFeatures = dataset.getInstance(instance);
		Set<Integer> candidates = new HashSet<Integer>();
		for (int f = 0; f < dataset.nrFeatures(); f++) {
			double oldVal = dataset.getFeature(f, originalFeatures).getValue();
			// May we swap a zero?
			if (oldVal == 0D && !settings.useZeroes)
				continue;
			// Is there support in the dataset for the new value
			double newVal = oldVal == 0D ? 1D : 0D;
			boolean exists = false;
			for (int i = 0; i < dataset.nrInstances(); i++) {
				if (dataset.getFeature(f, dataset.getInstance(i)).getValue() == newVal) {
					exists = true;
					break;
				}
			}
			if (!exists)
				continue;
			candidates.add(f);
		}
		return candidates.toArray(new Integer[] {});
	}
	
}
