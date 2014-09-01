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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import tratz.jwikt.WEntry;
import tratz.jwikt.WPOS;
import tratz.jwikt.Wiktionary;
import tratz.jwikt.WEntry.Definition;


/**
 * Feature extraction rule for returning definition words from Wiktionary definitions
 *
 */
public class WiktionaryGlossTermsFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;
	
	public final static String PARAM_DEFAULT_POS = "DefaultPos";
	public final static String PARAM_WIKTIONARY_PATH = "WiktionaryFile";
	
	protected String mDefaultPos;
	
	@Override
	public void init(Map<String, String> params) {
		mDefaultPos = params.get(PARAM_DEFAULT_POS);
		String wiktionaryPath = params.get(PARAM_WIKTIONARY_PATH);
		
		Wiktionary wiktionary = Wiktionary.getGlobal();
		if(wiktionary == null) {
			try {
				Wiktionary.setGlobal(wiktionary = Wiktionary.readWiktionary(wiktionaryPath));
			}
			catch(IOException ioe) {
					ioe.printStackTrace();
			}
		}
	}

	public Set<String> getProductions(String input, String type, Set<String> productions) {
		if(type == null) {
			type = mDefaultPos;
		}
		Wiktionary wiktionary = Wiktionary.getGlobal();
		WPOS pos = WPOS.NOUN;
		if(type.startsWith("VB")) {
			pos = WPOS.VERB;
		}
		else if(type.startsWith("JJ")) {
			pos = WPOS.ADJECTIVE;
		}
		if(pos != null) {
			WEntry entry = wiktionary.getEntry(pos, input);
			if(entry != null) {
				for(Definition def : entry.getDefinitions()) {
					String[] glossTerms = def.getGloss().split("\\s+");
					for(String term : glossTerms) {
						productions.add(term);
					}
				}
			}
		}
		return productions;
	}
	
}