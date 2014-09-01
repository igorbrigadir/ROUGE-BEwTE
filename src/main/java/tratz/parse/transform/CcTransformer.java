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

import java.util.ArrayList;
import java.util.List;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

public class CcTransformer implements ParseTransform {
	
	public void performTransformation(Parse parse) {
		Arc[] tokenToHead = parse.getHeadArcs();
		List[] tokenToArcs = parse.getDependentArcLists();
		boolean[] modified = new boolean[tokenToHead.length];
		// Proceed from right to left
		for(int i = tokenToHead.length-1; i >= 0; i--) {
			if(!modified[i] && tokenToHead[i] != null) {
				String dep = tokenToHead[i].getDependency();
				boolean foundCC = false;
				if(dep.equals(ParseConstants.CONJUNCTION_DEP) || dep.equals(ParseConstants.COORDINATING_CONJUNCTION_DEP)) {
					if(dep.equals(ParseConstants.COORDINATING_CONJUNCTION_DEP)) {
						foundCC = true;
					}
					
					List<Arc> arcsToRelocate = new ArrayList<Arc>();
					arcsToRelocate.add(tokenToHead[i]);
					System.err.println(tokenToHead[i].getChild().getText() + "--" + tokenToHead[i].getDependency()+ "-->" + tokenToHead[i].getHead().getText());
					// Need to locate topmost conjunct
					Token topConjunct = tokenToHead[i].getHead();
					while(true) {
						Arc nextArc = tokenToHead[topConjunct.getIndex()];
						if(nextArc != null) {
							String depNext = nextArc.getDependency();
							if(depNext.equals(ParseConstants.CONJUNCTION_DEP) || depNext.equals(ParseConstants.COORDINATING_CONJUNCTION_DEP)) {
								if(foundCC && nextArc.getHead().getPos().equals("CC")) {
									break; // going to hit a second 'cc' arc, need to combine what we have so far
								}
								if(depNext.equals(ParseConstants.COORDINATING_CONJUNCTION_DEP)) {
									foundCC = true;
								}
								topConjunct = nextArc.getHead();
								arcsToRelocate.add(nextArc);
							}
							else {
								break;
							}
						}
						else {
							break;
						}
					}
					// If there is only 1, there is no reason to do anything
					if(arcsToRelocate.size() > 1) {
						List<Arc> newHeadsChildren = tokenToArcs[topConjunct.getIndex()];
						for(Arc arc : arcsToRelocate) {
							modified[arc.getChild().getIndex()] = true;
							Token oldArcHead = arc.getHead();
							List<Arc> oldHeadsChildren = tokenToArcs[oldArcHead.getIndex()];
							oldHeadsChildren.remove(arc);
							
							arc.setHead(topConjunct);
							newHeadsChildren.add(arc);
						}
					}
					
				}
			}
		}
		
	}
	
}