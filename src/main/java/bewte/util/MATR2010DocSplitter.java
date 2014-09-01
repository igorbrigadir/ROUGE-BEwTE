package bewte.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class MATR2010DocSplitter {
	
	public static void main(String[] args) throws Exception {
		 String inFiles = args[0];
		 File outDir = new File(args[1]);
		 boolean useLanguagesInsteadOfSetId = Boolean.parseBoolean(args[2]);
		 outDir.mkdirs();
		 String[] files = inFiles.split(":");
		 for(String file : files) {
			 System.err.println(file);
			 readDoc(new File(file), outDir, useLanguagesInsteadOfSetId);
		 }	
	}
	
	public static void readDoc(File file, File outDir, boolean useLanguagesInsteadOfSetId) throws Exception {
		System.err.println("Building doc");
		String fullText = readFullText(file);
		Document d = new SAXBuilder().build(new StringReader(fullText));
		System.err.println("Doc built");
		
	    Element root = d.getRootElement();
	    List children = new ArrayList(root.getChildren("tstset"));
	    children.addAll(root.getChildren("refset"));
	    int numSystems = children.size();
	    for(int i = 0; i < numSystems; i++) {
	    	Element child = (Element)children.get(i);
	    	String setId = child.getAttributeValue("setid");
	    	String sourceLanguage = child.getAttributeValue("srclang");
	    	String targetLanguage = child.getAttributeValue("trglang");
	    	if(useLanguagesInsteadOfSetId) setId = sourceLanguage+"_"+targetLanguage;
	    	String system = child.getAttributeValue("sysid");
	    	if(system == null) {
	    		system = child.getAttributeValue("refid");
	    	}
	    	System.err.println("System: " + system);
	    	List docs = child.getChildren("doc");
	    	int numDocs = docs.size();
	    	for(int j = 0; j < numDocs; j++) {
	    		Element doc = (Element)docs.get(j);
	    		String docid = doc.getAttributeValue("docid");
	    		//String forFilename = docid.substring(docid.lastIndexOf('/')+1);
	    		List<Element> segments = new ArrayList<Element>();
	    		getAllSegments(doc, segments);
	    		File outFile = new File(outDir, setId+"+"+docid.replace('/', 'Ɣ')+"+"+system);
	    		System.err.println("OutFile: " + outFile.getAbsolutePath());
	    		PrintWriter writer = new PrintWriter(outFile);
	    		writer.println(docid);
	    		final int numSegments = segments.size();
	    		int lastSegmentNum = 0;
	    		for(int k = 0; k < numSegments; k++) {
	    			Element segment = (Element)segments.get(k);
	    			String id = segment.getAttributeValue("id");
	    			int currentSegmentNum = Integer.parseInt(id);
	    			for(int z = lastSegmentNum; z < currentSegmentNum-1; z++) {
	    				writer.println((z+1) + "  ");
	    			}
	    			writer.println(currentSegmentNum + " " + segment.getText().replaceAll("[\\n\\r]", " "));
	    			lastSegmentNum = currentSegmentNum;
	    		}
	    		writer.close();
	    	}
	    }
	}
	
	private static void getAllSegments(Element elem, List<Element> segments) {
		List children = elem.getChildren();
		final int numChildren = children.size();
		for(int i = 0; i < numChildren; i++) {
			Element child = (Element)children.get(i);
			if(child.getName().equals("seg")) {
				segments.add(child);
			}
			else {
				getAllSegments(child, segments);
			}
		}
	}
	
	private static String readFullText(File inFile) throws IOException {
		StringBuilder buf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		buf.append(reader.readLine()).append("\n");
		reader.readLine();
		String line = null;
		while((line = reader.readLine()) != null) {
			buf.append(line).append("\n");
		}
		reader.close();
		return buf.toString();
	}
	
}