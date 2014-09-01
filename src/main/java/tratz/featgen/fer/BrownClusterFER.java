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

package tratz.featgen.fer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tratz.featgen.InitException;

/**
 * A Feature Extraction Rule for returning Brown cluster information.
 * (needs a bit of work)
 *
 */
public class BrownClusterFER extends AbstractFeatureRule {
	
	public final static long serialVersionUID = 1;
	
	public final static String PARAM_DATA_FILE = "dataFile";
	public final static String PARAM_MIN_WORD_OCCURRENCE = "minOccurrence";
	public final static String PARAM_MAX_DEPTH = "maxDepth";
	
	private Map<String, String> mFeatMap = new HashMap<String, String>();
	
	public void init(Map<String, String> params) throws InitException {
		String dataFile = params.get(PARAM_DATA_FILE);
		String maxDepthS = params.get(PARAM_MAX_DEPTH);
		String minOccurrenceS = params.get(PARAM_MIN_WORD_OCCURRENCE);
		
		int minOccurrence = minOccurrenceS == null ? 1 : Integer.parseInt(minOccurrenceS);
		int maxDepth = maxDepthS == null ? Integer.MAX_VALUE : Integer.parseInt(maxDepthS);
		
		// split off as subroutine
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\\t+");
				String path = split[0];
				String token = split[1];
				int occurrences = Integer.parseInt(split[2]);
				if(occurrences >= minOccurrence) {
					mFeatMap.put(token, path.substring(0, Math.min(path.length(), maxDepth)));
				}
			}
			reader.close();
		}
		catch(IOException ioe) {
			throw new InitException(ioe);
		}
	}
	
	public Set<String> getProductions(String text, String pos, Set<String> productions) {
		String path = mFeatMap.get(text);
		if(path != null) {
			productions.add(path);
		}
		return productions;
	}
	
}