package bewte.endanalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import foal.map.IntIntHashMap;

import bewte.util.BEUtils;

public class MetricsMatrEndAnalyzer extends AbstractEndAnalyzer {
	
	public final static String PARAM_VERSION = "VERSION";
	
	private String systemOutputFilename;
	private String docLevelFilename;
	private String segmentLevelFilename;
	
	@Override
	public void init(Map<String, String> params) {
		String version = params.get(PARAM_VERSION);
		systemOutputFilename = "BEwT_E-sys.scr";
		docLevelFilename = "BEwT_E-doc.scr";
		segmentLevelFilename = "BEwT_E-seg.scr";	
	}
	
	@Override
	public boolean doSomething(List<String> topics, 
								Map<String, Double> systemToScore, 
								Map<String, Map<String, Double>> topicToSystemToScore,
								double[] ruleWeights, 
								double[] transformWeights, 
								Map<String, Integer> transformNameToBitIndex, 
								IntIntHashMap ruleToWeightIndex, 
								IntIntHashMap bitIndexToWeightIndex) {
		try {
			writeSystemLevelScores(systemToScore);
			writeDocumentLevelScores(topics, topicToSystemToScore);
			writeSegmentLevelScores(topics, topicToSystemToScore);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			System.err.println("Saving of scores has failed");
		}
		return false;
	}
	
	private void writeSystemLevelScores(Map<String, Double> systemToScore) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(systemOutputFilename));
		List<String> keys = BEUtils.sortScores(systemToScore.keySet(), systemToScore);	
		
		for (String setPlusSys : keys) {
			String[] split = setPlusSys.split("\\+");
			writer.println(split[0] + "\t" + split[1] + "\t" + systemToScore.get(setPlusSys));
		}
		
		/*for (String system : keys) {
			writer.println(system + "\t" + result.systemToScore.get(system));
		}*/
		writer.close();
	}
	
	private void writeDocumentLevelScores(List<String> topics, 
			Map<String, Map<String, Double>> topicToSystemToScore) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(docLevelFilename));
		// Split by doc
		Map<String, Map<String, Map<String, Double>>> setPlusDocToSystemToTopicToScore = new HashMap<String, Map<String, Map<String, Double>>>();
		for(String topic : topics) {
			Map<String, Double> systemToScore = topicToSystemToScore.get(topic);
			String[] split = topic.split("\\+");
			String set = split[0];
			String doc = split[1].replace('Ɣ', '/');
			String setPlusDoc = set + "+" + doc;
			Map<String, Map<String, Double>> systemToTopicToScore = setPlusDocToSystemToTopicToScore.get(setPlusDoc);
			if(systemToTopicToScore == null) {
				setPlusDocToSystemToTopicToScore.put(setPlusDoc, systemToTopicToScore = new HashMap<String, Map<String,Double>>());
			}
			
			for(String sys : systemToScore.keySet()) {
				Map<String, Double> topicToScore = systemToTopicToScore.get(sys);
				if(topicToScore == null) {
					systemToTopicToScore.put(sys, topicToScore = new HashMap<String, Double>());
				}
				
				topicToScore.put(topic, systemToScore.get(sys));
			}
		}
		
		List<String> setPlusDocList = new ArrayList<String>(setPlusDocToSystemToTopicToScore.keySet());
		Collections.sort(setPlusDocList);
		for(String setPlusDoc : setPlusDocList) {
			String[] split = setPlusDoc.split("\\+");
			String setId = split[0];
			String docId = split[1].replace('Ɣ', '/');
			
			Map<String, Map<String, Double>> systemToTopicToScore = setPlusDocToSystemToTopicToScore.get(setPlusDoc);
			List<String> sysList = new ArrayList<String>(systemToTopicToScore.keySet());
			Collections.sort(sysList);
			for(String sys : sysList) {
				
				Map<String, Double> topicToScore = systemToTopicToScore.get(sys);
				double total = 0.0;
				for(String topic : topicToScore.keySet()) {
					total+= topicToScore.get(topic);
				}
				total /= topicToScore.size();
				writer.println(setId + "\t" + sys.split("\\+")[1] + "\t" + docId + "\t" + total);
			}
			
		}
		
		writer.close();
	}
	
	private void writeSegmentLevelScores(List<String> topics, 
			Map<String, Map<String, Double>> topicToSystemToScore) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(segmentLevelFilename));
		for(String topic : topics) {
			Map<String, Double> systemToScore = topicToSystemToScore.get(topic);
			String[] split = topic.split("\\+");
			String setId = split[0];
			String docId = split[1];
			String segmentId = split[2];
			List<String> setList = new LinkedList<String>(systemToScore.keySet());
			Collections.sort(setList);
			for(String setPlusSys : setList) {
				String system = setPlusSys.split("\\+")[1];
				writer.println(setId + "\t" + system + "\t" + docId + "\t" + segmentId + "\t" + systemToScore.get(setPlusSys));
				//writer.println(setId + "+" + docId + "+" + segmentId + "+" + system + "\t" + systemToScore.get(setPlusSys));
			}
		}
		writer.close();
	}
	
}