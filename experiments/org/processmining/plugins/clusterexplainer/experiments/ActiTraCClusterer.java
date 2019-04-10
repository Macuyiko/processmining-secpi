package org.processmining.plugins.clusterexplainer.experiments;
import java.util.Iterator;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.actitrac.ActiTraCClustering;
import org.processmining.plugins.actitrac.algorithm.Clusterer;
import org.processmining.plugins.actitrac.algorithm.GreedyClusterer;
import org.processmining.plugins.actitrac.algorithm.GreedyDistanceClusterer;
import org.processmining.plugins.actitrac.types.Cluster;
import org.processmining.plugins.actitrac.utils.ATCParameters;
import org.processmining.plugins.actitrac.utils.OperationCancelledException;
import org.processmining.plugins.clusterexplainer.plugins.ActitracClusteredLogConvertorPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;


public class ActiTraCClusterer {

	private ActiTraCClustering clusterSolution;
	private XLog fullLog;
	private ATCParameters parameters;
    private HeuristicsMinerSettings hmSettings;

	public ActiTraCClusterer(XLog log) {
		fullLog = log;
		parameters = new ATCParameters();
		hmSettings = new HeuristicsMinerSettings();
	}
	
	public void cluster() {
		Clusterer clusterer;
		// Assign a ClustererEventListenerUI-object that is linked to the
		// current context to the Clusterer
		if (parameters.isGreedy())
			clusterer = new GreedyClusterer(fullLog, 
					parameters,
					hmSettings, 
					null);
		else
			clusterer = new GreedyDistanceClusterer(fullLog,
					parameters,
					hmSettings, 
					null);
		// Start calculations
		try {
			clusterer.cluster();
		} catch (OperationCancelledException e) {
			e.printStackTrace();
		}
		// Return the result
		clusterSolution = clusterer.getClustering();
	}
	
	public ATCParameters getActiTraCParameters() {
		return parameters;
	}
	
	public HeuristicsMinerSettings getHeuristicsMinerParameters() {
		return hmSettings;
	}
	
	public XLog getClusteredLog() {
		return ActitracClusteredLogConvertorPlugin.makeXLog(null, clusterSolution);
	}
	
	public ActiTraCClustering getClusterSolution() {
		return clusterSolution;
	}
	
	public int getNrClusters() {
		Iterator<Cluster> i = clusterSolution.getTree().iterator();
		int ccounter = 0;
		while(i.hasNext()) {
			i.next();
			ccounter++;
		}
		return ccounter;
	}
	
	public int getClusterSize(int cluster) {
		Iterator<Cluster> i = clusterSolution.getTree().iterator();
		long ccounter = 0;
		while(i.hasNext()) {
			Cluster c = i.next();
			if (ccounter == cluster) {
				return c.getInternalLog().size() + c.getExternalLog().size();
			}
			ccounter++;
		}
		return 0;
	}

}
