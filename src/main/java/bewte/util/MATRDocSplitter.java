package bewte.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Class for splitting the MetricsMATR 2008 input documents. May be useful for other MT datasets as well. 
 *
 */
public class MATRDocSplitter {
	
	public static void main(String[] args) throws Exception {
		
		String[] inputDirs = args[0].split(";");
		File outputDir = new File(args[1]);
		outputDir.mkdirs();
		
		
		for(String inputDirString : inputDirs) {
			System.err.println("InputFileDir: " + inputDirString);
			File[] inputFiles = new File(inputDirString).listFiles();
			PrintWriter writer = null;
			for(File file : inputFiles) {
				System.err.println(file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while((line = reader.readLine()) == null) {
					;//read any blank lines
				}
				int setIdIndex = line.indexOf("setid=\"");
				String setId = line.substring(setIdIndex+7, line.indexOf("\"", setIdIndex+7));
				//System.err.println("Overriding set id:" + setId);
				//setId = "setId";
				String sysId = "";
				
				String docId = "";
				
				while((line = reader.readLine()) != null) {
					if(line.startsWith("<DOC") || line.startsWith("<doc")) {
						int docIdIndex = line.indexOf("docid=\"");
						int sysIdIndex = line.indexOf("sysid=\"");
						docId = line.substring(docIdIndex+7, line.indexOf("\"", docIdIndex+7));
						sysId = line.substring(sysIdIndex+7, line.indexOf("\"", sysIdIndex+7));
						
						writer = new PrintWriter(new FileWriter(new File(outputDir, setId + "+" + docId + "+" + sysId)));
						
						System.err.println("file:" + file.getName() + " setId:" + setId + " docId:" + docId + " sysId:" + sysId);
					}
					else if(line.startsWith("<seg")) {
						int startId = line.indexOf("id=\"");
						int endId = line.indexOf("\"", startId+4);
						//System.err.println(line);
						int segId = Integer.parseInt(line.substring(startId+4,endId).trim());
						int segStart = line.indexOf(">")+1;
						int segEnd = line.lastIndexOf("<");
						
						writer.println(segId + "\t" + line.substring(segStart, segEnd));
					}
					else if(line.startsWith("</DOC") || line.startsWith("</doc")) {
						writer.close();
					}	
					else if(!line.trim().equals("") && !line.startsWith("</tstset") && !line.startsWith("</p>") && !line.startsWith("<p>") && !line.startsWith("</refset")) {
						System.err.println("Line: " + line);
						throw new RuntimeException("Unexpected tag (or lack thereof) at: " + line);
					}	
				}
				
			}
		}
	}
	
}