package org.processmining.plugins.clusterexplainer.plugins;

import java.util.Iterator;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.actitrac.ActiTraCClustering;
import org.processmining.plugins.actitrac.types.Cluster;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

@Plugin(name = "Export Actitrac solution to clustered XES", 
		parameterLabels = {"Actitrac solution"},
		returnLabels = {"Clustered XES"},
		returnTypes = {
			XLog.class
		},
		userAccessible = true,
		help = "Creates a merged XES log file from an Actitrac solution")

public class ActitracClusteredLogConvertorPlugin {
	
	@UITopiaVariant(uiLabel = "Export Actitrac solution to clustered XES",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	@PluginVariant(variantLabel = "Wizard settings", requiredParameterLabels = { 0 })
	
	public static XLog makeXLog(UIPluginContext context, ActiTraCClustering ac) {
		String originalName = XConceptExtension.instance().extractName(ac.getLog().getLog());
		XLog log = LogUtils.newLog(originalName + " (after ActiTraC clustering)");
		XFactory xFactory = new XFactoryBufferedImpl();
		
		Iterator<Cluster> i = ac.getTree().iterator();
		
		long ccounter = 0;
		int tcounter = 0;
		while(i.hasNext()) {
			ccounter++;
			if (context != null)
				context.log("Cluster nr. "+ccounter);
			
			Cluster c = i.next();
			if (context != null){
				context.getProgress().setIndeterminate(false);
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(c.getInternalLog().size());
				context.getProgress().setValue(0);
			}
			tcounter = 0;
			for (XTrace t : c.getInternalLog()) {
				if (context != null)
					context.getProgress().setValue(tcounter);
				tcounter++;
				XTrace tc = xFactory.createTrace(t.getAttributes());
				tc.getAttributes().put("cluster:label", 
						xFactory.createAttributeDiscrete("cluster:label", ccounter, null));
				for (XEvent e : t) {
					XEvent ec = xFactory.createEvent((XAttributeMap) e.getAttributes().clone());
					tc.add(ec);
				}
				log.add(tc);
			}
			tcounter = 0;
			for (XTrace t : c.getExternalLog()) {
				if (context != null)
					context.getProgress().setValue(tcounter);
				tcounter++;
				XTrace tc = xFactory.createTrace(t.getAttributes());
				tc.getAttributes().put("cluster:label", 
						xFactory.createAttributeDiscrete("cluster:label", ccounter, null));
				for (XEvent e : t) {
					XEvent ec = xFactory.createEvent((XAttributeMap) e.getAttributes().clone());
					tc.add(ec);
				}
				log.add(tc);
			}
		}
		return log;
		
	}
	

}
