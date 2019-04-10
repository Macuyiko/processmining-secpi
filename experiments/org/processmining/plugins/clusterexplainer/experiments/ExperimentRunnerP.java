package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.plugins.clusterexplainer.dataset.RuleDataSet;
import org.processmining.plugins.clusterexplainer.plugins.ClusterExplainerPlugin;
import org.processmining.plugins.clusterexplainer.rules.AbstractRule.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

public class ExperimentRunnerP {

	// Logs to experiment with
	//public final static String experimentRootPath = "C:\\ExperimentsData\\SECPIextended\\RealLifeLogs\\clusterlogsoutput\\";
	public final static String experimentRootPath = "D:\\Users\\u0104808\\Documents\\RealLifeLogs\\clusterlogsoutput\\";

	//directory names
	public final static String[] dirnames= new String[] {
			
			"KIM",

	"MCRM", "ICP","KP2P", //"MNB",


			"MOA", "TSL"
//TODO: folders for new datasets;
			};
	//clustering algorithms
	public final static String[] lognames = new String[]{
			
			"ACTw25mcs25tf100",
					"GtmMRA","GtmMR","GtmGED","GtmLED","GtmBOA",
					"Gtm3-gram","Markov","ACTmcs25tf100"
//TODO: remove Markov and/or add CoTraDiC;					
					};
	
	public final static String[] ruleSettings = new String[] {"Exi", "Adf", "Sdf", "Awf", "Swf", "All"};
	
	public final static double[] stepsC = new double[] {1d, 2d, 4d, 8d, 20d, 100d, 1000d, 10000d};
	public final static double[] stepsE = new double[] {0.01, 0.001, 0.0001};
	public final static int nrIterations = 50;
	public final static double[] stepsCorrelationThreshold= new double[] {/*2,*/ 1d};//, 0.95};
	public final static boolean stopEarly = true;
	
	public final static String[] j48configs = new String[] {/*"-U -M 2", "5perc",*/"-C 0.25 -M 2", "5perctotal"};
	public final static String[] jRipconfigs = new String[] {/*"-F 3 -N 2.0 -O 2 -S 1 -P", ,"5perc",*/"-F 3 -N 2.0 -O 2 -S 1","5perctotal"};
	
	public final static LogFrame logFrame = new LogFrame();
	
	public static void main(String[] args) throws Exception {
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(1000000000);
		XParser parser = new XesXmlParser();
		
		logFrame.addLine(new String[] {
				"directory",
				"logname",
				"log size",
				
				"technique",
				"settings",
				
				"runtime",
				"UseZeroes","CorrelationThreshold",
				
				"accuracy",
				"maccuracy",
				"totalinstances",
				"correctlyinstances",
				"Icount (explainableinstances)", "Imin", "Imax", "Iavg", "Isd",
				"Acount", "Amin", "Amax", "Aavg", "Asd",
				"Mcount", "Mmin", "Mmax", "Mavg", "Msd","NrLeaves", "TreeSize", "NrRules"
			
				//TODO: add number of clusters					
	
		});
		
		//TODO: Adjust for number of clusters, introduce cross-validation					

		for(int uZ=0; uZ<=1; uZ++){
			boolean useZeroes=false;
			if(uZ==1) useZeroes=true; 
			WriteArrayToCSV arrayWriterall= new WriteArrayToCSV(experimentRootPath + "ExperimentName"+uZ+ ".csv");
		
		for(String dirname: dirnames){
			WriteArrayToCSV arrayWriterdir= new WriteArrayToCSV(experimentRootPath + dirname +"\\"+dirname+"ExperimentName"+uZ+".csv");
			arrayWriterdir.addLine(new String[] {
					"directory",
					"logname",
					"log size",
					
					"technique",
					"settings",
					
					"runtime",
					"UseZeroes","CorrelationThreshold",
					
					"accuracy",
					"maccuracy",
					"totalinstances",
					"correctlyinstances",
					"Icount (explainableinstances)", "Imin", "Imax", "Iavg", "Isd",
					"Acount", "Amin", "Amax", "Aavg", "Asd",
					"Mcount", "Mmin", "Mmax", "Mavg", "Msd",
					"NrLeaves", "TreeSize", "NrRules"
					
					//TODO: add number of clusters					

			});
			
			arrayWriterall.addLine(new String[] {
					"directory",
					"logname",
					"log size",
					
					"technique",
					"settings",
					
					"runtime",
					"UseZeroes","CorrelationThreshold",
					
					"accuracy",
					"maccuracy",
					"totalinstances",
					"correctlyinstances",
					"Icount (explainableinstances)", "Imin", "Imax", "Iavg", "Isd",
					"Acount", "Amin", "Amax", "Aavg", "Asd",
					"Mcount", "Mmin", "Mmax", "Mavg", "Msd",
					"NrLeaves", "TreeSize", "NrRules"
					
					//TODO: add number of clusters					

			});
			
			
			for (String logname : lognames) {
				for (String ruleset :ruleSettings){
				Rule[] rules= getRulesFromStrings(ruleset);
				for(double correlationThreshold : stepsCorrelationThreshold){
			System.out.println("============= STARTING NEW LOG =============");
			System.out.println("* Directory: "+dirname + "File:" +logname );
			
			System.out.println("------ Setting up ------");
			ClusterExplainerSettings settings = new ClusterExplainerSettings();
			ExperimentUtils.configureDefaultSVMExplainer(settings);
			settings.useMultiClassExplanation = true;
			settings.useZeroes = useZeroes;
			settings.iterations = nrIterations;
			settings.stopEarly = stopEarly;
			settings.rulesToGenerate.clear();
			settings.removeZeroVariance = true;
			settings.removeCorrelatedThreshold = correlationThreshold;
			
			for (Rule rule : rules) settings.rulesToGenerate.add(rule);
			
			System.out.println("------ Loading log ------");
			String logFile= experimentRootPath + dirname +"\\" + logname+".xes";
			List<XLog> logs = parser.parse(new File(logFile));
			XLog log = logs.get(0);
			
			String[] basicInformation = new String[] {
					dirname,
					logname,
					""+log.size(),
					
					"",
					"",
					"",
					uZ+"",
					correlationThreshold+""
			};
		
			
			Collection<String> activities = LogUtils.getEventClassesAsString(log, XLogInfoImpl.STANDARD_CLASSIFIER);
			RuleDataSet dataset = ClusterExplainerPlugin.getDataSet(null, log, activities, settings);
			
	/*		System.out.println("------ Evaluating SVM Model ------");
			long startTime = System.currentTimeMillis();
			double[] infosvm = doSVM(log, settings, dataset);
			basicInformation[basicInformation.length-5] = "svm";
			basicInformation[basicInformation.length-4] = ruleset;
			long endTime = System.currentTimeMillis();
			basicInformation[basicInformation.length-3] = Float.toString(endTime-startTime);
			logExperiment(basicInformation, infosvm);
			logExperimentCSV(basicInformation, infosvm,arrayWriterdir);
			logExperimentCSV(basicInformation, infosvm,arrayWriterall);
*/
			
			
			System.out.println("------ Evaluating J48 Model ------");
			for (String config : j48configs) {
				long startTimeJ48 = System.currentTimeMillis();
				System.out.println("* Config: "+config);
				basicInformation[basicInformation.length-5] = "J48"+config;
				basicInformation[basicInformation.length-4] = ruleset;
				double[] infoj48 = doJ48(log, config, settings, dataset);
				long endTimeJ48 = System.currentTimeMillis();
				basicInformation[basicInformation.length-3] = Float.toString(endTimeJ48-startTimeJ48);
				logExperiment(basicInformation, infoj48);
				logExperimentCSV(basicInformation, infoj48,arrayWriterdir);
				logExperimentCSV(basicInformation, infoj48,arrayWriterall);

			}
			
	/*		System.out.println("------ Evaluating JRIP Model ------");
			for (String config : jRipconfigs) {
				long startTimeJRip = System.currentTimeMillis();
				System.out.println("* Config: "+config);
				basicInformation[basicInformation.length-5] = "JRIP"+config;
				basicInformation[basicInformation.length-4] = ruleset;
				double[] infojrip = doJRIP(log, config, settings, dataset);
				long endTimeJRip = System.currentTimeMillis();
				basicInformation[basicInformation.length-3] = Float.toString(endTimeJRip-startTimeJRip);
				logExperiment(basicInformation, infojrip);
				logExperimentCSV(basicInformation, infojrip,arrayWriterdir);
				logExperimentCSV(basicInformation, infojrip,arrayWriterall);
			}*/
				}
			
			}
		}
		}
		}
		}
	
	
	

	private static Rule[] getRulesFromStrings(String ruleset) {
		if (ruleset.equals("Exi")) return new Rule[] {Rule.EXISTS};
		else if (ruleset.equals("Adf")) return new Rule[] {Rule.FOLLOWS_DIRECT_ALWAYS};
		else if (ruleset.equals("Sdf")) return new Rule[] {Rule.FOLLOWS_DIRECT_SOMETIMES};	
		else if (ruleset.equals("Awf")) return new Rule[] {Rule.FOLLOWS_WEAK_ALWAYS};	
		else if (ruleset.equals("Swf")) return new Rule[] {Rule.FOLLOWS_WEAK_SOMETIMES};	
		else if (ruleset.equals("All")) return new Rule[] {Rule.EXISTS, Rule.FOLLOWS_DIRECT_ALWAYS,
			Rule.FOLLOWS_DIRECT_SOMETIMES, Rule.FOLLOWS_WEAK_ALWAYS, Rule.FOLLOWS_WEAK_SOMETIMES};
		else return null;
	}

	private static void logExperiment(String[] basicInformation, double[] modelStats) {
		String[] allInformation = new String[basicInformation.length + modelStats.length];
		for (int i = 0; i < basicInformation.length; i++)
			allInformation[i] = basicInformation[i];
		for (int i = 0; i < modelStats.length; i++)
			allInformation[basicInformation.length+i] = ""+modelStats[i];
		logFrame.addLine(allInformation);
	}
	
	private static void logExperimentCSV(String[] basicInformation, double[] modelStats,WriteArrayToCSV arrayWriter) {
		String[] allInformation = new String[basicInformation.length + modelStats.length];
		for (int i = 0; i < basicInformation.length; i++)
			allInformation[i] = basicInformation[i];
		for (int i = 0; i < modelStats.length; i++)
			allInformation[basicInformation.length+i] = ""+modelStats[i];
		
		arrayWriter.addLine(allInformation);
	}
		
	protected static double[] doJRIP(XLog clusteredLog, String config, ClusterExplainerSettings settings, RuleDataSet dataset) {
		JRipExplainerModel model = new JRipExplainerModel(clusteredLog, settings);
		String[] options;
		try {
			if(config== "5perc"){
				GroupedXLog glog = new GroupedXLog(clusteredLog);
				String minimum= String.valueOf(glog.size()/20);
				String configstring=	"-F 3 -N "+ minimum +" -O 2 -S 1"	;
				options = weka.core.Utils.splitOptions(configstring);
				}
			else if(config== "5perctotal"){
				String minimum= String.valueOf(clusteredLog.size()/20);
				String configstring=	"-F 3 -N "+ minimum +" -O 2 -S 1"	;
				options = weka.core.Utils.splitOptions(configstring);
				}
			else options = weka.core.Utils.splitOptions(config);
			model.setjRipParameterss(options);
			model.constructDataset(dataset);
			return getModelStats(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static double[] doJ48(XLog clusteredLog, String config, ClusterExplainerSettings settings, RuleDataSet dataset) {
		J48ExplainerModel model = new J48ExplainerModel(clusteredLog, settings);
		String[] options;
		try {
			if(config== "5perc"){
				GroupedXLog glog = new GroupedXLog(clusteredLog);
				String minimum= String.valueOf(glog.size()/20);
				String configstring=	"-C 0.25 -M "+ minimum		;
				options = weka.core.Utils.splitOptions(configstring);
				} 
			else if(config== "5perctotal"){
				String minimum= String.valueOf(clusteredLog.size()/20);
				String configstring=	"-C 0.25 -M "+ minimum		;
				options = weka.core.Utils.splitOptions(configstring);
				}
			else options = weka.core.Utils.splitOptions(config);
			model.setj48Parameterss(options);
			model.constructDataset(dataset);
			return getModelStats(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static double[] doSVM(XLog clusteredLog, ClusterExplainerSettings settings, RuleDataSet dataset) {
		double bestA = 0;
		SVMExplainerModel bestM = null;
		
		for (double c : stepsC) { if (bestA >= 1.0d) break;
		for (double e : stepsE) { if (bestA >= 1.0d) break;
			System.out.println(" -- test: c = "+c+" and e = "+e);
			SVMExplainerModel model = new SVMExplainerModel(clusteredLog, settings);
			model.getExplainerParameters().c = c;
			model.getExplainerParameters().e = e;
			model.constructDataset(dataset);
			double acc =  model.getAccuracy();
			if (acc >= bestA) {
				System.out.println("Got better accuracy: "+acc+" with c = "+c+" and e = "+e);
				bestA = acc;
				bestM = model;
			}
			System.gc();
		}}
		
		return getModelStats(bestM);
	}

	private static double[] getModelStats(ExplainerModel model) {
		double accuracy = model.getAccuracy();
		double maccuracy = model.getManualAccuracy();
		double totalinstances = model.getClusteredLog().size();
		double[] extraInfo= model.getExtraInfo();

		
		List<Double> explanationLengths = getModelExplanationLengths(model);
		double Imin = min(explanationLengths);
		double Imax = max(explanationLengths);
		double Iavg = avg(explanationLengths);
		double Isd = sd(explanationLengths);
		double Icount = count(explanationLengths);
		
		List<Double> explanationLengthsAvg = replaceNegatives(explanationLengths, Iavg);
		double Amin = min(explanationLengthsAvg);
		double Amax = max(explanationLengthsAvg);
		double Aavg = avg(explanationLengthsAvg);
		double Asd = sd(explanationLengthsAvg);
		double Acount = count(explanationLengthsAvg);
		
		List<Double> explanationLengthsMax = replaceNegatives(explanationLengths, Imax);
		double Mmin = min(explanationLengthsMax);
		double Mmax = max(explanationLengthsMax);
		double Mavg = avg(explanationLengthsMax);
		double Msd = sd(explanationLengthsMax);
		double Mcount = count(explanationLengthsMax);
		
		return new double[] {
				accuracy,
				maccuracy,
				totalinstances,
				explanationLengths.size(),
				Icount, Imin, Imax, Iavg, Isd,
				Acount, Amin, Amax, Aavg, Asd,
				Mcount, Mmin, Mmax, Mavg, Msd,
				extraInfo[0], extraInfo[1], extraInfo[2]
		};
	}
	
	private static List<Double> replaceNegatives(List<Double> original, double with) {
		List<Double> newList = new ArrayList<Double>();
		for (double i : original) {
			if (i < 0) newList.add(with);
			else newList.add(i);
		}
		return newList;
	}

	private static double count(List<Double> explanationLengths) {
		double total = 0;
		for (double i : explanationLengths)
			if (i >= 0)
				total++;
		return total;
	}

	private static double sd(List<Double> explanationLengths) {
		double n = explanationLengths.size();
		if (n < 2) {
			return -1d;
		}
		double avg = explanationLengths.get(0);
		double sum = 0;
		for (int i = 1; i < explanationLengths.size(); i++) {
			if (explanationLengths.get(i) < 0)
				continue;
			double newavg = avg + (explanationLengths.get(i) - avg) / (i + 1);
			sum += (explanationLengths.get(i) - avg) * (explanationLengths.get(i) - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n /*- 1*/));
	}

	private static double avg(List<Double> explanationLengths) {
		double sum = 0;
		for (double i : explanationLengths)
			if (i >= 0)
				sum+=i;
		return sum / count(explanationLengths);
	}

	private static double max(List<Double> explanationLengths) {
		if (explanationLengths.isEmpty()) return 0;
		double max = explanationLengths.get(0);
		for (double i : explanationLengths)
			if (i >= max && i >= 0)
				max=i;
		return max;
	}

	private static double min(List<Double> explanationLengths) {
		double min = max(explanationLengths);
		for (double i : explanationLengths)
			if (i <= min && i >= 0)
				min=i;
		return min;
	}

	private static List<Double> getModelExplanationLengths(ExplainerModel model) {
		List<Double> lengths = new ArrayList<Double>(); 
		for (int i = 0; i < model.getClusteredLog().size(); i++) {
			if (model.isCorrectlyClassified(i)) {
				lengths.add(model.getInstanceSize(i));
			}
		}
		return lengths;
	}
	
	
}
