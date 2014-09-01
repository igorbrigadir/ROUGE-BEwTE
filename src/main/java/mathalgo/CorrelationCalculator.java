package mathalgo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates Pearson and Spearman coefficients
 */
public class CorrelationCalculator {
	
	public static void main(String[] args) throws Exception {
	
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(3);
		File file1 = new File(args[0]);
		for(int i = 1; i < args.length; i++) {
			File file2 = new File(args[i]);
			double result = correlationCalculation(file1, file2, ".+", true);
			double result2 = correlationCalculation(file1, file2, "[0-9]+", true);
			double result3 = correlationCalculation(file1, file2, "[A-Z]+", true);
			
			double result4 = correlationCalculation(file1, file2, ".+", false);
			double result5 = correlationCalculation(file1, file2, "[0-9]+", false);
			double result6 = correlationCalculation(file1, file2, "[A-Z]+", false);
			System.err.println(format.format(result) + "\t" + 
					           format.format(result2)+ "\t"+ 
					           format.format(result3) + "\t" + 
					           format.format(result4) + "\t"+ 
					           format.format(result5) + "\t"+
					           format.format(result6) + "\t"+new File(args[i]).getName());
		}
	}
	
	public static double correlationCalculation(File file1, File file2, String pattern, boolean convertToRankMap) throws IOException {
		Map<String, Double> scoreMap1 = readScoreMap(file1);
		Map<String, Double> scoreMap2 = readScoreMap(file2);
		
		return calcScore(scoreMap1, scoreMap2, pattern, convertToRankMap);
	}
	
	private static Map<String, Double> convertToRankMap(final Map<String, Double> scoreMap) {
		Map<String, Double> rankMap = new HashMap<String, Double>();
		
		Map<Long, List<String>> valueToKeys = new HashMap<Long, List<String>>();
		for(String key : scoreMap.keySet()) {
			long value = (long)(1000000000l*scoreMap.get(key));
			List<String> keys = valueToKeys.get(value);
			if(keys == null) {
				valueToKeys.put(value, keys = new ArrayList<String>());
			}
			keys.add(key);
		}
		
		List<Long> valueList = new ArrayList<Long>(valueToKeys.keySet());
		Collections.sort(valueList);//, Collections.reverseOrder());
		//Collections.sort(valueList);
		
		int level = 0;
		for(Long d : valueList) {
			List<String> keys = valueToKeys.get(d);
			int numKeys = keys.size();
			double rank = (level + (level+numKeys-1))/2.0;
			for(int i = 0; i < keys.size(); i++) {
				rankMap.put(keys.get(i), rank);
				//System.err.println(keys.get(i) + " --> " + rank);
			}
			level+=numKeys;
		}
	
		return rankMap;
	}
	
	public static double calcScore(Map<String, Double> scoreMap1, 
								   Map<String, Double> scoreMap2,
								   String keyPattern,
								   boolean convertToRankMap) {
		
		scoreMap1 = new HashMap<String, Double>(scoreMap1);
		scoreMap2 = new HashMap<String, Double>(scoreMap2);
		Set<String> allKeys = new HashSet<String>();
		allKeys.addAll(scoreMap1.keySet());
		allKeys.addAll(scoreMap2.keySet());
		for(String key : allKeys) {
			if(!key.matches(keyPattern)) {
				scoreMap1.remove(key);
				scoreMap2.remove(key);
			}
		}
				
		if(scoreMap1.size() != scoreMap2.size()  || scoreMap1.size() != allKeys.size()) {
			System.err.println(CorrelationCalculator.class.getName() + " Warning: size mismatch, taking intersection of " + scoreMap1.size() + " and " + scoreMap2.size() + " keys");
			allKeys.retainAll(scoreMap1.keySet());
			allKeys.retainAll(scoreMap2.keySet());
			Map<String, Double> newScoreMap1 = new HashMap<String, Double>();
			Map<String, Double> newScoreMap2 = new HashMap<String, Double>();
			for(String key : allKeys) {
				newScoreMap1.put(key, scoreMap1.get(key));
				newScoreMap2.put(key, scoreMap2.get(key));
			}
			scoreMap1 = newScoreMap1;
			scoreMap2 = newScoreMap2;
			System.err.println("New size: " + scoreMap1.size() + " " + scoreMap2.size());
		}
		
		if(convertToRankMap) {
			scoreMap1 = convertToRankMap(scoreMap1);
			scoreMap2 = convertToRankMap(scoreMap2);
		}
		
		double xMean = 0.0;
		for(Double val : scoreMap1.values()) {
			xMean += val;
		}
		xMean /= scoreMap1.size();
		double yMean = 0.0;
		for(Double val : scoreMap2.values()) {
			yMean += val;
		}
		yMean /= scoreMap2.size();
		double xStd = 0.0;
		for(Double val : scoreMap1.values()) {
			xStd += Math.pow(val-xMean, 2);
		}
		xStd = Math.sqrt(xStd*1.0/(scoreMap1.size()-1));
		double yStd = 0.0;
		for(Double val : scoreMap2.values()) {
			yStd += Math.pow(val-yMean, 2);
		}
		yStd = Math.sqrt(yStd* 1.0/(scoreMap2.size()-1));
		
		double sum = 0.0;
		for(String key : scoreMap1.keySet()) {
			double x = scoreMap1.get(key);
			double y = scoreMap2.get(key);
			double z = ((x-xMean)/xStd) * ((y-yMean)/yStd);
			if(Double.isNaN(z)) {
				z = 0;
			}
			sum += z;
		}
		
		return sum / (scoreMap1.size()-1);
	}
	
	public static Map<String, Double> readScoreMap(File file) throws IOException {
		Map<String, Double> rankMap = new HashMap<String, Double>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("") && ! line.startsWith("#")) {
				String[] split = line.split("\\s+");
				rankMap.put(split[0], Double.parseDouble(split[1]));	
			}
		}
		reader.close();
		return rankMap;
	}
	
	
}