package bewte.endanalysis;

import java.util.List;
import java.util.Map;

import foal.map.IntIntHashMap;

/**
 * The default, do nothing EndAnalyzer
 */
public class DoNothingEndAnalyzer extends AbstractEndAnalyzer {
	
	 public boolean doSomething(List<String> topics, Map<String, Double> systemToScore, Map<String, Map<String, Double>> topicToSystemToScore, double[] coeffs, double[] transformWeights, 
				Map<String, Integer> transformNameToBitIndex, IntIntHashMap ruleToWeightIndex, IntIntHashMap bitIndexToWeightIndex) {
		 return false;
	 }
	
}