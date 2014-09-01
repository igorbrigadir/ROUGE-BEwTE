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

import java.util.Set;

/**
 * Feature Extraction Rule that returns the 2 and 3 letters of a word.
 *
 */
public class FirstLettersFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;

	public Set<String> getProductions(String term, String type, Set<String> productions) {
		int termLength = term.length();
		if(termLength > 2) {
			productions.add(term.substring(0, 2));
		}
		if(termLength > 3) {
			productions.add(term.substring(0, 3));
		}
		return productions;
	}
	
}