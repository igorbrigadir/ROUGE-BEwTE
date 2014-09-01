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
 

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tratz.types.ChecksumMap;
import tratz.types.FloatArrayList;
import tratz.types.IntArrayList;

public class TrainablePerceptron extends AbstractParseModel {
	
	public static final long serialVersionUID = 1;
	
	private int count;
	private List<Entry> mEntries = new ArrayList<Entry>();
	private ChecksumMap<String> mFeatToInd;
	
	public static class Entry implements Serializable {
		public final static long serialVersionUID = 1;
		public int classOne;
		public FloatArrayList w = null;
		public FloatArrayList w2 = null;
		public IntArrayList c = null;
	}
	
	public TrainablePerceptron() {
		
	}
	
	public TrainablePerceptron(List<String> actions) {
		
		mActions = new ArrayList<String>(actions);
		mActionToIndex = new HashMap<String, Integer>();
		mFeatToInd = new ChecksumMap<String>();
		
		// dumby object at index 0
		mEntries.add(new Entry()); 
		mFeatToInd.put("blahblah**blah", 0);
	}
	
	public int getIndex(String feat, boolean add) {
		int index = mFeatToInd.get(feat);
		if(index != mFeatToInd.DEFAULT_NOT_FOUND_VALUE) {
			return index;
		}
		if(add) {
			mFeatToInd.put(feat, index = mFeatToInd.size());//+1
		}
		return index;
	}
	
	public ChecksumMap<String> getFeatToInd() {
		return mFeatToInd;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public void updateFeature(int actionIndex, int feat, double change) {
		int numEntries = mEntries.size();
		
		if(feat >= numEntries) {
			for(int j = 0; j < feat-numEntries+1; j++) {
				mEntries.add(new Entry()); // expand to capacity
				if(numEntries % 100000 == 0) {
					System.err.println("Entries: " + numEntries);
				}
			}
		}
		Entry entry = mEntries.get(feat);
		
		if(entry.w == null) {
			entry.w = new FloatArrayList(1);
			entry.w2 = new FloatArrayList(1);
			entry.c = new IntArrayList(1);
				entry.w2.add(0);
				entry.w.add((float)change);
				entry.c.add(count);
				entry.classOne = actionIndex;
			}
			else { 
				int numWeights = entry.w.size();
				if(numWeights == 1) {
				if(entry.classOne == actionIndex) {
					entry.w2.set(0, entry.w2.get(0)+entry.w.get(0)*(count-entry.c.get(0)));
					entry.w.set(0, entry.w.get(0)+(float)change);//w.put(feat, oldw+change);
					entry.c.set(0, count);
				}
				else {
					int oldClassOne = entry.classOne;
					int c = entry.c.get(0);
					double w = entry.w.get(0);
					double w2 = entry.w2.get(0);
					// Insert new, empty entries
					int maxIndex = Math.max(actionIndex, entry.classOne);
					entry.classOne = -1;
					if(numWeights <= maxIndex) {
						int numToAdd = maxIndex-numWeights+1;
						//entry.w2.ensureCapacity(maxIndex+1);
						//entry.w.ensureCapacity(maxIndex+1);
						//entry.c.ensureCapacity(maxIndex+1);
						for(int j = 0; j < numToAdd; j++) {
							entry.w2.add(0);
							entry.w.add(0);
							entry.c.add(0);
						}
					}
					// Move the old entry
					entry.w2.set(0,0);
					entry.w.set(0,0);
					entry.c.set(0,0);
					entry.w2.set(oldClassOne, (float)w2);
					entry.w.set(oldClassOne, (float)w);//w.put(feat, oldw+change);
					entry.c.set(oldClassOne, c);
					// Update the new entry
					double oldW = entry.w.get(actionIndex);
					entry.w2.set(actionIndex, (float)(entry.w2.get(actionIndex)+oldW*(count-entry.c.get(actionIndex))));
					entry.w.set(actionIndex, (float)(oldW+change));//w.put(feat, oldw+change);
					entry.c.set(actionIndex, count);
				}
			}
			else {
				// Insert new empty entries as necessary
				if(numWeights <= actionIndex) {
					int numToAdd = actionIndex-numWeights+1;
					//entry.w2.ensureCapacity(actionIndex+1);
					//entry.w.ensureCapacity(actionIndex+1);
					//entry.c.ensureCapacity(actionIndex+1);
					for(int j = 0; j < numToAdd; j++) {
						entry.w2.add(0);
						entry.w.add(0);
						entry.c.add(0);
					}
				}
				// Update appropriate entry
				double oldW = entry.w.get(actionIndex);
				entry.w2.set(actionIndex, (float)(entry.w2.get(actionIndex)+oldW*(count-entry.c.get(actionIndex))));
				entry.w.set(actionIndex, (float)(oldW+change));//w.put(feat, oldw+change);
				entry.c.set(actionIndex, count);
			}
			}
		
	}
	
	public void update(String action, IntArrayList feats, double change) {
		int actionIndex = getActionIndex(action, true);
		//System.err.println("ActionIndex: " + action + " " + actionIndex + " " + change);
		final int numFeats = feats.size();
		
		int numEntries = mEntries.size();
		
		for(int i = 0; i < numFeats; i++) {
			int feat = feats.get(i);
			if(feat >= numEntries) {
				for(int j = 0; j < feat-numEntries+1; j++) {
					mEntries.add(new Entry()); // expand to capacity
				}
				if(mEntries.size() % 100000 == 0) {
					System.err.println("Entries: " + mEntries.size());
				}
			}
			Entry entry = mEntries.get(feat);
			
			if(entry.w == null) {
				entry.w = new FloatArrayList(1);
				entry.w2 = new FloatArrayList(1);
				entry.c = new IntArrayList(1);
				entry.w2.add(0);
				entry.w.add((float)change);
				entry.c.add(count);
				entry.classOne = actionIndex;
			}
			else {
				int numWeights = entry.w.size();
				if(numWeights == 1) {
			
				if(entry.classOne == actionIndex) {
					entry.w2.set(0, entry.w2.get(0)+entry.w.get(0)*(count-entry.c.get(0)));
					entry.w.set(0, entry.w.get(0)+(float)change);//w.put(feat, oldw+change);
					entry.c.set(0, count);
				}
				else {
					int oldClassOne = entry.classOne;
					int c = entry.c.get(0);
					double w = entry.w.get(0);
					double w2 = entry.w2.get(0);
					// Insert new, empty entries
					int maxIndex = Math.max(actionIndex, entry.classOne);
					entry.classOne = -1;
					if(numWeights <= maxIndex) {
						int numToAdd = maxIndex-numWeights+1;
						//entry.w2.ensureCapacity(maxIndex+1);
						//entry.w.ensureCapacity(maxIndex+1);
						//entry.c.ensureCapacity(maxIndex+1);
						for(int j = 0; j < numToAdd; j++) {
							entry.w2.add(0);
							entry.w.add(0);
							entry.c.add(0);
						}
					}
					// Move the old entry
					entry.w2.set(0,0);
					entry.w.set(0,0);
					entry.c.set(0,0);
					entry.w2.set(oldClassOne, (float)w2);
					entry.w.set(oldClassOne, (float)w);//w.put(feat, oldw+change);
					entry.c.set(oldClassOne, c);
					// Update the new entry
					double oldW = entry.w.get(actionIndex);
					entry.w2.set(actionIndex, (float)(entry.w2.get(actionIndex)+oldW*(count-entry.c.get(actionIndex))));
					entry.w.set(actionIndex, (float)(oldW+change));//w.put(feat, oldw+change);
					entry.c.set(actionIndex, count);
				}
			}
			else {
				// Insert new empty entries as necessary
				if(numWeights <= actionIndex) {
					int numToAdd = actionIndex-numWeights+1;
					//entry.w2.ensureCapacity(actionIndex+1);
					//entry.w.ensureCapacity(actionIndex+1);
					//entry.c.ensureCapacity(actionIndex+1);
					for(int j = 0; j < numToAdd; j++) {
						entry.w2.add(0);
						entry.w.add(0);
						entry.c.add(0);
					}
				}
				// Update appropriate entry
				double oldW = entry.w.get(actionIndex);
				entry.w2.set(actionIndex, (float)(entry.w2.get(actionIndex)+oldW*(count-entry.c.get(actionIndex))));
				entry.w.set(actionIndex, (float)(oldW+change));//w.put(feat, oldw+change);
				entry.c.set(actionIndex, count);
			}
		}
		}
	}
	

	
	public final void scoreIntermediate(List<String> actions, IntArrayList feats, int[] indices, double[] scores) {
		final int numActions = actions.size();
		//double[] scores = new double[actions.size()];
		final int numFeats = feats.size();
		final int numEntries = mEntries.size();
		for(int i = 0; i < numActions; i++) {
			Integer val = mActionToIndex.get(actions.get(i));
			indices[i] = val == null ? -1 : val;
			scores[i] = 0;
		}
		for(int i = 0; i < numFeats; i++) {
			int feat = feats.get(i);
			if(feat < numEntries) {
				Entry entry = mEntries.get(feat);
				int entrySize = entry.c == null ? 0 : entry.c.size();
				if(entrySize > 1) {
					for(int a = 0; a < numActions; a++) {
						int actionIndex = indices[a];
						if(actionIndex != -1 && actionIndex < entrySize) {
							scores[a] += entry.w.get(actionIndex);
						}
					}
				}
				else if(entrySize == 1) {
					for(int a = 0; a < numActions; a++) {
						if(indices[a] == entry.classOne) {
							scores[a] += entry.w.get(0);
							break;
						}
					}
				}
			}
		}
	}
	
	public final void score(List<String> actions, IntArrayList feats, int[] indices, double[] scores) {
		final int numActions = actions.size();
		//double[] scores = new double[actions.size()];
		final int numFeats = feats.size();
		final int numEntries = mEntries.size();
		for(int i = 0; i < numActions; i++) {
			Integer val = mActionToIndex.get(actions.get(i));
			indices[i] = val == null ? -1 : val;
			scores[i] = 0;
		}
		for(int i = 0; i < numFeats; i++) {
			int feat = feats.get(i);
			if(feat < numEntries) {
				Entry entry = mEntries.get(feat);
				int entrySize = entry.c == null ? 0 : entry.c.size();
				if(entrySize > 1) {
					for(int a = 0; a < numActions; a++) {
						int actionIndex = indices[a];
						if(actionIndex != -1 && actionIndex < entrySize) {
							double w = entry.w.get(actionIndex);
							double w2 = entry.w2.get(actionIndex);
							int c = entry.c.get(actionIndex);
							scores[a] += w2 + w * (count-c);//entry.w.get(actionIndex);
							
						}
					}
				}
				else if(entrySize == 1) {
					for(int a = 0; a < numActions; a++) {
						if(indices[a] == entry.classOne) {
							//scores[a] += entry.w.get(0);
							double w = entry.w.get(0);
							double w2 = entry.w2.get(0);
							int c = entry.c.get(0);
							scores[a] += w2 + w * (count-c);//
							break;
						}
					}
				}
			}
		}
	}
	
	public FinalizedParseModel createFinal(double amountToKeep) {
		return new FinalizedParseModel(mActions, mActionToIndex, mPosPosActs, mFeatToInd, count, (ArrayList<Entry>)mEntries, amountToKeep);
	}
	
	
}