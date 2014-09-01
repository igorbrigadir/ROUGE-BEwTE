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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import tratz.runpipe.Annotation;
import tratz.runpipe.TextDocument;

/**
 * TextDocument implementation class
 */
public class TextDocumentImpl implements TextDocument, Serializable {
	
	public final static long serialVersionUID = 1l;
	
	protected String mText;
	protected String mUri;
	protected Map<Class, TreeSet<Annotation>> mAnnotationIndex = new HashMap<Class, TreeSet<Annotation>>();
	
	public TextDocumentImpl(String text) {
		mText = text;
	}
	
	public TextDocumentImpl() {
		
	}
	
	@Override
	public void setUri(String uri) {
		mUri = uri;
	}
	
	@Override
	public void setText(String text) {
		mText = text;
	}
	
	@Override
	public String getUri() {
		return mUri;
	}

	@Override
	public void addAnnotation(Annotation annot) {
		Class type = annot.getClass();
		TreeSet<Annotation> annotations = mAnnotationIndex.get(type);
		if(annotations == null) {
			mAnnotationIndex.put(type, annotations = new TreeSet<Annotation>());
		}
		annotations.add(annot);
	}
	
	@Override
	public void removeAnnotation(Annotation annot) {
		Class type = annot.getClass();
		TreeSet<Annotation> annots = mAnnotationIndex.get(type);
		if(annots != null) {
			annots.remove(annot);
		}
	}
	
	@Override
	public TreeSet<Annotation> getAnnotationSet(Class annotationType) {
		TreeSet<Annotation> annotSet = mAnnotationIndex.get(annotationType);
		if(annotSet != null) {
			annotSet = new TreeSet<Annotation>(annotSet);
		}
		return annotSet;
	}
	
	@Override
	public List<Annotation> getAnnotationList(Class annotationType) {
		TreeSet<Annotation> annotSet = mAnnotationIndex.get(annotationType);
		List<Annotation> annots = null;
		if(annotSet != null) {
			annots = new ArrayList<Annotation>(annotSet);
		}
		return annots;
	}
	
	public String getText() {
		return mText;
	}
}