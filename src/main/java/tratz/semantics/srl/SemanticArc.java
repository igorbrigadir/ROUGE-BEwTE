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

package tratz.semantics.srl;

public class SemanticArc {
	
	private String mType;	
	private String mFullForm;
	private String mPredicate;
	
	public SemanticArc(String type, String fullForm, String predicate) {
		mType = type;
		mFullForm = fullForm;
		mPredicate = predicate;
	}
	
	public String getPredicate() {
		return mPredicate;
	}
	
	public String getFullForm() {
		return mFullForm;
	}
	
	public String getType() {
		return mType;
	}
	
}