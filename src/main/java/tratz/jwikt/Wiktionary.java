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

package tratz.jwikt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.jwikt.WEntry.Definition;


public class Wiktionary {
	
	protected static Wiktionary mGlobal;
	public static Wiktionary getGlobal() {
		return mGlobal;
	}
	
	public static void setGlobal(Wiktionary wiktionary) {
		mGlobal = wiktionary;
	}
	
	private List<String> mAllTerms;
	private Map<WPOS, Map<String, WEntry>> mPosToTermToEntry;
	private Map<WEntry, Map<LinkType, List<String>>> mEntryToLinkTypeToString;
	
	public Wiktionary(Map<WPOS, Map<String, WEntry>> posToTermToEntry, 
			          Map<WEntry, Map<LinkType, List<String>>> entryToLinkTypeToString, 
			          List<String> allTerms) {
		mAllTerms = allTerms;
		mPosToTermToEntry = posToTermToEntry;
		mEntryToLinkTypeToString = entryToLinkTypeToString;
	}
	
	public Map<WPOS, Map<String, WEntry>> getMap() {
		return mPosToTermToEntry;
	}
	
	public Map<WEntry, Map<LinkType, List<String>>> getLinkMap() {
		return mEntryToLinkTypeToString;
	}
	
	public List<String> getTerms() {
		return mAllTerms;
	}
	
	public WEntry getEntry(WPOS pos, String term) {
		Map<String, WEntry> termToEntry = mPosToTermToEntry.get(pos);
		return termToEntry == null ? null : termToEntry.get(term);
	}
	
	
	
	public static Wiktionary readWiktionary(String inFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		String line = null;
		
		Set<String> cats = new HashSet<String>();
		final Map<String, Integer> sToCount = new HashMap<String, Integer>();
		
		Object[][] stringToLinkType = new Object[][]{
				{"Antonyms",LinkType.ANTONYM},
				{"Synonyms",LinkType.SYNONYM},
				{"Related terms",LinkType.RELATED_TERM},
				{"Derived terms",LinkType.DERIVED_TERM},
				{"See also",LinkType.SEE_ALSO},
				{"Hyponyms", LinkType.HYPONYM},
				{"Troponyms", LinkType.HYPONYM},
				{"Scientific names", LinkType.SCIENTIFIC_NAME},
				{"Hypernyms", LinkType.HYPERNYM},
				{"Holonyms", LinkType.HOLONYM},
				{"Meronymns", LinkType.MERONYM},
				{"Coordinate terms", LinkType.COORDINATE_TERM}
		};
		Map<String, LinkType> stringToLinkTypeMap = new HashMap<String, LinkType>();
		for(Object[] mapping : stringToLinkType) {
			String s = (String)mapping[0];
			LinkType type = (LinkType)mapping[1];
			stringToLinkTypeMap.put(s, type);
		}
		Map<WEntry, Map<LinkType, List<String>>> entryToLinkTypeToStrings = new HashMap<WEntry, Map<LinkType,List<String>>>();
		
		Map<String, WPOS> pstringToPos = new HashMap<String, WPOS>();
		Object[][] maps = new Object[][]{
				{"Adjective", WPOS.ADJECTIVE},
				{"Adverb", WPOS.ADVERB},
				{"Determiner", WPOS.DETERMINER},
				{"Noun", WPOS.NOUN},
				{"Verb", WPOS.VERB},
				{"Article", WPOS.ART},
				{"Conjunction", WPOS.CONJ},
				{"Pronoun", WPOS.PRONOUN},
				{"Abbreviation", WPOS.ABBREV},
				{"Proper noun", WPOS.PROPER_NOUN},
				{"{{acronym}}", WPOS.ACRONYM},
				{"{{acronym|en}}", WPOS.ACRONYM},
				{"{{acronym|English}}", WPOS.ACRONYM},
				{"{{acronym|mul}}", WPOS.ACRONYM},
				{"Acronym", WPOS.ACRONYM},
				{"{{abbreviation|en}}", WPOS.ABBREV},
				{"{{abbreviation|English}}", WPOS.ABBREV},
				{"{{abbreviation}}", WPOS.ABBREV},
				{"Initialism", WPOS.INITIALISM},
				{"Preposition", WPOS.PREP},
				{"{{initialism|English}}", WPOS.INITIALISM},
				{"{{initialism|english}}", WPOS.INITIALISM},
				{"{{initialism|en}}", WPOS.INITIALISM},
				{"{{initialism|eu}}", WPOS.INITIALISM},
				{"{{initialism}}", WPOS.INITIALISM}
		};
		for(Object[] mapping : maps) {
			pstringToPos.put((String)mapping[0], (WPOS)mapping[1]);
		}
		
		Set<String> allTerms = new HashSet<String>();
		boolean printFromPage = false;
		boolean inInterestingSubsection = false;
		
		Matcher languageMatcher = Pattern.compile("==([^\\]=]*)==\\s*").matcher("");
		Matcher typeMatcher = Pattern.compile("====*([^\\]=]*)=*===\\s*").matcher("");

		//Matcher wikiSaurusMatcher = Pattern.compile("\\s*<title>Wikisaurus:(.*)</title>\\s*").matcher("");
		Matcher titleMatcher = Pattern.compile("TITLE:(.*)").matcher("");
		Matcher appendixTitleMatcher = Pattern.compile("\\s*<title>((Wikisaurus|Image|WS|WT|Concordance|Special|User|File|MediaWiki|Rhymes|Transwiki|Citations|MediaWiki|Index|Template|Category|Help|Appendix|Wiktionary)([\\s_]talk)?):(.*)</title>\\s*").matcher("");
		
		String title = null;
		String sectionType = null;
		Map<WPOS, Map<String, WEntry>> posToTermToEntry = new HashMap<WPOS, Map<String,WEntry>>();
		WPOS pos = null;
		LinkType linkType = null;
		WEntry currentEntry = null;
		WEntry.Definition def = null;
		List<String> linkStrings = null;
		Map<LinkType, List<String>> linkTypeToStrings = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("[[Category:")) {
				cats.add(line);
				continue;// These are so hit-and-miss there doesn't seem to be much point in keeping them
			}
			if(titleMatcher.reset(line).matches()) {
				printFromPage = !appendixTitleMatcher.reset(line).matches();
				title = titleMatcher.group(1);
				continue;
			}
			if(printFromPage) {
				
				if(languageMatcher.reset(line).matches()) {
					continue;
				}
			}
			if(line.contains("</text>")) {
				inInterestingSubsection = false;
				continue;
			}
			
				
				if(typeMatcher.reset(line).matches()) {
					String subsection = typeMatcher.group(1).trim();
					allTerms.add(subsection);
					sectionType = subsection;
					
					pos = pstringToPos.get(sectionType);
					if(pos != null) {
						linkTypeToStrings = new HashMap<LinkType, List<String>>();
						entryToLinkTypeToStrings.put(currentEntry = new WEntry(title, pos), linkTypeToStrings);
						Map<String, WEntry> termToEntry = posToTermToEntry.get(pos);
						if(termToEntry == null) {
							posToTermToEntry.put(pos, termToEntry = new HashMap<String, WEntry>());
						}
						termToEntry.put(title, currentEntry);
					}
					else {
						linkType = null;
						linkType = stringToLinkTypeMap.get(sectionType);
						if(linkType != null) {
							linkTypeToStrings.put(linkType, linkStrings = new ArrayList<String>());
						}
						else {
							// hit something were pos == null && linkType == null
							// this is unexpected
							System.err.println(line);
						}
					}
					continue;
				}
				if(pos != null) {
					
					if(line.startsWith("#:")) {
						// Begin example
						def.getExamples().add(line.substring(2).trim().replaceAll("\\[\\[(w:)?([^\\]\\|]*)[^\\]]*\\]\\]","$2"));
					}
					else if(line.startsWith("#*:")) {
						// Begin cited example
						// IGNORE THESE FOR NOW
					}
					else if(line.startsWith("#:") && line.contains("—")) {
						// Treat as cited example (IGNORED FOR NOW)
					}
					else if(line.startsWith("#*")) {
						// Begin cite
						
					}
					else if(line.startsWith("#")) {
						// Begin definition
						String gpart = line.substring(1).trim();
						if(gpart.startsWith("{{") && !gpart.startsWith("{{superlative of") && !gpart.startsWith("{{alternative form of") && !gpart.startsWith("{{comparative of") && !gpart.startsWith("{{plural of") && !gpart.startsWith("{{present participle") && !gpart.startsWith("{{third-person") &&!gpart.startsWith("{{alternative spelling") && !gpart.startsWith("{{past of") && gpart.indexOf("}}")>0) {
							for(String s : gpart.substring(2, gpart.indexOf("}")).split("\\|")) {
								Integer count = sToCount.get(s);
								if(count == null) {
									count = new Integer(0);
								}
								sToCount.put(s, count.intValue()+1);
							}
							gpart = gpart.substring(gpart.indexOf("}}")+2);
						}
						String gloss = line.substring(1).trim().replaceAll("\\[\\[(w:|wikipedia:|Wikipedia:)?([^\\]\\|]*)([^\\]])*\\]\\]","$2");
						gloss = gloss.trim();
						if(!gloss.startsWith("{{superlative of") && !gloss.startsWith("{{comparative of")) {
							gloss = gloss.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
						}
						gloss = gloss.trim();
						if(!gloss.equals("")) {
						def = new WEntry.Definition(gloss);
						currentEntry.addDefinition(def);
						}
					}
					else if(!line.trim().equals("")){
						currentEntry.mJunk.add(line);
						// {{en-adj|-}} -> not comparative
						// {{en-adj}} -> more/most for comparative/superlative
						// {{en-adj|x|ier}} -> x is base form with a -y, becomes -ier/iest
						// {{en-noun|-}} -> not countable
						// {{en-noun|-|s}} -> usually uncountable, sometimes takes -s
						// {{en-noun|s|-}} -> countable and uncountable
						// {{en-noun}} -> takes an -s
						// {{en-noun|pl=hypertrichoses}} -> unnormal plural
						// {{en-noun|hypertroph|ies|-}} -> y to -ies pluralization
						// {{en-noun|hyperviscosities|-}} -> another plural
					}
					
					// &!
					// replace("\\{\\{italbrac\\|([^\\}]*)\\}\\}","(\1)")
				//	writer2.println(line.replace("'''", "").replace("''","").replace("&amp;", "&").replace("&lt;","<").replace("&gt;", ">").replace("&quot;","\"").replaceAll("<\\!--.*-->", ""));
				}
				else if(linkType != null) {
					if(line.startsWith("*")) {
						line = line.substring(1).trim();
						if(line.startsWith("*") && line.length() > 1) {
							line = line.substring(1).trim();
						}
						// Synonym pattern
						line = line.replaceAll("\\s*See also\\s*\\[\\[Wikisaurus:([^\\]]*)\\]\\]","$1");
						
						linkStrings.add(line);
					}
					else if (!line.trim().equals("")){
						// ignore for now?
						//linkStrings.add("$$$$ " + line.trim());
					}
				}
			}
		
		
		reader.close();
		List<String> list = new ArrayList<String>(allTerms);
		Collections.sort(list);
		
		// NON ESSENTIAL
	/*	List<String> defTags = new ArrayList<String>(sToCount.keySet());
		Collections.sort(defTags, new Comparator<String>() 
				{
			public int compare(String s1, String s2) {
				int c1 = sToCount.get(s1);
				int c2 = sToCount.get(s2);
				int diff = c1-c2;
				if(diff == 0) {
					return s1.compareTo(s2);
				}
				else {
					return diff;
				}
			}
		});
		for(String dtag : defTags) {
	//		System.err.println(dtag + " " + sToCount.get(dtag));
		}
		for(String cat : cats) {
			System.err.println(cat);
		}*/
		//
		
		return new Wiktionary(posToTermToEntry, entryToLinkTypeToStrings, list);
	}
	
	public static void main(String[] args) throws Exception {
		PrintWriter writer = new PrintWriter(new FileWriter("/media/1500GB_/regurge.txt"));
		//PrintWriter emptyWriter = new PrintWriter(new FileWriter("/media/1500GB/empty.txt"));
		Wiktionary wiktionary = readWiktionary("/media/1500GB_/limited");
		List<String> list = wiktionary.getTerms();
		Map<WPOS, Map<String, WEntry>> posToTermToEntry = wiktionary.getMap();
		Map<WEntry, Map<LinkType, List<String>>> entryToLinkTypeToStrings = wiktionary.getLinkMap();
		
		System.err.println("Print:");
		for(String s : list) {
			System.err.println(s);
		}
		System.err.println(list.size());
		for(WPOS wpos : posToTermToEntry.keySet()) {
			Map<String, WEntry> termToEntry = posToTermToEntry.get(wpos);
			List<String> allEntries = new ArrayList<String>(termToEntry.keySet());
			Collections.sort(allEntries);
			for(String entryName : allEntries) {
				WEntry entry = termToEntry.get(entryName);
				if(entry.getDefinitions().size() == 0) continue;
				
				// skipping
				writer.println(entry.getTerm() + "\t" + entry.getPos());
				/*if(entry.getDefinitions().size() == 0) {
					emptyWriter.println("$$$ " + entry.getTerm() + "\t" + entry.getPos());
				}*/
				for(Definition def2 : entry.getDefinitions()) {
					writer.println("\t"+def2.getGloss());
					for(String ex : def2.getExamples()) {
						writer.println("\t\t" + ex);
					}
				}
				/*for(String s : entry.mJunk) {
					writer.println(" j:"+s);
				}*/
				Map<LinkType, List<String>> linkTypeToStrings2 = entryToLinkTypeToStrings.get(entry);
				for(LinkType lt : linkTypeToStrings2.keySet()) {
					List<String> targets = linkTypeToStrings2.get(lt);
					
					writer.println("\t"+lt);
					for(String s : targets) {
						writer.println("\t\t" + s);
					}
					
				}
			}
		}
		writer.close();
		//emptyWriter.close();
		
		
	}
	
}