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

public class CopTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
		Arc[] tokenToHead = parse.getHeadArcs();
		List[] tokenToArcs = parse.getDependentArcLists();
		boolean[] modified = new boolean[tokenToHead.length];
		for(int i = tokenToHead.length-1; i >= 0; i--) {
			Arc arc = tokenToHead[i];
			if(arc != null && arc.getDependency().equals(ParseConstants.COPULAR_COMPLEMENT_DEP) && !modified[i]) {
				Token arcHead = arc.getHead();
				Token arcChild = arc.getChild();
				List<Arc> siblings = tokenToArcs[arcHead.getIndex()];
				boolean hasExpl = false;
				for(Arc sibling : siblings) {
					if(sibling.getDependency().equals(ParseConstants.EXPLETIVE_THERE_DEP)) {
						hasExpl = true;
						break;
					}
				}
				if(hasExpl) {
					// cop becomes a rightward-linking nsubj
					arc.setDependency(ParseConstants.NOMINAL_SUBJECT_DEP);
				}
				else {
				
					siblings.remove(arc);
					List<Arc> kids = tokenToArcs[arcChild.getIndex()];
					if(kids == null) {
						tokenToArcs[arcChild.getIndex()] = kids = new ArrayList<Arc>();
					}
					for(Arc sibling : siblings) {
						sibling.setHead(arcChild);
					}
					for(Arc kid : kids) {
						if(kid.getDependency().equals(ParseConstants.INFINITIVE_MODIFIER_DEP)) {
							kid.setDependency(ParseConstants.CLAUSAL_COMPLEMENT_WO_SUBJ_DEP); // not sure why Stanford does it this way
						}
					}
					kids.addAll(siblings);
					siblings.clear();
				
					Arc grandparentArc = tokenToHead[arcHead.getIndex()];
					if(grandparentArc != null && grandparentArc.getHead() != null) {
						arc.setDependency(grandparentArc.getDependency());
						Token grandparent = grandparentArc.getHead();
						arc.setHead(grandparent);
						List<Arc> children = tokenToArcs[grandparent.getIndex()];
						if(children == null) {
							tokenToArcs[grandparentArc.getHead().getIndex()] = children = new ArrayList<Arc>();
						}
						children.add(arc);
						children.remove(grandparentArc);
					
						kids.add(grandparentArc);
					}
					else {
						arc.setDependency(ParseConstants.ROOT_DEP);
						arc.setHead(parse.getRoot());
						List<Arc> rootChildren = tokenToArcs[0];
						if(rootChildren == null) {
							tokenToArcs[0] = rootChildren = new ArrayList<Arc>();
						}
						rootChildren.add(arc);
						
						if(grandparentArc == null) {
							grandparentArc = new Arc(arcHead, arcChild, ParseConstants.COPULAR_COMPLEMENT_DEP);
							tokenToHead[arcHead.getIndex()] = grandparentArc;
						}
						rootChildren.remove(grandparentArc);
						kids.add(grandparentArc);		
					}
					grandparentArc.setHead(arcChild);
					grandparentArc.setDependency(ParseConstants.COPULAR_COMPLEMENT_DEP);
					modified[arcHead.getIndex()] = true;
					}
			}
		}
	}
	
}