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

package tratz.runpipe;

import java.util.List;
import java.util.TreeSet;

/**
 *	TextDocument interface 
 *  Defines interface of basic data structure
 */
public interface TextDocument {
	public void setText(String text);
	public void setUri(String uri);
	public void addAnnotation(Annotation annot);
	public void removeAnnotation(Annotation annot);
	public TreeSet<? extends Annotation> getAnnotationSet(Class annotationClass);
	public List<? extends Annotation> getAnnotationList(Class annotationClass);
	public String getText();
	public String getUri();
}