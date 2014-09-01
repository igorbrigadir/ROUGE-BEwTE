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

package tratz.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.types.ChecksumMap.TwoPartKey;

/**
 * Class for holding a linear classification model such as those trained using LIBLINEAR.
 */
public class LinearClassificationModel implements Serializable {
	public final static long serialVersionUID = 1;
	
	private List<float[]> mModel;
	private int[] mModelLabelOrder;
	private ClassDictionary mLabelAlphabet;
	private FeatureDictionary mAlphabet;
	
	public LinearClassificationModel(List<float[]> model,
									int[] modelLabelOrder,
									ClassDictionary labelAlphabet,
									FeatureDictionary alphabet) {
		mModel = model;
		mModelLabelOrder = modelLabelOrder;
		mLabelAlphabet = labelAlphabet;
		mAlphabet = alphabet;
	}
	
	public LinearClassificationModel createTrimmedModel(double amountToRemove) {
		assert(amountToRemove < 1.0 && amountToRemove > 0.0);
		
		ClassDictionary labelAlphabet = new ClassDictionary(mLabelAlphabet);
		
		Map<TwoPartKey, Integer> featset = mAlphabet.getKeySet();
		
		final Map<TwoPartKey, Float> featToAbsoluteValue = new HashMap<TwoPartKey, Float>();
		int numClasses = mModel.size();
		int maxVectorWidth = 0;
		for(float[] vector : mModel) {
			if(vector.length > maxVectorWidth) {
				maxVectorWidth = vector.length;
			}
		}
		List<TwoPartKey> featList = new ArrayList<TwoPartKey>();
		for(TwoPartKey feat : featset.keySet()) {
			float abs = 0;
			int featIndex = featset.get(feat);//mAlphabet.lookupIndex(feat, false);
			//if(featIndex < maxVectorWidth) {
				for(int i = 0; i < numClasses; i++) {
					abs += Math.abs(mModel.get(i)[featIndex]);
				
				}
			if(abs > 0) {
				featToAbsoluteValue.put(feat, abs);
				featList.add(feat);
			}
			
		}
		
		Collections.sort(featList, new Comparator<TwoPartKey>() {
			public int compare(TwoPartKey s1, TwoPartKey s2) {
				float value1 = (float)featToAbsoluteValue.get(s1);
				float value2 = (float)featToAbsoluteValue.get(s2);
				if(value2 > value1) {
					return 1;
				}
				else if(value2 < value1) {
					return -1;
				}
				else {
					return 0;
				}
			}
		});
		
		FeatureDictionary alphabet = new FeatureDictionary();
		int featsToKeep = (int)((1-amountToRemove)*featList.size());
		
		
		List<float[]> model = new ArrayList<float[]>(numClasses);
		for(int i = 0; i < numClasses; i++) {
			float[] oldvector = mModel.get(i);
			float[] vector = new float[featsToKeep+1];
			model.add(vector);
			for(int j = 0; j < featsToKeep; j++) {
				TwoPartKey feat = featList.get(j);
				vector[alphabet.lookupIndex(feat, true)] = oldvector[mAlphabet.lookupIndex(feat, false)];
			}
		}
		
		return new LinearClassificationModel(model, mModelLabelOrder, labelAlphabet, alphabet);
	}
	
	public ClassScoreTuple[] getDecision(Set<String> features) {
		int[] featList = new int[features.size()];
		int x = 0;
		for(String feat : features) {
			int index = mAlphabet.lookupIndex(feat, false);
			if(index >= 0) {
				featList[x] = index;
				x++;
			}
		}		
		final int numClasses = mModel.size();
		float[] decVals = new float[numClasses];
		for(int i = 0; i < numClasses; i++) {
			decVals[i] = calculateScore(featList, mModel.get(i), x);
		}
		ClassScoreTuple[] ranks = new ClassScoreTuple[numClasses];
		for(int i = 0; i < numClasses; i++) {
			ranks[i] = new ClassScoreTuple(mLabelAlphabet.lookupLabel(mModelLabelOrder[i]), 0);
		}
		for(int i = 0; i < numClasses; i++) {
			ranks[i].score = decVals[i];
		}
		Arrays.sort(ranks);
		return ranks;
	}
	
	private final float calculateScore(final int[] features, final float[] vector, final int numFeats) {
		float score = 0;
		for(int i = 0; i < numFeats; i++) {
			score += vector[features[i]];
		}
		return score;
	}
	
}