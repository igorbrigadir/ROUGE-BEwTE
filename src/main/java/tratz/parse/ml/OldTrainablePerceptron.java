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
 

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tratz.types.FloatArrayList;
import tratz.types.IntArrayList;

/**
 * An older version of the TrainablePerceptron class that stores that actual feature
 * Strings instead of just hashes+checksums. Hasn't been used in a while so it may
 * not be fully functional with the rest of the system.
 *
 */
public class OldTrainablePerceptron extends AbstractParseModel {
	
	public static final long serialVersionUID = 1;
	
	private int count;
	private List<Entry> mEntries = new ArrayList<Entry>();
	private int numEntries;
	private Map<String, Integer> mFeatToIndex;
	
	public static class DoubleHolder implements Serializable {
		public final static long serialVersionUID = 1;
	}
	private static class Entry implements Serializable {
		public final static long serialVersionUID = 1;
		public int classOne;
		public FloatArrayList w = null;// = new FloatArrayList(1);
		public FloatArrayList w2 = null;//new FloatArrayList(1);
		public IntArrayList c = null;//new IntArrayList(1);
	}

	
	public void printStats(PrintWriter writer, CharSequence feat) throws IOException {
		int featIndex = mFeatToIndex.get(new String(feat.toString()));
		Entry entry = mEntries.get(featIndex);
		if(entry.c.size() == 1) {
			writer.print("\t" + entry.w.get(0)+":"+entry.w2.get(0)+":"+entry.c.get(0));
		}
		else if(entry.c.size() > 1) {
			for(int i = 0; i < entry.c.size(); i++) {
				writer.print("\t" + entry.w.get(i)+":"+entry.w2.get(i)+":"+entry.c.get(i));
			}
		}
	}
	
	public OldTrainablePerceptron() {
		
	}
	
	public OldTrainablePerceptron(List<String> actions) {
		mEntries.add(new Entry()); // dumby object at index 0
		mActions = new ArrayList<String>(actions);
		mActionToIndex = new HashMap<String, Integer>();
		mFeatToIndex = new HashMap<String, Integer>();
	}
	
	public int getIndex(String feat, boolean add) {
		Integer index = mFeatToIndex.get(feat);
		if(index != null) {
			return index;
		}
		if(add) mFeatToIndex.put(feat, index = mFeatToIndex.size()+1);
		return index;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public void updateFeature(int actionIndex, int feat, double change) {
		int numEntries = mEntries.size();
		
		if(feat >= numEntries) {
			for(int j = 0; j < feat-numEntries+1; j++) {
				mEntries.add(new Entry()); // expand to capacity
				if(numEntries % 100 == 0) {
					System.err.println("Entries: " + numEntries);
				}
			}
		}
		Entry entry = mEntries.get(feat);
		
		if(entry.w == null) {
			entry.w = new FloatArrayList(1);
			entry.w2 = new FloatArrayList(1);
			entry.c = new IntArrayList();
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
		final int numFeats = feats.size();
		
		int numEntries = mEntries.size();
		
		for(int i = 0; i < numFeats; i++) {
			int feat = feats.get(i);
			if(feat >= numEntries) {
				for(int j = 0; j < feat-numEntries+1; j++) {
					mEntries.add(new Entry()); // expand to capacity
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
			Integer index = mActionToIndex.get(actions.get(i));
			indices[i] = index == null ? -1 : 0;
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
	
	public Map<String, Integer> getActionToIndex() {
		return mActionToIndex;
	}
	
	public float getValue(int featIndex, int actionIndex) {
		Entry entry = mEntries.get(featIndex);
		int entrySize = entry.c.size();
		try {
		float retValue = 0;
		if(entrySize > 1) {
			float w = entry.w.get(actionIndex);
			float w2 = entry.w2.get(actionIndex);
			int c = entry.c.get(actionIndex);
			return (w2 + w * (count-c));
		}
		else {
			if(entry.classOne == actionIndex) {
			float w = entry.w.get(0);
			float w2 = entry.w2.get(0);
			int c = entry.c.get(0);
			return w2 + w * (count-c);
			}
			else {
				return 0;
			}
		}
		}
		catch(IndexOutOfBoundsException iobe) {
			return 0;
		}
	}
	
	public final void score(List<String> actions, IntArrayList feats, int[] indices, double[] scores) {
		final int numActions = actions.size();
		//double[] scores = new double[actions.size()];
		final int numFeats = feats.size();
		final int numEntries = mEntries.size();
		for(int i = 0; i < numActions; i++) {
			Integer index = mActionToIndex.get(actions.get(i));;
			indices[i] = index == null ? -1 : index;
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
	
	public Map<String, Map<String, List<String>>> getPosPosMap() {
		return mPosPosActs;
	}
	
}