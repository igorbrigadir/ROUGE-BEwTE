package bewte.endanalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import foal.map.IntIntHashMap;

import mathalgo.CorrelationCalculator;


public class NumberOfTopicsEndAnalyzer extends AbstractEndAnalyzer {
	
	public final static int MAX_TOPICS = 45;
	public static final String TRUTH_PARAM = "TRUTH_FILE";
	
	private Map<String, Double> truthScoreMap;
	
	@Override
	public void init(Map<String, String> params) throws IOException {
		String truthParam = params.get(TRUTH_PARAM);
		truthScoreMap = CorrelationCalculator.readScoreMap(new File(truthParam));
	}
	
	@Override
	public boolean doSomething(	List<String> topics, 
							   	Map<String, Double> systemToScore, 
							   	Map<String, Map<String, Double>> topicToSystemToScore, 
								double[] ruleWeights, 
								double[] transformWeights, 
								Map<String, Integer> transformNameToBitIndex, 
								IntIntHashMap ruleToWeightIndex, 
								IntIntHashMap bitIndexToWeightIndex) {
		
		try {
			PrintWriter writer = new PrintWriter(new FileWriter("dumby.txt"));
			System.err.println("FINISHING");
			Random rng = new Random(0);
		
			int numTopics = topics.size();
			int numToAverage = 5000;
			// The size of the subset
			for(int i = 0; i < numTopics; i++) {
				double average = 0.0;
				// Need to average several correlation scores, let's average 'numTopics' many
				for(int avg = 0; avg < numToAverage; avg++) {
					Map<String, Double> sysHashToScore = new HashMap<String, Double>();
					Map<String, Integer> sysHashToCount = new HashMap<String, Integer>();
					// Pick out the subset of topics to use
					List<String> copy = new ArrayList<String>(topics);
					for(int j = 0; j < i+1; j++) {
						String selectedTopic = copy.remove(rng.nextInt(copy.size()));
						Map<String, Double> scoreMap = topicToSystemToScore.get(selectedTopic);
						for(String sys : scoreMap.keySet()) {
							Double score = sysHashToScore.get(sys);
							Integer count = sysHashToCount.get(sys);
							sysHashToScore.put(sys, (score == null ? new Double(0) : score) + scoreMap.get(sys));
							sysHashToCount.put(sys, (count == null ? new Integer(0) : count) + 1);
						}
					}	
					Map<String, Double> scoreMap = new HashMap<String, Double>();
				
					// Convert to a score map
				
					for(String system : sysHashToScore.keySet()) {
						double score = sysHashToScore.get(system)/sysHashToCount.get(system);
						scoreMap.put(system, score);
					}
			
				
					// Get the correlation score
					double score = CorrelationCalculator.calcScore(truthScoreMap, scoreMap, ".*[0-9]", false);
					// add it to the running total
					average+= score;
				}
				// Finished correlation score determination for size 'i'
				String outline = ""+(average/numToAverage);
				System.err.println(outline);
				writer.println(outline);
			}
			writer.close();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return false;
	}
	
}