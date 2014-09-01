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

package tratz.jwikt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WEntry {
	
	public static class Definition {
		private String mGloss;
		private List<String> mExamples = new ArrayList<String>();
		public Definition(String gloss) {
			mGloss = gloss;
		}
		public String getGloss() {
			return mGloss;
		}
		public List<String> getExamples() {
			return mExamples;
		}
	}

	public Set<String> mJunk = new HashSet<String>();
	private final String mTerm;
	private final WPOS mPos;
	private boolean mIsCountable;
	private boolean mIsComparable;
	private final List<Definition> mDefinitions = new ArrayList<Definition>();
	
	public WEntry(String term, WPOS pos) {
		mTerm = term;
		mPos = pos;
		
	}
	
	

	public WPOS getPos() {
		return mPos;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	void setComparable(boolean comparable) {
		mIsComparable = comparable;
	}
	
	void setCountable(boolean countable) {
		mIsCountable = countable;
	}
	
	public boolean isCountable() {
		return mPos == WPOS.NOUN && mIsCountable;
	}
	
	public boolean isComparable() {
		return mPos == WPOS.ADJECTIVE && mIsComparable;
	}
	
	void addDefinition(Definition def) {
		mDefinitions.add(def);
	}
	
	public List<Definition> getDefinitions() {
		return mDefinitions;
	}
	
}