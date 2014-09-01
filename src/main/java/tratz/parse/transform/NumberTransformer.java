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

package tratz.parse.transform;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.util.ParseConstants;

public class NumberTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
		for(Arc arc : parse.getHeadArcs()) {
			if(arc != null && arc.getDependency().equals(ParseConstants.NUMERAL_MOD_DEP) && "$".equals(arc.getHead().getText())) {
				arc.setDependency(ParseConstants.MULTI_WORD_NUMBER_DEP);
			}
		}
		
	}
	
}