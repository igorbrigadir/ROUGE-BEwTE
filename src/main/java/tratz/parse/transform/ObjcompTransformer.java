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
import tratz.parse.util.ParseConstants;

public class ObjcompTransformer implements ParseTransform {
	
	@Override
	public void performTransformation(Parse parse) {
		Arc[] tokenToHead = parse.getHeadArcs();
		List[] tokenToArcs = parse.getDependentArcLists();
		for(int i = tokenToHead.length-1; i>=0; i--) {
			Arc arc = tokenToHead[i];
			if(arc != null && arc.getDependency().equals(ParseConstants.OBJECT_COMPLEMENT_DEP)) {
				List<Arc> headsChildren = tokenToArcs[arc.getHead().getIndex()];
				Arc dobjArc = null;
				for(Arc child : headsChildren) {
					if(child.getDependency().equals(ParseConstants.DIRECT_OBJECT_DEP)) {
						dobjArc = child;
						break;
					}
				}
				if(dobjArc != null) {
					arc.setDependency(ParseConstants.CLAUSAL_COMPLEMENT_WO_SUBJ_DEP);
					headsChildren.remove(dobjArc);
					dobjArc.setDependency(ParseConstants.NOMINAL_SUBJECT_DEP);
					dobjArc.setHead(arc.getChild());
					List<Arc> objcompChildren = tokenToArcs[arc.getChild().getIndex()];
					if(objcompChildren == null) {
						tokenToArcs[arc.getChild().getIndex()] = objcompChildren = new ArrayList<Arc>();
					}
					objcompChildren.add(dobjArc);
				}
				else {
					arc.setDependency(ParseConstants.UNSPECIFIED_DEP); // sometimes dep, sometimes xcomp
				}
			}
		}
	}
}