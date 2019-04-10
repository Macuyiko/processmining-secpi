package org.processmining.plugins.clusterexplainer.experiments;
import java.io.BufferedWriter;
import java.io.FileWriter;




public class WriteArrayToCSV  {
	private  String fileName;

	
	public WriteArrayToCSV(String fileName) {
		this.fileName= fileName;
	}

	
	
	public void addLine(String[] line) {
		try{
		BufferedWriter br = new BufferedWriter(new FileWriter(fileName, true));
    	StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String i : line) {
			if (!first)
				sb.append(",");
			sb.append(i);
			first = false;
		}
		 br.write(sb.toString());
		 br.newLine();
		 br.close();
	} catch (Exception e){e.printStackTrace();}
}
}
