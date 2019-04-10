package org.processmining.plugins.clusterexplainer.jochen;
/**
 * 
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XSemanticExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author deweerdt
 *
 */
public class LogFromCSV {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String baseDir = "C:\\Users\\n09042\\Documents\\2. Onderzoek\\SECPI\\LogGeneration\\";
		
		int tracecount = 0;
		XLog log = newLog();
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(new File(baseDir+"Log.csv")));
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				
				String trace = nextLine[0];
				int frequency = Integer.parseInt(nextLine[1]);
				int cluster = Integer.parseInt(nextLine[2]);
				
				int i = 0;
				while (i < frequency){
					log = addTrace(trace,tracecount,log,cluster);
					tracecount++;
					i++;
				}
			}
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		XesXmlSerializer serializer = new XesXmlSerializer();

		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(new File(baseDir + "Log.xes"));
			serializer.serialize(log, outputStream);
			outputStream.close();
		} catch (Exception e) {
			System.out.println("Foutje");
		}
		System.out.println(log.size());

	}
	
	private static XLog newLog() {
		XLog log = null;
		log = null;
		XFactory xFactory = new XFactoryBufferedImpl();
		log = xFactory.createLog();
		// Set extensions
		log.getExtensions().add(XConceptExtension.instance());
		log.getExtensions().add(XOrganizationalExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		log.getExtensions().add(XSemanticExtension.instance());
		log.getExtensions().add(XTimeExtension.instance());
		// Set log classifiers
		log.getClassifiers().add(XLogInfoImpl.STANDARD_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.RESOURCE_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.LIFECYCLE_TRANSITION_CLASSIFIER);
		// Set guaranteed attributes
		log.getGlobalTraceAttributes().add((XAttribute)XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute)XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute)XLifecycleExtension.ATTR_TRANSITION.clone());
		
		
        XConceptExtension.instance().assignName(log, "Simulated event log");
        XLifecycleExtension.instance().assignModel(log, XLifecycleExtension.VALUE_MODEL_STANDARD);
		return log;
	}
	
	private static XLog addTrace(String tracestring, int traceCounter, XLog log, int cluster) {
		XFactory xFactory = new XFactoryBufferedImpl();
		XTrace trace = xFactory.createTrace();
		
		XConceptExtension.instance().assignName(trace, "trace_" + traceCounter);
        

		for (char activity : tracestring.toCharArray()) {
			

			long completeTime = System.currentTimeMillis();;
			
			String resource = "NONE";
				
			XEvent complete = xFactory.createEvent();
			XConceptExtension.instance().assignName(complete, Character.toString(activity));
			XOrganizationalExtension.instance().assignResource(complete, resource);
			XTimeExtension.instance().assignTimestamp(complete, completeTime);
			XLifecycleExtension.instance().assignStandardTransition(complete, StandardModel.COMPLETE);
			trace.getAttributes().put("cluster:label", 
					xFactory.createAttributeDiscrete("cluster:label", cluster, null));
			trace.add(complete);

		}
		log.add(trace);
		return log;
	}

}
