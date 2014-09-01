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

/**
 * Class for holding an arc (a dependency link)
 *
 */
public class Arc implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * The child (dependent) of the arc
	 */
	private Token mChild;
	
	/**
	 * The head (governor) that the arc descends from
	 */
	private Token mHead;
	
	/**
	 * The label of the arc. Typically the syntactic dependency type
	 */
	private String mDependency;
	
	/* place holder for indicating at which step the arc was creating in the parsing of the sentence... 
	 * may not the best place for this */
	private int mCreationNum;
	
	/* place holder for additional semantic annotation... may not be the best place for this*/
	private String mSemanticAnnotation;
	
	public Arc(Token child, Token head, String type) {
		this(child,head,type,-1);
	}
	
	public Arc(Token child, Token head, String type, int creationNumber) {
		mChild = child;
		mHead = head;
		mDependency = type;
		mCreationNum = creationNumber;
	}
	
	public String getDependency() {
		return mDependency;
	}
	
	public void setDependency(String dep) {
		mDependency = dep;
	}
	
	public void setSemanticAnnotation(String semanticAnnotation) {
		mSemanticAnnotation = semanticAnnotation;
	}
	
	public String getSemanticAnnotation() {
		return mSemanticAnnotation;
	}
	
	public Token getChild() {
		return mChild;
	}
	
	public void setChild(Token newChild) {
		mChild = newChild;
	}
	
	public Token getHead() {
		return mHead;
	}
	
	public void setHead(Token newHead) {
		mHead = newHead;
	}
	
	public void setCreationNum(int creationNum) {
		this.mCreationNum = creationNum;
	}
	
	public int getCreationNum() {
		return mCreationNum;
	}
	
	// TODO: Fix this so it truly checks equality
	public boolean equals(Arc arc) {
		return mChild.getIndex() == arc.mChild.getIndex() && mHead.getIndex() == arc.mHead.getIndex() && mDependency.equals(arc.mDependency);
	}
}