package bewte.endanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foal.map.IntIntHashMap;

public abstract class AbstractEndAnalyzer implements EndAnalyzer {
	
	protected String mAutoSystemsPattern;
	protected String mRefSystemsPattern;
	
	public void setSystemPatterns(String autoSystemsPattern, String refSystemsPattern) {
		mAutoSystemsPattern = autoSystemsPattern;
		mRefSystemsPattern = refSystemsPattern;
	}
	
	public void init(Map<String, String> params) throws IOException {
	}
	
	public static AbstractEndAnalyzer createEnder(File configFile) throws Exception {
		AbstractEndAnalyzer ender = null;
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		String line = null;
		Map<String, String> params = new HashMap<String, String>();
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.startsWith("#") && !line.equals("")) {
				// classname
				if(ender == null) {
					ender = (AbstractEndAnalyzer)Class.forName(line).newInstance();
				}
				// must be a param
				else {
					String[] split = line.split("=");
					params.put(split[0], split[1]);
				}
			}
		}
		reader.close();
		ender.init(params);
		return ender;
	}
	
	abstract public boolean doSomething(List<String> topics, Map<String, Double> systemToScore, Map<String, Map<String, Double>> topicToSystemToScore, double[] coeffs, double[] transformWeights, 
			Map<String, Integer> transformNameToBitIndex, IntIntHashMap ruleToWeightIndex, IntIntHashMap bitIndexToWeightIndex);
	
}