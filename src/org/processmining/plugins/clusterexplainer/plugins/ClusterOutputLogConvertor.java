package org.processmining.plugins.clusterexplainer.plugins;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.guidetreeminer.ClusterLogOutput;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

@Plugin(name = "Export GuideTreeMiner solution to clustered XES", 
		parameterLabels = {"GuideTreeMiner ClusterLogOutput"},
		returnLabels = {"Clustered XES"},
		returnTypes = {
			XLog.class
		},
		userAccessible = true,
		help = "Creates a merged XES log file from a GuideTreeMiner solution")

public class ClusterOutputLogConvertor {
	
	@UITopiaVariant(uiLabel = "Export GuideTreeMiner solution to clustered XES",
			affiliation = "KU Leuven",
			author = "Pieter De Koninck",
			email = "pieter.dekoninck@kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	
	public static XLog makeXLog(UIPluginContext context, ClusterLogOutput clo) {
		int NoClusters = clo.getNoClusters();
		
		//String originalName = XConceptExtension.instance().extractName(clo.getClusterLog(0));
		 XLog log = LogUtils.newLog("ClusterLogOutput after GuideTree clustering");
		XFactory xFactory = new XFactoryBufferedImpl();
		
	   
		
		int tcounter = 0;
		for(int ccounter= 0; ccounter< NoClusters; ccounter++) {
			if (context != null)
				context.log("Cluster nr. "+ccounter);
			
			XLog c = clo.getClusterLog(ccounter);
			if (context != null){
				context.getProgress().setIndeterminate(false);
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(c.size());
				context.getProgress().setValue(0);
			}
			tcounter = 0;
			for (XTrace t : c) {
				if (context != null)
					context.getProgress().setValue(tcounter);
				tcounter++;
				XTrace tc = (XTrace) t.clone();
				tc.getAttributes().put("cluster:label", 
						xFactory.createAttributeDiscrete("cluster:label", ccounter+1, null));
				log.add(tc);
			}
			/**tcounter = 0;
			for (XTrace t : c.getExternalLog()) {
				if (context != null)
					context.getProgress().setValue(tcounter);
				tcounter++;
				XTrace tc = (XTrace) t.clone();
				tc.getAttributes().put("cluster:label", 
						xFactory.createAttributeDiscrete("cluster:label", ccounter, null));
				log.add(tc);
			} */
		}
		return log;
		
	}
	

}
