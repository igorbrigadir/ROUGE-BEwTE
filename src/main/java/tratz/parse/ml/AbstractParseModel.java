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

package tratz.parse.ml;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tratz.parse.types.Arc;
import tratz.parse.types.Token;
import tratz.types.IntArrayList;

abstract public class AbstractParseModel implements ParseModel {
	
	public final static long serialVersionUID = 1;
	protected List<String> mActions;
	
	protected Map<String, Map<String, List<String>>> mPosPosActs = new HashMap<String, Map<String,List<String>>>(); 
	
	protected Map<String, Integer> mActionToIndex;
	
	public List<String> getActions() {
		return mActions;
	}
	
	public void incrementCount() {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	public void update(String s, IntArrayList ilist, double change) {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	public void updateFeature(int actionIndex, int feat, double change) {
		throw new UnsupportedOperationException("Not implemented for this model: " + this.getClass().getCanonicalName());
	}
	
	
	public int getActionIndex(String action, boolean addIfNecessary) {
		Integer actionIndex = mActionToIndex.get(action);
		if(actionIndex == null && addIfNecessary) {
			mActionToIndex.put(action, actionIndex = mActionToIndex.size()+1);
		}
		return actionIndex;
	}
	
	private List<String> EMPTY_LIST = new ArrayList<String>();
	public List<String> getActions(Token tc, Token tr, Arc[] goldTokenToHead) {
		
		if(EMPTY_LIST == null) {
			EMPTY_LIST = new ArrayList<String>();
		}
		List<String> actions;
		if(mPosPosActs != null) { //mPosPosRightActs != null) {
			
			//System.err.println(tc.getPos() + " " + (tr == null ? "null" : "r:"+tr.getPos()));
			//System.err.println(tc.getPos());
			Map<String, List<String>> acts = mPosPosActs.get(tc.getPos());
			if(acts == null) {
				mPosPosActs.put(tc.getPos(), acts = new HashMap<String, List<String>>());
			}
			actions = tr != null ? acts.get(tr.getPos()) : EMPTY_LIST;
			if(actions == null) {
				acts.put(tr.getPos(), actions = new ArrayList<String>());
				
				actions.add("SWAPRIGHT");
				actions.add("SWAPLEFT");
				
				actions.add("depr");
				actions.add("depl");
				
			}
			if(goldTokenToHead != null) {
				Arc leftHead = goldTokenToHead[tc.getIndex()];
				Arc rightHead = tr == null ? null : goldTokenToHead[tr.getIndex()];
				if(leftHead != null && leftHead.getHead() == tr) {
					String depend = leftHead.getDependency() + "l";
					if(!actions.contains(depend)) {
					//	System.err.println("adding:left " + depend + " for " + leftHead.getHead().getPos() + " " + leftHead.getChild().getPos());
						actions.add(depend);
					}
					//actions.add(leftHead.getDependency() + "l");
				}
				else if(rightHead != null && rightHead.getHead() == tc) {
					String depend = rightHead.getDependency() + "r";
					if(!actions.contains(depend)) {
						//System.err.println("adding:right " + depend + " for " + rightHead.getHead().getPos() + " " + rightHead.getChild().getPos());
						actions.add(depend);
					}
				}
			}
		}
		else {
			System.err.println("Returning full action set");
			actions = mActions;
		}
		return actions;
	}
	
}