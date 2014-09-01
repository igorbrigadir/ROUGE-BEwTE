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
import java.util.HashMap;
import java.util.Map;

/**
 * An expandable dictionary with reverse lookup capability.
 * It is typically used to contain a mapping of class identifier Strings to indices.
 */
public class ClassDictionary implements Serializable {
	public final static long serialVersionUID = 1;
	
	//private Map<String, Integer> mDict = new HashMap<String, Integer>();
	private Map<String, Integer> mDict = new HashMap<String, Integer>();
	private Map<Integer, String> mReverse = new HashMap<Integer, String>();
	private int maxIndex;
	
	public ClassDictionary() {
		
	}
	
	public ClassDictionary(ClassDictionary original) {
		mDict = original.mDict;
		mReverse = original.mReverse;
		maxIndex = original.maxIndex;
	}
	
	public void override(String key, int index) {
		mDict.put(key, index);
		mReverse.put(index, key);
		if(index > maxIndex) {
			maxIndex = index;
		}
	}
	
	public String lookupLabel(int index) {
		return mReverse.get(index);
	}
	
	public int lookupIndex(String key, boolean add) {
		int result = -1;
		Integer index = mDict.get(key);
		if(index == null) {
			if(add) {
				maxIndex++;
				mDict.put(key, maxIndex);
				mReverse.put(maxIndex, key);
				result = maxIndex;
			}
		}
		else {
			result = index;//index.intValue();
		}
		return result;
	}
	
	public int size() {
		return mDict.size();
	}
	
	/*public void printIt() {
		for(String s : mDict..keySet()) {
			System.err.println(s);
		}
	}*/
	
}