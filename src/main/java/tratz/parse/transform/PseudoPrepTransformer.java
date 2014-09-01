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

import java.util.List;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.util.ParseConstants;

public class PseudoPrepTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
			
		for(Arc arc : parse.getHeadArcs()) {
			if(arc != null) {
				String dep = arc.getDependency();
				if(ParseConstants.ADVERBIAL_CLAUSE_DEP.equals(dep)) {
					String childPos = arc.getChild().getPos();
					if(childPos.equals("VBG") || childPos.equals("VBN")) {
						String childText = arc.getChild().getText().toLowerCase();
						if(childText.equals("according")||
						   childText.equals("depending")||
						   childText.equals("compared")) {
							arc.setDependency(ParseConstants.PREP_MOD_DEP);
							List<Arc> children = parse.getDependentArcLists()[arc.getChild().getIndex()];
							if(children != null) {
								for(Arc child : children) {
									if(child.getDependency().equals(ParseConstants.PREP_MOD_DEP)) {
										child.setDependency(ParseConstants.UNSPECIFIED_DEP);
									}
								}
							}
						}
						else if(childText.equals("given")||
								childText.equals("including")||
								childText.equals("assuming")) {
							arc.setDependency("prep");
							List<Arc> children = parse.getDependentArcLists()[arc.getChild().getIndex()];
							if(children != null) {
								for(Arc child : children) {
									if(child.getDependency().equals(ParseConstants.DIRECT_OBJECT_DEP)) {
										child.setDependency(ParseConstants.PREP_OBJECT_DEP);
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	
}