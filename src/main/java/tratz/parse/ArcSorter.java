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

package tratz.parse;

import java.util.Comparator;

import tratz.parse.types.Arc;

/**
 * Sorts arcs by the index of their children in ascending order
 *
 */
public class ArcSorter implements Comparator<Arc> {	
	public int compare(Arc arc1, Arc arc2) {
			int index1 = arc1.getChild().getIndex();
			int index2 = arc2.getChild().getIndex();
			return index1-index2;
	}
}