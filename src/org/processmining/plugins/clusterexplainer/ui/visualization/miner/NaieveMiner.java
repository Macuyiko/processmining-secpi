package org.processmining.plugins.clusterexplainer.ui.visualization.miner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
		name = "Naieve Dependency Net Miner",
		parameterLabels = { "Log" },
		returnLabels = { "Dependency Net" },
		returnTypes = {
				DependencyNet.class
		},
		userAccessible = true,
		help = "Naieve Dependency Net Miner")

public class NaieveMiner {

	private DependencyNet dependencyNet;
	private XLog log;
	private XEventClassifier classifier;
	private XEventClasses classes;
	private Map<XEventClass, Integer> classToTask;
	
	@UITopiaVariant(uiLabel = "Mine Naieve Dependency Net",
			affiliation = UITopiaVariant.EHV,
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@econ.kuleuven.be",
			website = "http://econ.kuleuven.be")
	
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static DependencyNet ExecutePlugin(PluginContext context, XLog log) {
		NaieveMiner miner = new NaieveMiner(log);
		return miner.mine();
	}

	public NaieveMiner(XLog log) {
		this(log, XLogInfoImpl.NAME_CLASSIFIER);
	}

	public NaieveMiner(XLog log, XEventClassifier classifier) {
		this.log = log;
		this.classifier = classifier;
		this.classes = XEventClasses.deriveEventClasses(classifier, log);
	}

	public DependencyNet getDependencyNet() {
		return dependencyNet;
	}

	public XLog getLog() {
		return log;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public XEventClasses getClasses() {
		return classes;
	}

	public Map<XEventClass, Integer> getClassToTask() {
		return classToTask;
	}

	public DependencyNet mine() {
		mineDependencyNet();

		return getDependencyNet();
	}

	public void mineDependencyNet() {
		this.dependencyNet = new DependencyNet();
		this.classToTask = new TreeMap<XEventClass, Integer>();

		int t = 0;
		for (XEventClass cl : this.classes.getClasses()) {
			t++;
			this.classToTask.put(cl, t);
			this.dependencyNet.addTask(t);
			this.dependencyNet.setLabel(t, cl.getId());
		}

		for (XTrace trace : log) {
			List<EventRow> eventTable = makeEventTable(trace);
			Set<Flow> flowChain = makeFlowChain(eventTable);
			for (Flow f : flowChain) {
				this.dependencyNet.setArc(
						classToTask.get(f.from),
						classToTask.get(f.to),
						true);
			}
		}
	}

	private List<EventRow> makeEventTable(XTrace trace) {
		List<EventRow> eventTable = new ArrayList<EventRow>();

		boolean[] completeEventConsumed = new boolean[trace.size()];

		Date lastSeenTimestamp = null;
		for (int pos = 0; pos < trace.size(); pos++) {
			XEvent event = trace.get(pos);
			XEventClass eventClass = classes.getClassOf(event);
			String transition = XLifecycleExtension.instance().extractTransition(event);
			Date timestamp = XTimeExtension.instance().extractTimestamp(event);
			if (timestamp == null) timestamp = new Date(pos);
			if (lastSeenTimestamp != null && lastSeenTimestamp.compareTo(timestamp) > 0) {
				System.err.println("A trace was found where the events are not ordened " +
						"according to their timestamp: " + XConceptExtension.instance().extractName(trace));
			}
			boolean completeFound = false;
			if ("start".equals(transition)) {
				// Find the complete event, if there is one
				for (int nextPos = pos + 1; nextPos < trace.size(); nextPos++) {
					XEvent nextEvent = trace.get(nextPos);
					XEventClass nextEventClass = classes.getClassOf(nextEvent);
					String nextTransition = XLifecycleExtension.instance().extractTransition(nextEvent);
					Date nextTimestamp = XTimeExtension.instance().extractTimestamp(nextEvent);
					if (!nextEventClass.equals(eventClass)) continue;
					if (!nextTransition.equals("complete")) continue;
					if (completeEventConsumed[nextPos]) continue;
					completeFound = true;
					eventTable.add(new EventRow(eventClass, timestamp.getTime(), nextTimestamp.getTime()));
					completeEventConsumed[nextPos] = true;
					break;
				}
			}
			if (!completeFound) {
				if (completeEventConsumed[pos] == false) {
					eventTable.add(new EventRow(eventClass, timestamp.getTime(), timestamp.getTime()));
					completeEventConsumed[pos] = true;
				}
			}
			lastSeenTimestamp = timestamp;
		}

		return eventTable;
	}

	private Set<Flow> makeFlowChain(List<EventRow> eventTable) {
		Set<Flow> flowChain = new HashSet<Flow>();
		List<int[]> intChain = new ArrayList<int[]>();
		for (int e = 0; e < eventTable.size(); e++) {
			int back = findClosestCompletedBefore(eventTable, e);
			int forw = findClosestStartingAfter(eventTable, e);
			intChain.add(new int[] { back, forw });
		}
		for (int i = 0; i < intChain.size(); i++) {
			int from = -1;
			int to = -1;
			if (intChain.get(i)[0] > -1
					&& intChain.get(intChain.get(i)[0])[1] != i
					&& intChain.get(intChain.get(i)[0])[1] > -1) {
				from = intChain.get(i)[0];
				to = i;
				Flow f = new Flow(eventTable.get(from).eventClass, eventTable.get(to).eventClass);
				flowChain.add(f);
			}
			if (intChain.get(i)[1] > -1) {
				from = i;
				to = intChain.get(i)[1];
				Flow f = new Flow(eventTable.get(from).eventClass, eventTable.get(to).eventClass);
				flowChain.add(f);
			}
		}
		return flowChain;
	}

	private int findClosestCompletedBefore(List<EventRow> eventTable, int e) {
		long startTime = eventTable.get(e).startTime;
		long bestDiff = -1;
		int bestR = -1;
		for (int r = e - 1; r >= 0; r--) {
			long endTime = eventTable.get(r).stopTime;
			long diffTime = startTime - endTime;
			if (diffTime < 0)
				continue;
			if (bestDiff == -1 || diffTime < bestDiff) {
				bestR = r;
				bestDiff = diffTime;
			}
		}
		return bestR;
	}

	private int findClosestStartingAfter(List<EventRow> eventTable, int e) {
		long endTime = eventTable.get(e).stopTime;
		long bestDiff = -1;
		int bestR = -1;
		for (int r = e + 1; r < eventTable.size(); r++) {
			long startTime = eventTable.get(r).startTime;
			long diffTime = startTime - endTime;
			if (diffTime < 0)
				continue;
			if (bestDiff == -1 || diffTime < bestDiff) {
				bestR = r;
				bestDiff = diffTime;
			}
		}
		return bestR;
	}

}

class EventRow {
	public XEventClass eventClass;
	public long startTime;
	public long stopTime;

	public EventRow(XEventClass eclass, long start, long stop) {
		this.eventClass = eclass;
		this.startTime = start;
		this.stopTime = stop;
	}
}

class Flow {
	public XEventClass from;
	public XEventClass to;

	public Flow(XEventClass from, XEventClass to) {
		this.from = from;
		this.to = to;
	}
}
