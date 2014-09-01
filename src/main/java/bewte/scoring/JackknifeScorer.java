package bewte.scoring;

import foal.list.DoubleArrayList;

public class JackknifeScorer implements MultiScoreCombiner {
	
	public double score(DoubleArrayList precisionScores, DoubleArrayList recallScores, boolean isModelSummary) {
		double result;
		int numModels = precisionScores.size();

		// Single reference case
		if(numModels == 1) {
			if(isModelSummary) {
				result = 1.0;	
			}
			else {
				result = recallScores.get(0);
			}
		}
		// Multi-reference case
		else {
			// Average of other scores
			// NOTE: currently assumes self score to be less or equal to 0, which isn't intuitive and should be
			// changed in BEwT_E.java
			if(isModelSummary) {
				double max = 0.0;
				for(int i = 0; i < numModels; i++) {
					double rscore = recallScores.get(i);
					if(rscore > max) {
						max = rscore;
					}
				}
				result = max;
			}
			else {
				// Average of N mult-reference scores
				double total = 0.0;
				for(int i = 0; i < numModels; i++) {
					double max = 0.0;
					for(int j = 0; j < numModels; j++) {
						if(i != j) {
							double rscore = recallScores.get(j);
							if(rscore > max) {
								max = rscore;
							}
						}
					}
					total += max;
				}
				result = total / numModels;
			}
		}
		return result;
	}
	
}