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

package tratz.runpipe.impl;

import java.io.Serializable;

import tratz.runpipe.Annotation;
import tratz.runpipe.TextDocument;

/**
 * Implementation of annotation class
 */
abstract public class AnnotationImpl implements Annotation, Serializable {
	public final static long serialVersionUID = 1L;
	
	protected int mStart = -1;
	protected int mEnd = -1;
	protected TextDocument mDocument;
	
	public AnnotationImpl(TextDocument document) {
		mDocument = document;
	}
	
	public AnnotationImpl(TextDocument document, int start, int end) {
		mDocument = document;
		mStart = start;
		mEnd = end;
	}
	
	@Override
	public TextDocument getDocument() {
		return mDocument;
	}
	
	@Override
	public void setStart(int start) {
		mStart = start;
	}
	
	@Override
	public void setEnd(int end) {
		mEnd = end;
	}
	
	@Override
	public int getStart() {
		return mStart;
	}
	
	@Override
	public int getEnd() {
		return mEnd;
	}
	
	@Override
	public String getAnnotText() {
		return mDocument.getText().substring(mStart, mEnd);
	}
	
	@Override
	public int compareTo(Annotation o) {
		Annotation annot = (Annotation)o;
		int startDiff = mStart - annot.getStart();
		if(startDiff != 0) {
			return startDiff;
		}
		int endDiff = annot.getEnd() - mEnd;
		if(endDiff != 0) {
			return endDiff;
		}
		return hashCode()-annot.hashCode();
	}
}