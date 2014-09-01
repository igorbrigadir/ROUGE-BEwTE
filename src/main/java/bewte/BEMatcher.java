package bewte;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import foal.list.IntArrayList;
import foal.map.IntDoubleHashMap;
import foal.map.IntIntHashMap;
import foal.map.IntObjectHashMap;

import bewte.scoring.TallyFunction;

import mathalgo.WeightedAssignmentSolver;

/**
 * Used for performing the BE matching between two sets of BEs. 
 *
 */
public class BEMatcher {
	
	private List<BE> mBEList1;
	private IntObjectHashMap<IntArrayList> mPart1ToIndex;
	private int[] mPart1StrengthIndices;
	
	private List<BE> mBEList2;
	private IntObjectHashMap<IntArrayList> mPart2ToIndex;
	private int[] mPart2StrengthIndices;
	
	/** Contains edges between BEs from the two lists and a mapping of the transforms (encoded in the BitSet) used 
	 *  originally used to map the BEs to each other
	 */
	private IntObjectHashMap<IntObjectHashMap<List<BitSet>>> mEdgeTable;
	
	/** BE extraction rule weights */
	private double[] mStrengths;
	
	private IntIntHashMap mBeToModelFrequency;
	
	public BEMatcher(double[] strengths, final List<BE> beList1, final List<BE> beList2, IntIntHashMap beToModelFrequency) {
		mBeToModelFrequency = beToModelFrequency;
		mStrengths = strengths;
		
		mBEList1 = beList1; final int numPart1 = beList1.size();
		mPart1ToIndex = new IntObjectHashMap<IntArrayList>(numPart1);
		mPart1StrengthIndices = new int[numPart1];
		
		mBEList2 = beList2; final int numPart2 = beList2.size();
		mPart2ToIndex = new IntObjectHashMap<IntArrayList>(numPart2);
		mPart2StrengthIndices = new int[numPart2];
		
		mEdgeTable = new IntObjectHashMap<IntObjectHashMap<List<BitSet>>>(numPart1);
		
		populate(mBEList1, mPart1ToIndex, mPart1StrengthIndices);
		populate(mBEList2, mPart2ToIndex, mPart2StrengthIndices);
	}
	
	private static void populate(List<BE> beList, IntObjectHashMap<IntArrayList> partToIndex, int[] strengthIndices) {
		final int numBEs = beList.size();
		for(int i = 0; i < numBEs; i++) {
			BE be = beList.get(i);
			int equivId = be.getEquivalentId();
			IntArrayList equivList = partToIndex.get(equivId);
			if(equivList == null) {
				partToIndex.put(equivId, equivList = new IntArrayList());
			}
			equivList.add(i);
			strengthIndices[i] = be.getCoeff()-1;
		}
	}
	
	public void updateEdge(BE be1, BE be2, BitSet bs) {
		// I forget, but I think the equivList will never be of size > 1 if duplicates are ignored
		IntArrayList equivList = mPart2ToIndex.get(be2.getEquivalentId());
		if(equivList != null) {
			IntArrayList part1UniqueList = mPart1ToIndex.get(be1.getEquivalentId());
			int numPart1 = part1UniqueList.size();
			
			for(int i = 0; i < numPart1; i++) {
				int i1 = part1UniqueList.get(i);
				IntObjectHashMap<List<BitSet>> links = mEdgeTable.get(i1);
				if(links == null) {
					mEdgeTable.put(i1, links = new IntObjectHashMap<List<BitSet>>());
				}
				int numEquivIds = equivList.size();
				for(int j = 0; j < numEquivIds; j++) {
					int i2 = equivList.get(j);
					List<BitSet> current = links.get(i2);
					if(current == null) {
						links.put(i2, current = new LinkedList<BitSet>());
						current.add(bs);
					}
					else {
						boolean isSuperSet = false;
						for(BitSet bsPrior : current) {
							BitSet copy = new BitSet();
							copy.or(bs);
							copy.or(bsPrior);
							if(copy.equals(bs)) {
								isSuperSet = true;
								break;
							}
						}
						// There is no point in adding a link that has a super set
						// of an existing set of transforms since it could not have a 
						// a higher weight (because no transform weight is > 1.0)
						if(!isSuperSet) {
							current.add(bs);
						}
					}
					//links.put(i2, Math.max(currentScore, bs));
				}
			}
		}
	}

	/**
	 * 
	 */
	public Object[] solve(IntIntHashMap bitIndexToWeightIndex, double[] transformWeights, TallyFunction tallyFunction) {
		
		// Create the mapping (left to right to matchWeight)
		IntObjectHashMap<IntDoubleHashMap> edgeMap = new IntObjectHashMap<IntDoubleHashMap>();
		IntArrayList keys = mEdgeTable.keys();
		final int numKeys = keys.size();
		for(int i = 0; i < numKeys; i++) {
			int key = keys.get(i);
			IntDoubleHashMap map = edgeMap.get(key);
			if(map == null) {
				edgeMap.put(key, map = new IntDoubleHashMap());
			}
			IntObjectHashMap<List<BitSet>> links = mEdgeTable.get(key);
			IntArrayList jobs = links.keys();
			final int numJobs = jobs.size();
			for(int j = 0; j < numJobs; j++) {
				int job = jobs.get(j);
				List<BitSet> bitSets = links.get(job);
				double max = 0.0;
				for(BitSet bs : bitSets) {
					// Score is BEWEIGHT * PRODUCT(WEIGHT OF EACH TRANSFORM USED IN ORDER TO CREATE MAPPING)
					// Hence, an exact match gets whatever the tallyFunction says the weight of the be is
					double score = tallyFunction.tally((double)mBeToModelFrequency.get(mBEList2.get(job).getEquivalentId()),mBEList2.get(job));
					for (int t = bs.nextSetBit(0); t >= 0; t = bs.nextSetBit(t+1)) {
						score *= transformWeights[bitIndexToWeightIndex.get(t)];
     				} 
					if(score > max) {
						max = score;
					}
				}
				if(max > 0.0) { // should always be true?
					map.put(job, max * mStrengths[mPart2StrengthIndices[job]]);
				}
			}
		}
		
		// 
		int[][] assignments = WeightedAssignmentSolver.assignment(mBEList1.size(), mBEList2.size(), edgeMap);
		double result = 0.0;
		List<BE> matchedBEs = new ArrayList<BE>(assignments.length);
		for(int[] assignment : assignments) {
			/*BE be1 = mBEList1.get(assignment[0]);
			BE be2 = mBEList2.get(assignment[1]);
			if(!be1.toTextString().toLowerCase().equals(be2.toTextString().toLowerCase())) {
				System.err.println(be1.getRule()+":"+be1.toTextString() + " --> " + be2.getRule()+":"+be2.toTextString());
			}*/
			//System.err.println(part1Size + " " + part2Size + " " + assignment[0] + " " + assignment[1]);
			// Seems like the strengths might be getting counted twice...
			result +=  mStrengths[mPart2StrengthIndices[assignment[1]]] * edgeMap.get(assignment[0]).get(assignment[1]);
			matchedBEs.add(mBEList2.get(assignment[1]));
		}
		
		// Sum up the total weight of the left side of the graph
		double part1Weight = calcGraphSideWeight(mBEList1, mPart1StrengthIndices, tallyFunction);
		// Sum up the total weight of the right side of the graph
		double part2Weight = calcGraphSideWeight(mBEList2, mPart2StrengthIndices, tallyFunction);
		
		double precision = part1Weight == 0 ? 0 : result / part1Weight;
		double recall = part2Weight == 0 ? 0 : result / part2Weight;
		return new Object[]{precision, recall, matchedBEs};
	}
	
	private double calcGraphSideWeight(List<BE> beList, int[] strengthIndices, TallyFunction tallyFunction) {
		double graphSideWeight = 0;
		final int part = beList.size();
		for(int i = 0; i < part; i++) {
			// get the weight of the BE based upon frequency (1.0 for Binary tally function)
			double freqWeight = tallyFunction.tally((double)mBeToModelFrequency.get(beList.get(i).getEquivalentId()),beList.get(i));
			graphSideWeight += mStrengths[strengthIndices[i]] * freqWeight;
		}
		return graphSideWeight;
	}
	
}