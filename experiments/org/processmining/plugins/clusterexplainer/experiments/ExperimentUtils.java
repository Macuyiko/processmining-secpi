package org.processmining.plugins.clusterexplainer.experiments;
import org.processmining.plugins.actitrac.utils.ATCParameters;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;


public class ExperimentUtils {

	public static void configureDefaultActiTraC(ATCParameters actiTraCParameters) {
		actiTraCParameters.setGreedy(true);
		actiTraCParameters.setGreedyDistance(false);
		
		actiTraCParameters.setDistanceGreediness(0.25F);
		
		actiTraCParameters.setMinimalClusterSize(0.25F);
		actiTraCParameters.setTargetFitness(1.0F);
		
		actiTraCParameters.setStopByNbGroups(true);
		actiTraCParameters.setStopByPercentage(false);
		
		actiTraCParameters.setStopNbGroups(4);
		actiTraCParameters.setStopPercentage(90.0F);
		
		actiTraCParameters.setSeparateNonFitting(false);
		
		
	}
	
	public static void configureDefaultSVMExplainer(ClusterExplainerSettings explainerParameters) {
		explainerParameters = new ClusterExplainerSettings();		
	}
	

}
