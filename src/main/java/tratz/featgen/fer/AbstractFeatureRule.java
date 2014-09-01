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

package tratz.featgen.fer;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tratz.featgen.InitException;

abstract public class AbstractFeatureRule implements FeatureExtractionRule, Serializable {
	
	public final static long serialVersionUID = 1;
	
	private String mPrefix;
	
	public void init(Map<String, String> params) throws InitException {
		
	}
	
	public Set<String> getProductions(String base, String type) throws FERProductionException {
		return getProductions(base, type, new HashSet<String>());
	}
	
	protected void addSet(Set<String> features, Collection<String> newFeatures, String prefix, Map<String,String> canonFeats) {
		for(String s : newFeatures) {
			String newFeat = prefix+s;
			if(canonFeats.containsKey(newFeat)) {
				features.add(canonFeats.get(newFeat));
			}
			else {
				canonFeats.put(newFeat, newFeat);
				features.add(newFeat);
			}
		}
	}
	
	protected void addSet(Set<String> features, Collection<String> newFeatures, String prefix) {
		for(String s : newFeatures) {
			features.add(prefix+s);
		}
	}

	public void setPrefix(String prefix) {
		mPrefix = prefix;
	}
	
	public String getPrefix() {
		return mPrefix;
	}
	
}