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

package tratz.jwni;

public class IndexEntry {
	
	private String mLemma;
	private int[] mIndices;
	private WordNet mWn;
	private POS mPos;
	
	public IndexEntry(WordNet wn, POS pos, String lemma, int[] indices) {
		mWn = wn;
		mLemma = lemma;
		mIndices = indices;
		mPos = pos;
	}
	
	public String getLemma() {
		return mLemma;
	}
	
	public Sense[] getSenses() {
		Sense[] senses = new Sense[mIndices.length];
		for(int i = 0; i < mIndices.length; i++) {
			senses[i] = mWn.getSenseAtOffset(mPos, mIndices[i]);
		}
		return senses;
	}
	
}