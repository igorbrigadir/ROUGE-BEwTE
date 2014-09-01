package bewte.endanalysis;

import java.util.List;
import java.util.Map;

import foal.map.IntIntHashMap;

public interface EndAnalyzer {
	
	// this should be removed
	//public void setSystemPatterns(String autoSystemsPattern, String refSystemsPattern);
	public boolean doSomething(List<String> topics, Map<String, Double> systemToScore, Map<String, Map<String, Double>> topicToSystemToScore, double[] coeffs, double[] transformWeights, 
			Map<String, Integer> transformNameToBitIndex, IntIntHashMap ruleToWeightIndex, IntIntHashMap bitIndexToWeightIndex);
	
}