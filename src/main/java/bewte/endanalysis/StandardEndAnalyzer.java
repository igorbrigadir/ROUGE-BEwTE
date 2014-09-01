package bewte.endanalysis;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import foal.map.IntIntHashMap;

import mathalgo.CorrelationCalculator;

/**
 *	Writes out the correlation coefficients and then exits.
 */
public class StandardEndAnalyzer extends AbstractEndAnalyzer {
	
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
		
		
		String truthParam = params.get(TRUTH_PARAM);
		if(truthParam != null && !truthParam.equals("null")) {
			mTruthScoreMap = CorrelationCalculator.readScoreMap(new File(truthParam));
		}
	}
	
	public boolean doSomething(List<String> topics, Map<String, Double> systemToScore, Map<String, Map<String, Double>> topicToSystemToScore, double[] coeffs, double[] transformWeights, 
			Map<String, Integer> transformNameToBitIndex, IntIntHashMap ruleToAlpha, IntIntHashMap bitIndexToWeightIndex) {
		//	 Calculate system level spearman/pearson coefficients
		double spearmanAuto = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, mAutoSystemsPattern, true);
		double spearmanAll = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, ".*", true);
		double spearmanRef = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, mRefSystemsPattern, true);
	
		double pearsonAuto = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, mAutoSystemsPattern, false);
		double pearsonAll = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, ".*", false);
		double pearsonRef = CorrelationCalculator.calcScore(mTruthScoreMap, systemToScore, mRefSystemsPattern, false);
		
		// Print out coefficients
		System.err.println(mLongStyle.format(pearsonAuto) + "\t" + mLongStyle.format(pearsonAll) + "\t" + mLongStyle.format(pearsonRef) + "\t" + mLongStyle.format(spearmanAuto)  + "\t" + mLongStyle.format(spearmanAll) + "\t" + mLongStyle.format(spearmanRef));
		
		// We are done
		return false;
	}
	
}