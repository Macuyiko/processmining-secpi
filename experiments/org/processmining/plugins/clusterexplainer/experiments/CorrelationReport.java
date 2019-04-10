package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.clusterexplainer.plugins.ClusterExplainerPlugin;
import org.processmining.plugins.clusterexplainer.rules.AbstractRule.Rule;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

public class CorrelationReport {

	// Logs to experiment with
	public final static String experimentRootPath = "\\\\econgfs.econ.kuleuven.ac.be\\groups\\KBI_MI\\_People\\Seppe vanden Broucke\\SECPI\\";
	public final static String experimentLogFile = experimentRootPath+"GtmGED.xes";
	
	public final static Rule[] rules = new Rule[] {Rule.EXISTS, Rule.FOLLOWS_DIRECT_ALWAYS,
			Rule.FOLLOWS_DIRECT_SOMETIMES, Rule.FOLLOWS_WEAK_ALWAYS, Rule.FOLLOWS_WEAK_SOMETIMES};
	public final static LogFrame logFrame = new LogFrame();
	
	public static void main(String[] args) throws Exception {
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(1000000000);
		XParser parser = new XesXmlParser();
				
		System.out.println("------ Loading log ------");
		List<XLog> logs = parser.parse(new File(experimentLogFile));
		XLog log = logs.get(0);
		
		ClusterExplainerSettings settings = new ClusterExplainerSettings();
		ExperimentUtils.configureDefaultSVMExplainer(settings);
		for (Rule rule : rules) settings.rulesToGenerate.add(rule);
		
		Collection<String> activities = LogUtils.getEventClassesAsString(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		List<org.processmining.plugins.clusterexplainer.rules.Rule> rules = ClusterExplainerPlugin.getRuleHeader(activities, settings);
		List<Pair<Integer, List<Double>>> values = ClusterExplainerPlugin.getValues(null, log, rules);
			
		logFrame.addLine("--------- variances ---------");
		int total = 0;
		for (int i = rules.size()-1; i >= 0; i--) {
			List<Double> col = ClusterExplainerPlugin.getCol(values, i);
			double z = ClusterExplainerPlugin.sd(col);
			if (z == 0D) {
				logFrame.addLine(rules.get(i).toShortString());
				total++;
			}
		}
		logFrame.addLine("Total: "+total);
		
		logFrame.addLine("\r\n\r\n--------- correlations ---------");
		for (double t : new double[]{1d, 0.95d, 0.90d, 0.85d}) {
			int totalc = 0;
			for (int i = rules.size()-1; i >= 0; i--) {
				List<Double> col = ClusterExplainerPlugin.getCol(values, i);
				for (int ii = i-1; ii >= 0; ii--) {
					List<Double> col2 = ClusterExplainerPlugin.getCol(values, ii);
					double cor = ClusterExplainerPlugin.cor(col, col2);
					if (cor >= t) {totalc++; break;}
				}
			}
			logFrame.addLine("Total for "+t+": "+totalc);
		}
		logFrame.addLine("Total features before pruning "+rules.size());
		
	}
		
}
