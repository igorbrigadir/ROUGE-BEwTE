/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package tratz.parse.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tratz.parse.ml.TrainablePerceptron.Entry;
import tratz.types.ByteArrayList;
import tratz.types.ChecksumMap;
import tratz.types.IntArrayList;
import tratz.types.ShortArrayList;
import tratz.types.ChecksumMap.TwoPartKey;

/**
 * A more compact and faster parsing model. These objects are created from
 * existing trained models.
 */
public class FinalizedParseModel extends AbstractParseModel {

	private static final long serialVersionUID = 1L;
	
	private byte[] mSparseClasses;
	private short[] mSparseEntries;
	private short[] mDenseEntries;
	private int mNumActions;
	
	private ChecksumMap<String> mFeatToInd;
	public FinalizedParseModel(List<String> actions,
			Map<String, Integer> actionToIndex,
			Map<String, Map<String, List<String>>> posPosActs,
			ChecksumMap<String> featToInd,
			int count,
			ArrayList<Entry> originalEntries, /* NOTE: entries will get deleted to save memory */
			double sizeToKeep) {
		mActions = actions;
		mActionToIndex = actionToIndex;
		mPosPosActs = posPosActs;
		
		int numActions = actionToIndex.size()+1;// need to add 1... maybe indices were starting at 1 instead of 0?

		mNumActions = numActions;
		
		double maxAbs = 0;
		for(Entry entry : originalEntries) {
			if(entry.w != null) {
				int length = entry.w.size();
				for(int i = 0; i < length; i++) {
					double w = entry.w.get(i);
					double w2 = entry.w2.get(i);
					int c = entry.c.get(i);
					double score = (w2 + w * (count-c))/count;
					maxAbs = Math.max(maxAbs, Math.abs(score));
				}
			}
		}
		
		Map<TwoPartKey, Integer> keyToIndexMap = featToInd.getKeyToIndexMap();
		int maxIndex = 0;
		for(Integer val : keyToIndexMap.values()) {
			maxIndex = Math.max(val, maxIndex);
		}
		System.err.println("Max index: " + maxIndex + " Number of keys: " + keyToIndexMap.size());
		System.err.println("Number of entries: " + originalEntries.size());
		
		List<TwoPartKey> keptKeys = new ArrayList<TwoPartKey>();
		final Map<TwoPartKey, Double> keyToScoreMap = new HashMap<TwoPartKey, Double>();
		//List<TwoPartKey> keysToRemove = new ArrayList<TwoPartKey>();
		for(TwoPartKey key : keyToIndexMap.keySet()) {
			int index = keyToIndexMap.get(key);
			double score = 0;
			if(index < originalEntries.size()) {
				Entry entry = originalEntries.get(index);
				if(entry.w != null) {
					int length = entry.w.size();
					for(int i = 0; i < length; i++) {
						double val = (((entry.w2.get(i) + entry.w.get(i) * (count-entry.c.get(i)))/count)/maxAbs);
						score += Math.abs(val);
					}
				}
			}
			keyToScoreMap.put(key, score);
			if(score != 0) {
//				keysToRemove.add(key);
				keptKeys.add(key);
			}
		}
		
		System.err.println("Number of features where sum(abs(feature_weights)) > 0.0: " + keptKeys.size() + " out of " + keyToIndexMap.size());
		List<TwoPartKey> keys = new ArrayList<TwoPartKey>(keyToScoreMap.keySet());
		//System.err.println("Sorting...");
		Collections.sort(keys, new Comparator<TwoPartKey>() {
			public int compare(TwoPartKey one, TwoPartKey two) {
				double score1 = keyToScoreMap.get(one);
				double score2 = keyToScoreMap.get(two);
				if(score2 > score1) {
					return 1;
				}
				else if(score2 < score1) {
					return -1;
				}
				else if(one.hash-two.hash != 0){
					return one.hash-two.hash;
				}
				else {
					return one.checksum - two.checksum;
				}
			}
		});
		
		
		int numKeysToKeep = (int)(keptKeys.size() * sizeToKeep);
		//System.err.println("Minimum skrinkage factor: " + sizeToKeep + " => maximum of: " + numKeysToKeep + " non-zero keys to keep.");
		numKeysToKeep = Math.min(numKeysToKeep, keptKeys.size());
		System.err.println("Keeping " + numKeysToKeep + " keys");
		keys = keys.subList(0, numKeysToKeep);
		ChecksumMap<String> newFeatToInd = new ChecksumMap<String>();
		
		System.err.println("Max sum(abs(feature_weights)): " + maxAbs);
		
		int numEntries = keys.size();
		//int numEntries = originalEntries.size();
		
		ByteArrayList sparseClasses = new ByteArrayList(numEntries/3);
		ShortArrayList sparseEntries = new ShortArrayList(numEntries/3);
		ShortArrayList denseEntries = new ShortArrayList(numEntries*20);
		int sparseIndex = 1; // start at 1 to avoid 0/-0 issue
		int denseIndex = 0;
		System.err.println("Creating new entries");
		for(int e = 0; e < numEntries; e++) {
			//Entry entry = originalEntries.get(e);
			TwoPartKey key = keys.get(e);
			int oldIndex = keyToIndexMap.get(key);
			
			if(oldIndex >= originalEntries.size()) {
				//System.err.println("Unexpected situation: index (" + oldIndex + ") exceeds number of original entries: " + originalEntries.size());
				//System.err.println("This is likely indicative of a bug someplace. The only other possibility I can think of is that all the weights for this feature are 0, but that's unlikely.");
				//System.err.println("Sum of absolute value of weights: " + keyToScoreMap.get(key));
				continue;
			}
			
			Entry entry = originalEntries.get(oldIndex);
			originalEntries.set(oldIndex, null); // set to null to conserve memory
			
			
			if(entry.w != null) {
				//n.mWeights = new short[entry.w.size()];
				
				int index;
				if(entry.w.size() == 1) {
					if(entry.classOne > Byte.MAX_VALUE) {
						System.err.println("DANGER: max byte value exceeded for action index");
					}
					double scaledValue = (((entry.w2.get(0) + entry.w.get(0) * (count-entry.c.get(0)))/count)/maxAbs)*(Short.MAX_VALUE-100);
					sparseClasses.add((byte)entry.classOne);
					sparseEntries.add((short)scaledValue);
					index = -sparseIndex;
					sparseIndex++;
					
				}
				else {
					index = denseIndex;
					denseIndex++;
					
					int baseIndex = index*numActions;
					int width = entry.w.size();
					if(width > numActions) {
						System.err.println("DANGER: more weight entries for feat than there are actions! " + width + " vs " + numActions);
					}
					for(int i = 0; i < width; i++) {
						double scaledValue = (((entry.w2.get(i) + entry.w.get(i) * (count-entry.c.get(i)))/count)/maxAbs)*(Short.MAX_VALUE-100);
						if(Math.abs(scaledValue) > Short.MAX_VALUE) {
							System.err.println("DANGER: max short value exceeded for feature weight: " + scaledValue + " " + Short.MAX_VALUE + " " + Short.MIN_VALUE) ;
						}
						
						//denseEntries.ensureCapacity(baseIndex+i+1);
						for(int j = denseEntries.size(); j < baseIndex+i; j++) {
							denseEntries.add((short)0);
						}
						denseEntries.add((short)scaledValue);
					}
				}
				newFeatToInd.put(key.hash, key.checksum, index);
			}
		}
		//Need to pad the end with 0s
		int padding = denseEntries.size()%mNumActions;
		for(int i = 0; i < padding; i++) {
			denseEntries.add((short)0);
		}
		
		mFeatToInd = newFeatToInd;
		
		mDenseEntries = denseEntries.toCompactArray();
		denseEntries = null;
		mSparseEntries = sparseEntries.toCompactArray();
		sparseEntries = null;
		mSparseClasses = sparseClasses.toCompactArray();
	}
	
	public int getIndex(String feat, boolean add) {
		return mFeatToInd.get(feat);
	}

	public void score(List<String> actions, IntArrayList feats,
			int[] indices, double[] scores) {
		final int numActions = actions.size();
		final int numFeats = feats.size();
		
		for(int i = 0; i < numActions; i++) {
			Integer val = mActionToIndex.get(actions.get(i));
			indices[i] = val == null ? -1 : val;
			scores[i] = 0;
		}
		for(int i = 0; i < numFeats; i++) {
			int feat = feats.get(i);
			
			//	int entrySize = entry.mWeights.length;
			if(feat >= 0) {
				for(int a = 0; a < numActions; a++) {
					int actionIndex = indices[a];
					if(actionIndex != -1) { // && actionIndex < entrySize) {
						int index = feat*mNumActions+actionIndex;
						if(index >= mDenseEntries.length) {
							// This appears to be happening... let's just ignore for the current release
							// TODO: FIX ME FIRST
						}
						else {
							scores[a] += mDenseEntries[index];//entry.mWeights[actionIndex];
						}
					}
				}
			}
			else {
				feat = (-feat-1);
				byte sparseClass = mSparseClasses[feat];
				for(int a = 0; a < numActions; a++) {
					if(indices[a] == sparseClass) {
						scores[a] += mSparseEntries[feat];//entry.mWeights[0];
						break;
					}
				}
			}
		}
	}

	public void scoreIntermediate(List<String> action, IntArrayList feats,
			int[] actionIndices, double[] scores) {
		
	}
	
	
}