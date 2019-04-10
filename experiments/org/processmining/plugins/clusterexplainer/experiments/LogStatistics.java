package org.processmining.plugins.clusterexplainer.experiments;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
	
public class LogStatistics {

	// Logs to experiment with
	//public final static String experimentRootPath = "C:\\Users\\n11093\\Documents\\Process Logs & Models\\";
	//public final static String experimentRootPath = "C:\\ExperimentsData\\SECPIextended\\LogsRevision\\";
	public final static String experimentRootPath = "C:\\ExperimentsData\\Cotradic event logs\\";

	public final static String[] experimentLogFiles = new String[] {
	/*	experimentRootPath+"Real Life KUL Incident Management\\incidentKUL.xes",
		experimentRootPath+"Real Life Mercator\\Mercator-Docflow-v4.xes",
	//	experimentRootPath+"Real Life Siemens\\Siemens-IncidentManagement.xes",
		experimentRootPath+"Real Life Telecom\\IncidentManagementTEL.xes",
		//experimentRootPath+"Real Life Walter Interventies\\Anonymized\\Interventies_Riskmatrix_05022013_clean_anon2.xes",
	//	experimentRootPath+"Real Life Walter Outsourcing\\Anonymized\\rawdataMetDataEnBlankEnZonderEnd.anon.xes",
		experimentRootPath+"Real Life KUL Purchase2Pay\\KU Leuven-Purchase2Pay.xes",
		experimentRootPath+"Real Life Walter Outsourcing\\Anonymized\\controlflow.sample.anon.xes",
		//experimentRootPath+"Real Life Hospital Log\\positives.xes",*/
			

			/*"KIM",

	"MCRM", "ICP","KP2P", //"MNB",


			"MOA", 
			"isbpm2013",

			"cProblemVolvo",
			"RTFMP",
			"WABOCoselog",
			//"BPI2012",
			"TSL",
			//"ReceiptWABOCosalog",*/
			"etm", "hospital", "incidents", "isbpm2013", "openproblem", "photo", "repair", "reviewing", "teleclaims"
	};
	
	public final static LogFrame logFrame = new LogFrame();
	
	public static void main(String[] args) throws Exception {
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(1000000000);
		XParser parser = new XesXmlParser();
		
		logFrame.addLine(new String[] {
				"log file",
				"log size",
				"distinct log size",
				"number of activities",
				"average trace length"
		});
		
		for (String logFile : experimentLogFiles) {
			System.out.println("============= STARTING NEW LOG =============");
			System.out.println("* Log: "+logFile);
			
			System.out.println("------ Loading log ------");
			List<XLog> logs = parser.parse(new File(experimentRootPath+logFile+".xes"));
			XLog log = logs.get(0);
			GroupedXLog glog = new GroupedXLog(log);
			XEventClasses eclasses = XEventClasses.deriveEventClasses(XLogInfoImpl.STANDARD_CLASSIFIER, log);
			
			List<Double> traceLengths = new ArrayList<Double>();
			for (XTrace t : log)
				traceLengths.add((double) t.size());
			
			logFrame.addLine(new String[] {
					""+logFile,
					""+log.size(),
					""+glog.size(),
					""+eclasses.size(),
					""+avg(traceLengths)
			});
			
		}
	}
	
	public static double count(List<Double> explanationLengths) {
		double total = 0;
		for (double i : explanationLengths)
			if (i >= 0)
				total++;
		return total;
	}

	public static double sd(List<Double> explanationLengths) {
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

	public static double avg(List<Double> explanationLengths) {
		double sum = 0;
		for (double i : explanationLengths)
			if (i >= 0)
				sum+=i;
		return sum / count(explanationLengths);
	}

	public static double max(List<Double> explanationLengths) {
		double max = explanationLengths.get(0);
		for (double i : explanationLengths)
			if (i >= max && i >= 0)
				max=i;
		return max;
	}

	public static double min(List<Double> explanationLengths) {
		double min = max(explanationLengths);
		for (double i : explanationLengths)
			if (i <= min && i >= 0)
				min=i;
		return min;
	}
	
	
}
