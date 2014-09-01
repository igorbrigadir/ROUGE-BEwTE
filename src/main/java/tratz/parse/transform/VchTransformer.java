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

/**
 * Script for converting 'vch' dependency links into 'aux' and 'auxpass' links.
 * It also converts 'nsubj' and 'csubj' links into 'nsubjpass' and 'csubjpass' if
 * they are in a passive construction.
 * 
 * Need to check out the case of the word 'get' as an auxillary.
 */
public class VchTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
		Arc[] tokenToHead = parse.getHeadArcs();
		List[] tokenToArcs = parse.getDependentArcLists();
		boolean anyChangesMade = false;
		boolean changeMade = true;
		while(changeMade) {
			changeMade = false;
			for(int i = tokenToHead.length-1; i>=0; i--) {
				Arc arc = tokenToHead[i];
				if(arc != null && arc.getDependency().equals(ParseConstants.VERBAL_CHAIN_DEP)) {
					boolean hasVchild = false;
					List<Arc> arcs = tokenToArcs[arc.getChild().getIndex()];
					if(arcs != null) {
						for(Arc a : arcs) {
							if(a.getDependency().equals(ParseConstants.VERBAL_CHAIN_DEP)) {
								hasVchild = true;
								break;
							}
						}
					}
					if(!hasVchild) {
						handleArc(parse.getRoot(), tokenToArcs, tokenToHead, arc);
						changeMade = true;
						anyChangesMade = true;
					}
				}
			}
		}
		for(int i = 0; i < tokenToHead.length; i++) {
			Arc arc = tokenToHead[i];
			if(arc != null && (arc.getDependency().equals(ParseConstants.NOMINAL_SUBJECT_DEP) || arc.getDependency().equals(ParseConstants.CLAUSAL_SUBJECT_DEP))) {
				Token head = arc.getHead();
				boolean hasAuxpass = false;
				List<Arc> children = tokenToArcs[head.getIndex()];
				if(children != null) {
					for(Arc child : children) {
						if(child.getDependency().equals(ParseConstants.PASSIVE_AUXILLARY_DEP)) {
							hasAuxpass = true;
							break;
						}
					}
				}
				if(hasAuxpass) {
					arc.setDependency(arc.getDependency()+"pass");
				}
			}
		}
		
	}
	
	private void handleArc(Token root, 
								  List[] tokenToArcs,
			 					  Arc[] tokenToHead,
			 					  Arc arc) {
		Token originalChild = arc.getChild();
		while(((arc = tokenToHead[originalChild.getIndex()]) != null) 
				&& arc.getDependency().equals(ParseConstants.VERBAL_CHAIN_DEP)) {
			
			Token child = arc.getChild();
			Token head = arc.getHead();
			
			String newDep = ParseConstants.UNSPECIFIED_DEP;
			String headText = head.getText().toLowerCase();
			if(headText.matches("ai|been|be|am|'m|are|were|'re|was|is")) {
				if(child.getPos().matches("VB(D|N)|JJ") || (child.getPos().startsWith("NN")&&child.getText().toLowerCase().matches(".*(ed|n)"))) { 
					newDep = ParseConstants.PASSIVE_AUXILLARY_DEP;
					child.setPos("VBN");
				}
				else if(child.getPos().matches("VBG")) {
					newDep = ParseConstants.AUXILLARY_DEP;
				}
				else {
					newDep = ParseConstants.AUXILLARY_DEP;
				}
			}
			else if(headText.matches("'s")) {
				// may be a little buggy in the case of adjectives
				if( hasChildWithText(child, tokenToArcs, "being")) {
					newDep = ParseConstants.PASSIVE_AUXILLARY_DEP;
				}
				else {
					newDep = ParseConstants.AUXILLARY_DEP;
				}
				
				/*if(arc.getChild().getText().toLowerCase().equals("been")) {
					newDep = "aux";
				}
				else if(hasBeenChild(head, tokenToArcs) || hasBeenChild(child, tokenToArcs)) {
					newDep = "aux";
				}
				else if(child.getPos().matches("VB(D|N)|JJ") || (child.getPos().matches("NN.*") && child.getText().toLowerCase().matches(".*(ed|n)"))) {
					newDep = "auxpass";
				}
				else {
					newDep = "aux";
				}*/
			}
			else if(headText.matches("done|don't|ca|'d|can|coulda?|did|does|do|may|mighta?|must|ought|should|woulda?|will|shall|shalt|'ll|wo|had|has|have|'ve|having|being")) {
				newDep = ParseConstants.AUXILLARY_DEP;
			}			
			//getting|gotten|get|gets
			
			demoteAUX(root, head, child, tokenToArcs, tokenToHead, newDep);
		}
	}
	
	private static boolean hasChildWithText(Token child, List[] tokenToArcs, String text) {
		boolean hasChildMatchingText = false;
		List<Arc> arcs = tokenToArcs[child.getIndex()];
		if(arcs != null) {
			for(Arc arc : arcs) {
				if(arc.getChild().getText().toLowerCase().matches(text)) {
					hasChildMatchingText = true;
					break;
				}
			}
		}
		return hasChildMatchingText;
	}
	
	private static void demoteAUX(Token root,
								  Token demotee, 
								  Token promotee, 
								  List[] tokenToArcs, 
								  Arc[] tokenToHead, 
								  String newDependency) {
		Arc demoteeHeadArc = tokenToHead[demotee.getIndex()];
		Arc promoteeHeadArc = tokenToHead[promotee.getIndex()];
		
		Token demoteeHead = demoteeHeadArc == null ? null : demoteeHeadArc.getHead();
		
		if(demoteeHead != null) {
			List<Arc> demoteeHeadArcs = tokenToArcs[demoteeHead.getIndex()];
			demoteeHeadArcs.remove(demoteeHeadArc);
			demoteeHeadArcs.add(promoteeHeadArc);
		}
		promoteeHeadArc.setDependency(demoteeHead == null ? "ROOT" : demoteeHeadArc.getDependency());
		
		List<Arc> demoteesChildren = tokenToArcs[demotee.getIndex()];
		demoteesChildren.remove(promoteeHeadArc);
		
		//promoteeHeadArc.setHead(demoteeHead==null? root : demoteeHead);
		promoteeHeadArc.setHead(demoteeHead==null? null : demoteeHead);
		
		List<Arc> reattachedArcs = new ArrayList<Arc>();
		
		if(demoteeHeadArc != null) {
			demoteeHeadArc.setHead(promotee);
			demoteeHeadArc.setDependency(newDependency);
		
			reattachedArcs.add(demoteeHeadArc);
		}
		else {
			demoteeHeadArc = new Arc(demotee, promotee, newDependency);
			tokenToHead[demotee.getIndex()] = demoteeHeadArc;
			reattachedArcs.add(demoteeHeadArc);
		}
		// now need to reattach any determiners
		// determiners? really?
		for(Arc demoteeChildArc : demoteesChildren) {
			reattachedArcs.add(demoteeChildArc);
			demoteeChildArc.setHead(promotee);
		}
		
		List<Arc> promoteesChildren = tokenToArcs[promotee.getIndex()];
		if(promoteesChildren == null) {
			tokenToArcs[promotee.getIndex()] = promoteesChildren = new ArrayList<Arc>();
		}
		promoteesChildren.addAll(reattachedArcs);
	}
	

	
}