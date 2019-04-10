package org.processmining.plugins.clusterexplainer.plugins;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

@Plugin(name = "Convert Log Array to Clustered XES", 
		parameterLabels = {"Normal Logs"},
		returnLabels = {"Clustered XES"},
		returnTypes = {
			XLog.class
		},
		userAccessible = true,
		help = "Convert an array of logs to a clustered log")

public class NormalLogConvertorPlugin {
	
	@UITopiaVariant(uiLabel = "Convert Normal Log to clustered XES",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	
	public static XLog makeXLog(UIPluginContext context, XLog[] nLogs) {
		String originalName = XConceptExtension.instance().extractName(nLogs[0]);
		
		XLog log = LogUtils.newLog(originalName + " (after assignment)");
		XFactory xFactory = new XFactoryBufferedImpl();
		
		for (int l = 0; l < nLogs.length; l++) {
			for (XTrace t : nLogs[l]) {
				XTrace tc = (XTrace) t.clone();
				tc.getAttributes().put("cluster:label", 
						xFactory.createAttributeDiscrete("cluster:label", (l+1), null));
				log.add(tc);
			}
		}
		return log;
		
	}
	

}
