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

package tratz.parse.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tratz.parse.ArcSorter;

/**
 * The data structure for holding a sentence and its parse.
 */
public class Parse implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Arc> mArcs;
	private Token mRoot;
	private List[] mTokenToChildren;
	private Arc[] mTokenToHead;
	
	private Sentence mSentence;
	
	public Parse(Sentence sentence, 
				 Token root, 
				 List<Arc> arcs,
				 List[] tokenToChildren,
				 Arc[] tokenToHead) {
		mSentence = sentence;
		mRoot = root;
		mArcs = arcs;
		mTokenToChildren = tokenToChildren;
		mTokenToHead = tokenToHead;
	}
	
	public Parse(Sentence sentence, 
			 Token root, 
			 List<Arc> arcs) {
	mSentence = sentence;
	mRoot = root;
	mArcs = arcs;
	mTokenToChildren = buildTokenToChildren();
	mTokenToHead = buildTokenToHeadArcs();
}
	
	private List[] buildTokenToChildren() {
		List[] tokenToChildren = new List[mSentence.getTokens().size()+1];
		if(mArcs != null) {
			for(Arc a : mArcs) {
				List<Arc> arcs = tokenToChildren[a.getHead().getIndex()];
				if(arcs == null) {
					tokenToChildren[a.getHead().getIndex()] = arcs = new ArrayList<Arc>();
				}
				arcs.add(a);
			}
			ArcSorter sorter = new ArcSorter();
			for(List<Arc> arcList : tokenToChildren) {
				if(arcList != null) { // null for root
					Collections.sort(arcList, sorter);
				}
			}
		}
		return tokenToChildren;
	}
	
	private Arc[] buildTokenToHeadArcs() {
		int numTokens = mSentence.getTokens().size();
		Arc[] tokenToHead = new Arc[numTokens+1];
		if(mArcs != null) {
			for(Arc arc : mArcs) {
				tokenToHead[arc.getChild().getIndex()] = arc;
			}
		}
		return tokenToHead;
	}
	
	public List<Arc> getArcs() {
		return mArcs;
	}
	
	public Token getRoot() {
		return mRoot;
	}
	
	public Sentence getSentence() {
		return mSentence;
	}
	
	public Arc[] getHeadArcs() {
		return mTokenToHead;
	}
	
	public List[] getDependentArcLists() {
		return mTokenToChildren;
	}
}