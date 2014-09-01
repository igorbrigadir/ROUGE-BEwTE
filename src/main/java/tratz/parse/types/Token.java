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
 * A token class. Represents a single token (or word) in a sentence.
 */
public class Token implements Serializable, Comparable<Token> {
	public final static long serialVersionUID = 1;
	
	/**
	 * The text of the token
	 */
	private String mText;
	
	/**
	 * The index of the token within a sentence
	 */
	private int mIndex;
	
	/**
	 * The lemma of the token
	 */
	private String mLemma;
	
	/**
	 * The 'sense' of the token
	 */
	private String mLexSense;
	
	
	/**
	 * part-of-speech
	 */
	private String mPos;
	
	/**
	 * Placeholder for a coarse part-of-speech type 
	 */
	private String mCoarsePos;
	
	public Token(String text, int index) {
		mText = text;
		mIndex = index;
	}
	
	public Token(String text, String tag, int index) {
		this(text, index);
		mPos = tag;
	}
	
	public Token(Token original) {
		this.mText = original.mText;
		this.mCoarsePos = original.mCoarsePos;
		this.mLemma = original.mLemma;
		this.mLexSense = original.mLexSense;
		this.mPos = original.mPos;
		this.mIndex = original.mIndex;
	}
	
	public void setLexSense(String lexSense) {
		mLexSense = lexSense;
	}
	
	public String getLexSense() {
		return mLexSense;
	}
	
	public void setLemma(String lemma) {
		mLemma = lemma;
	}
	
	public String getLemma() {
		return mLemma;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public String getText() {
		return mText;
	}
	
	public int getIndex() {
		return mIndex;
	}
	
	public void setPos(String pos) {
		mPos = pos;
	}
	
	public String getPos() {
		return mPos;
	}
	
	public void setCoarsePos(String coarsePos) {
		mCoarsePos = coarsePos;
	}
	
	public String getCoarsePos() {
		return mCoarsePos;
	}
	
	public int compareTo(Token other) {
		return mIndex-other.mIndex;
	}
	
}