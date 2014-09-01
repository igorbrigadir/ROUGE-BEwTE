package bewte.scoring;

import foal.list.DoubleArrayList;

/**
 * Defines the interface for combining multiple scores
 */
public interface MultiScoreCombiner {
	public double score(DoubleArrayList precisionScores, DoubleArrayList recallScores, boolean isModelSummary);
}