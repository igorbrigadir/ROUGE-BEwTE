package bewte.endanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import foal.map.IntIntHashMap;

import mathalgo.CorrelationCalculator;

/**
 *	Does some unsophisticated optimization
 */
public class OptimizingEndAnalyzer extends AbstractEndAnalyzer {
	
	public static final String TRUTH_PARAM = "TRUTH_FILE";
	
	private DecimalFormat mStyle = new DecimalFormat();		
	private DecimalFormat mLongStyle = new DecimalFormat();
	private DecimalFormat mIntegerStyle = new DecimalFormat();
	
	private Map<String, Double> mTruthScoreMap;
	
	public void init(Map<String, String> params) throws IOException {
		mStyle.setMinimumFractionDigits(3);
		mStyle.setMaximumFractionDigits(3);
		mStyle.setGroupingUsed(false);	
		mLongStyle.setMinimumFractionDigits(5);
		mLongStyle.setMaximumFractionDigits(5);
		mLongStyle.setGroupingUsed(false);
		mIntegerStyle.setMinimumIntegerDigits(2);
		mIntegerStyle.setGroupingUsed(false);
		
		
		//String truthParam = params.get(TRUTH_PARAM);
		//if(truthParam != null && !truthParam.equals("null")) {
			mTruthScoreMap = readTruth();//CorrelationCalculator.readScoreMap(new File(truthParam));
	//	}
		
	}
	
	public static Map<String, Double> readTruth() throws IOException {		
		Map<String, Double> truth = new HashMap<String, Double>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/stratz/Files/corpora/NISTMetricsMATR10Dev_v1.1/mt06/adequacy.csv"));
		String line = null;
		line = reader.readLine();
		while((line = reader.readLine()) != null) {
			String[] split = line.split(",");
			String tag = "Arabic_English+"+split[1] + "+" + split[2] + "+" + split[0];
			truth.put(tag, Double.parseDouble(split[5]));
		}
		reader.close();
		return truth;
	}
	
	double[] searchDirection;
	double[] prevTransformWeights;
	double scaleJumps = 100000;
	double previousCorrelation;
	Random rng = new Random(0);
	public boolean doSomething(List<String> topics, Map<String, Double> systemToScore, Map<String, Map<String, Double>> topicToSystemToScore, double[] coeffs, double[] transformWeights, 
			Map<String, Integer> transformNameToBitIndex, IntIntHashMap ruleToAlpha, IntIntHashMap bitIndexToWeightIndex) {
		Map<String, Double> segmentsToScores = new HashMap<String, Double>();
		for(String topic : topicToSystemToScore.keySet()) {
			//System.err.println("Topic: " + topic);
			Map<String, Double> sys2Score = topicToSystemToScore.get(topic);
			for(String sys : sys2Score.keySet()) {
				String coresys = sys.substring(sys.indexOf('+')+1);
				String tag = topic + "+"+coresys;
				//System.err.println("Sys: " + sys + " -> " + tag);
				Double score = sys2Score.get(sys);
				if(score == null) {
					System.err.println("Oddball");
					score = new Double(0);
				}
				segmentsToScores.put(tag, score);
			}
		}
		//	 Calculate system level spearman/pearson coefficients
		double spearmanAuto = CorrelationCalculator.calcScore(mTruthScoreMap, segmentsToScores, ".*system.*", true);
		double pearsonAuto = CorrelationCalculator.calcScore(mTruthScoreMap, segmentsToScores, ".*system.*", false);
		
		if(prevTransformWeights == null) {
			prevTransformWeights = new double[transformWeights.length];
			searchDirection = new double[transformWeights.length];
			for(int i = 0; i < searchDirection.length; i++) {
				searchDirection[i] = rng.nextDouble()/scaleJumps;
			}
		}
		else {
			if(pearsonAuto > previousCorrelation) {
				// search faster in current direction
				System.err.println("FORWARD!");
				for(int i = 0; i < searchDirection.length; i++) {
					searchDirection[i] *= 2;//= rng.nextDouble()/scaleJumps;
					transformWeights[i] += searchDirection[i];
				}	
			}
			else {
				pearsonAuto = previousCorrelation;
				// choose new direction
				System.err.println("NEW DIRECTION");
				int directionToMove = rng.nextInt(searchDirection.length);
				for(int i = 0; i < searchDirection.length; i++) {
					searchDirection[i] = i == directionToMove ? (.5-rng.nextDouble())/scaleJumps : 0;//rng.nextBoolean() ? (.5-rng.nextDouble())/scaleJumps : 0;
					transformWeights[i] = prevTransformWeights[i] + searchDirection[i];
				}
			}
		}
		
		// Print out coefficients
		System.err.println(mLongStyle.format(pearsonAuto) + "\t" + mLongStyle.format(spearmanAuto));
		System.arraycopy(transformWeights, 0, prevTransformWeights, 0, transformWeights.length);
		previousCorrelation = pearsonAuto;
		return true;
	}
	
}