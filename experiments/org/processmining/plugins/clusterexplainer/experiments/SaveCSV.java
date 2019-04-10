package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.plugins.clusterexplainer.settings.ClusterExplainerSettings;
import au.com.bytecode.opencsv.CSVWriter;
	
public class SaveCSV {

	// Logs to experiment with
	public final static String experimentRootPath = "C:\\Users\\n11093\\Documents\\Process Logs & Models\\";
	public final static String[] experimentLogFiles = new String[] {
		experimentRootPath+"Real Life KUL Incident Management\\incidentKUL.xes",
	//	experimentRootPath+"Real Life Mercator\\Mercator-Docflow-v4.xes",
	//	experimentRootPath+"Real Life Siemens\\Siemens-IncidentManagement.xes",
	//	experimentRootPath+"Real Life Telecom\\IncidentManagementTEL.xes",
	//	//experimentRootPath+"Real Life Walter Interventies\\Anonymized\\Interventies_Riskmatrix_05022013_clean_anon2.xes",
	//	experimentRootPath+"Real Life Walter Outsourcing\\Anonymized\\rawdataMetDataEnBlankEnZonderEnd.anon.xes",
	//	experimentRootPath+"Real Life KUL Purchase2Pay\\KU Leuven-Purchase2Pay.xes",
	//	experimentRootPath+"Real Life Walter Outsourcing\\Anonymized\\controlflow.sample.anon.xes",
	//	//experimentRootPath+"Real Life Hospital Log\\positives.xes",
	};
	
	public static void main(String[] args) throws Exception {
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(1000000000);
		XParser parser = new XesXmlParser();
		
		for (String logFile : experimentLogFiles) {
			System.out.println("============= STARTING NEW LOG =============");
			System.out.println("* Log: "+logFile);
			
			System.out.println("------ Loading log ------");
			List<XLog> logs = parser.parse(new File(logFile));
			XLog log = logs.get(0);
			
			System.out.println("* Log size: "+log.size());
			
			ActiTraCClusterer clusterer = new ActiTraCClusterer(log);
			ExperimentUtils.configureDefaultActiTraC(clusterer.getActiTraCParameters());
			clusterer.getActiTraCParameters().setSeparateNonFitting(true);
			clusterer.getActiTraCParameters().setStopNbGroups(3);
			clusterer.cluster();
			
			XLog clusteredLog = clusterer.getClusteredLog();
			Collection<XEventClass> activities = XEventClasses.deriveEventClasses(
					XLogInfoImpl.STANDARD_CLASSIFIER, clusteredLog).getClasses();
			List<XEventClass> activitiesOrdered = new ArrayList<XEventClass>();
			activitiesOrdered.addAll(activities);
			
			CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\n11093\\Desktop\\dataset.csv"));
			List<String> headings = new ArrayList<String>();
			headings.add("label");
			for (XEventClass a : activitiesOrdered) {
				for (XEventClass b : activitiesOrdered) {
					headings.add(a.getId()+"-"+b.getId());
				}
			}
			writer.writeNext(headings.toArray(new String[]{}));
			
			for (int traceCounter = 0; traceCounter < clusteredLog.size(); traceCounter++) {
				XTrace trace = clusteredLog.get(traceCounter);
				List<String> line = new ArrayList<String>();
				int c = Integer.parseInt(trace.getAttributes().get(ClusterExplainerSettings.CLUSTER_ATTRIBUTE).toString());
				line.add(""+c);
				Set<String> directs = new HashSet<String>();
				for (int i = 0; i < trace.size(); i++) {
					String first = XLogInfoImpl.STANDARD_CLASSIFIER.getClassIdentity(trace.get(i));
					directs.add(first);
				}
				for (XEventClass a : activitiesOrdered) {
					for (XEventClass b : activitiesOrdered) {
						line.add(directs.contains(a.getId()) && directs.contains(b.getId()) ? "1" : "0");
					}
				}
				writer.writeNext(line.toArray(new String[]{}));
				
			}
			
			writer.close();
		}
		
	}

	
}
