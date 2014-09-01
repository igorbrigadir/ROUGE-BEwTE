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

package tratz.jwni;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorphoProcessor {

	private WordNet mWn;

	private static class SuffixRule {
		private String originalSuffix;

		private String newSuffix;

		public SuffixRule(String originalSuffix, String newSuffix) {
			this.originalSuffix = originalSuffix;
			this.newSuffix = newSuffix;
		}
	}
	
	private final static List<SuffixRule> NOUN_RULES = new ArrayList<SuffixRule>();
	private final static List<SuffixRule> VERB_RULES = new ArrayList<SuffixRule>();
	private final static List<SuffixRule> ADJ_RULES = new ArrayList<SuffixRule>();

	private final static Map<POS, List<SuffixRule>> POS_TO_RULES = new HashMap<POS, List<SuffixRule>>();
	static {
		NOUN_RULES.add(new SuffixRule("s$", ""));
		NOUN_RULES.add(new SuffixRule("ses$", "s"));
		NOUN_RULES.add(new SuffixRule("xes$", "x"));
		NOUN_RULES.add(new SuffixRule("zes$", "z"));
		NOUN_RULES.add(new SuffixRule("ches$", "ch"));
		NOUN_RULES.add(new SuffixRule("shes$", "sh"));
		NOUN_RULES.add(new SuffixRule("men$", "man"));
		NOUN_RULES.add(new SuffixRule("ies$", "y"));
		VERB_RULES.add(new SuffixRule("s$", ""));
		VERB_RULES.add(new SuffixRule("ies$", "y"));
		VERB_RULES.add(new SuffixRule("es$", "e"));
		VERB_RULES.add(new SuffixRule("es$", ""));
		VERB_RULES.add(new SuffixRule("ed$", "e"));
		VERB_RULES.add(new SuffixRule("ed$", ""));
		VERB_RULES.add(new SuffixRule("ing$", "e"));
		VERB_RULES.add(new SuffixRule("ing$", ""));
		ADJ_RULES.add(new SuffixRule("er$", ""));
		ADJ_RULES.add(new SuffixRule("est$", ""));
		ADJ_RULES.add(new SuffixRule("er$", "e"));
		ADJ_RULES.add(new SuffixRule("est$", "e"));
		POS_TO_RULES.put(POS.NOUN, NOUN_RULES);
		POS_TO_RULES.put(POS.VERB, VERB_RULES);
		POS_TO_RULES.put(POS.ADJECTIVE, ADJ_RULES);
	}

	private Map<String, String> mNounExceptions = new HashMap<String, String>();

	private Map<String, String> mVerbExceptions = new HashMap<String, String>();

	private Map<String, String> mAdjExceptions = new HashMap<String, String>();

	private Map<POS, Map<String, String>> mPosToExceptionsMap = new HashMap<POS, Map<String, String>>();

	public MorphoProcessor(WordNet wn, URL nounExceptionsURL,
			URL verbExceptionsURL, URL adjExceptionsURL) throws IOException {
		mWn = wn;
		readExceptions(nounExceptionsURL, mNounExceptions);
		readExceptions(verbExceptionsURL, mVerbExceptions);
		readExceptions(adjExceptionsURL, mAdjExceptions);
		mPosToExceptionsMap.put(POS.NOUN, mNounExceptions);
		mPosToExceptionsMap.put(POS.VERB, mVerbExceptions);
		mPosToExceptionsMap.put(POS.ADJECTIVE, mAdjExceptions);
	}

	public MorphoProcessor(WordNet wn, String base) throws IOException {
		this(wn, URI.create(base + "noun.exc").toURL(), URI.create(
				base + "verb.exc").toURL(), URI.create(base + "adj.exc")
				.toURL());
	}
	
	public MorphoProcessor(WordNet wn, File basedir) throws IOException {
		this(wn, new File(basedir, "noun.exc").toURI().toURL(), new File(basedir, "verb.exc").toURI().toURL(), new File(basedir, "adj.exc").toURI().toURL());
	}

	private void readExceptions(URL url, Map<String, String> exceptions)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(url
				.openStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] split = line.split("\\s+");
			exceptions.put(split[0], split[1]);
		}
		reader.close();
	}

	public IndexEntry lookupIndexEntry(POS pos, String word, boolean splitIfNecessary) {
		word = word.trim().toLowerCase().replaceAll("\\s+", "_");
		IndexEntry entry = mWn.getIndexEntry(pos, word);
		if (entry == null) {
			Map<String, String> exceptions = mPosToExceptionsMap.get(pos);
			if(exceptions != null) {
				String exceptionOption = exceptions.get(word);
				if (exceptionOption != null) {
					entry = mWn.getIndexEntry(pos, exceptionOption);
				}
			}
			if (entry == null) {
				List<SuffixRule> rules = POS_TO_RULES.get(pos);
				if(rules != null) {
					for (SuffixRule rule : rules) {
						String newWord = word.replaceAll(rule.originalSuffix,
							rule.newSuffix);
						entry = mWn.getIndexEntry(pos, newWord);
						if (entry != null) {
							break;
						}
					}
				}
			}
		}
		if (entry == null && splitIfNecessary) {
			String[] split = word.split("[^a-z]");
			if (split.length > 1) {
				entry = lookupIndexEntry(pos, split[split.length - 1], splitIfNecessary);
			}
		}
		return entry;
	}
	
	// What's the difference between these methods???????
	/*public IndexEntry lookupBaseEntry(POS pos, String word) {
		word = word.trim().toLowerCase().replaceAll("\\s+", "_");
		IndexEntry entry = mWn.getIndexEntry(pos, word);
		if (entry == null) {
			Map<String, String> exceptions = mPosToExceptionsMap.get(pos);
			if(exceptions != null) {
				String exceptionOption = exceptions.get(word);
				if (exceptionOption != null) {
					entry = mWn.getIndexEntry(pos, exceptionOption);
				}
			}
			if (entry == null) {
				List<SuffixRule> rules = POS_TO_RULES.get(pos);
				for (SuffixRule rule : rules) {
					String newWord = word.replaceAll(rule.originalSuffix,
							rule.newSuffix);
					entry = mWn.getIndexEntry(pos, newWord);
					if (entry != null) {
						break;
					}
				}
			}
		}
		if(entry == null) {
			entry = mWn.getIndexEntry(pos, word);
		}
		if (entry == null) {
			String[] split = word.split("[^a-z]");
			if (split.length > 1) {
				entry = lookupIndexEntry(pos, split[split.length - 1]);
			}
		}
		
		return entry;
	}*/

}