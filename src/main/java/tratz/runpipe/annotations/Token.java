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

package tratz.runpipe.annotations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.AnnotationImpl;

/**
 * Token annotation
 */
public class Token extends AnnotationImpl {
	
	public final static long serialVersionUID = 1;
	
	public static class Arc implements Serializable {
		private static final long serialVersionUID = 1L;
		private Token mHead;
		private Token mChild;
		private String mDependency;
		public Arc(Token head, Token child, String dependency) {
			mHead = head;
			mChild = child;
			mDependency = dependency;
		}
		public Token getHead() {
			return mHead;
		}
		public Token getChild() {
			return mChild;
		}
		public String getDependency() {
			return mDependency;
		}
	}
	
	protected Arc mParentArc;
	protected List<Arc> mDependentArcs;
	protected String mPartOfSpeech;
	protected String mLemma;
	
	public Token(TextDocument doc) {
		super(doc);
	}
	
	public Token(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
	
	public void addDependent(Arc dependent) {
		if(mDependentArcs == null) {
			mDependentArcs = new ArrayList<Arc>(5);
		}
		mDependentArcs.add(dependent);
	}
	
	public List<Arc> getDependentArcs() {
		return mDependentArcs;
	}
	
	public String getLemma() {
		return mLemma;
	}
	
	public Arc getParentArc() {
		return mParentArc;
	}
	
	public String getPos() {
		return mPartOfSpeech;
	}
	
	
	
	public void setLemma(String lemma) {
		mLemma = lemma;
	}
	
	public void setParentArc(Arc parentArc) {
		mParentArc = parentArc;
	}
	
	public void setPos(String partOfSpeech) {
		mPartOfSpeech = partOfSpeech;
	}
	
}