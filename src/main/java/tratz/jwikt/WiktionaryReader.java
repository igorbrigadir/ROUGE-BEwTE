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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiktionaryReader {
	
	//Created for wiktionary version 20100403
	public static void main(String[] args) throws Exception {
		String wiktionaryPagesArticlesXml = args[0];
		String outputFile = args[1];
		
		BufferedReader reader = new BufferedReader(new FileReader(wiktionaryPagesArticlesXml));
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
		
		String line = null;
		
		Set<String> ignoreSet = new HashSet<String>();
		ignoreSet.add("Literature");
		ignoreSet.add("Etymology");
		ignoreSet.add("Etymology 1");
		ignoreSet.add("Etymology 2");
		ignoreSet.add("Etymology 3");
		ignoreSet.add("Etymology 4");
		ignoreSet.add("Etymology 5");
		ignoreSet.add("Etymology 6");
		ignoreSet.add("Etymology 7");
		ignoreSet.add("Pronunciation");
		ignoreSet.add("Pronunciation 1");
		ignoreSet.add("Pronunciation 2");
		ignoreSet.add("References");
		ignoreSet.add("Trivia");
		ignoreSet.add("External links");
		ignoreSet.add("Anagrams");
		ignoreSet.add("Translations");
		ignoreSet.add("Notes");
		ignoreSet.add("Usage notes");
		ignoreSet.add("Quotations");
		ignoreSet.add("Hyphenation");
		
		// Hard to see ever caring about these
		ignoreSet.add("Shorthand");
		ignoreSet.add("Descendants");
		ignoreSet.add("Inflection");
		
		// Don't care for now
		ignoreSet.add("Homophones");
		ignoreSet.add("Dictionary notes");
		ignoreSet.add("Homographs");
		ignoreSet.add("Homonyms");
		
		// Not sure what to do with for now, maybe ignoring is best
		ignoreSet.add("Alternative spellings");
		ignoreSet.add("Variant spellings");
		ignoreSet.add("Alternate forms");
		ignoreSet.add("Alternative forms");
		ignoreSet.add("Alternative abbreviations");
		ignoreSet.add("Particle");
		ignoreSet.add("Interjection");
		ignoreSet.add("Contraction");
		ignoreSet.add("Cardinal number");
		ignoreSet.add("Ordinal number");
		ignoreSet.add("Letter");
		ignoreSet.add("Proverb");
		ignoreSet.add("Prefix");
		ignoreSet.add("Suffix");
		ignoreSet.add("Infix");
		ignoreSet.add("History");
		ignoreSet.add("Combining form");
		ignoreSet.add("Affix");
		ignoreSet.add("Interfix");
		ignoreSet.add("Numeral");
		ignoreSet.add("Number");
		ignoreSet.add("Postposition");
		
		ignoreSet.add("Abbreviations");
		ignoreSet.add("Symbol");
		
		// would like to pick up some of these someday
		ignoreSet.add("Phrase");
		ignoreSet.add("Idiom");
		
		// limited in quality and frequency
		ignoreSet.add("Meronyms");
		ignoreSet.add("Interjection");
		
		// Only 1 instance
		ignoreSet.add("Personal noun");
		ignoreSet.add("Similar terms");
		ignoreSet.add("Taxonomic names");
		ignoreSet.add("Adverb phrase");
		ignoreSet.add("Possessive adjective"); // possessive pronoun
		ignoreSet.add("Standard");
		ignoreSet.add("Variant");
		ignoreSet.add("America");
		ignoreSet.add("Canada");
		ignoreSet.add("Cyrillic spelling");
		ignoreSet.add("Phrases"); // not to be confused with Phrase
		ignoreSet.add("Translingual");
		ignoreSet.add("Related");
		ignoreSet.add("Collocations");
		
		// Only a few instances
		ignoreSet.add("Verb form");
		ignoreSet.add("Derived phrase");
		ignoreSet.add("Use");
		ignoreSet.add(" Alternate forms ");
		ignoreSet.add(" Usage ");
		ignoreSet.add("Usage");
		ignoreSet.add("Variation");
		ignoreSet.add("Compounds");
		ignoreSet.add("Conjugation");
		ignoreSet.add("Declension");
		ignoreSet.add("Relation");
		ignoreSet.add("Misspelling");
		ignoreSet.add("Lujvo");
		ignoreSet.add("Gregg");
		ignoreSet.add("Examples");
		ignoreSet.add("Circumfix");
		ignoreSet.add("Derived Meanings");
		ignoreSet.add("Combining Form");
		ignoreSet.add("Preposition phrase");
		ignoreSet.add("Prepositional phrase");
		
		
		
		int count = 0;
		Set<String> allTerms = new HashSet<String>();
		boolean printFromPage = false;
		boolean inEnglishSection = false;
		boolean inInterestingSubsection = false;
		
		Matcher languageMatcher = Pattern.compile("==([^\\]=]*)==\\s*").matcher("");
		Matcher typeMatcher = Pattern.compile("====*([^\\]=]*)=*===\\s*").matcher("");

		//Matcher wikiSaurusMatcher = Pattern.compile("\\s*<title>Wikisaurus:(.*)</title>\\s*").matcher("");
		Matcher titleMatcher = Pattern.compile("\\s*<title>(.*)</title>\\s*").matcher("");
		Matcher appendixTitleMatcher = Pattern.compile("\\s*<title>((Wikisaurus|Image|WS|WT|Concordance|Special|User|File|MediaWiki|Rhymes|Transwiki|Citations|MediaWiki|Index|Template|Category|Help|Appendix|Wiktionary)([\\s_]talk)?):(.*)</title>\\s*").matcher("");
		
		String title = null;
		
		StringBuilder buf = new StringBuilder();
		
		int sinceLastMatch = 1000;
		boolean bad = false;
		boolean bad2 = false;
		int numHashes = 0;
		boolean inComment = false;
		while((line = reader.readLine()) != null) {
			line = line.replace("WikiSaurus", "Wikisaurus");
			line = line.replace("[[wikisaurus:", "[[Wikisaurus");
			line = line.replace("&mdash;", "—");
			
			if(line.trim().startsWith("#: All drinks are free")){//("#: All drinks are free <!--#:free of [[charge]] this example not unambiguous; could be the adverb-->")) {
				System.err.println("Got it.");
				System.err.println(line);
			}
			// Eliminate in-line comments
			line = line.replace("&lt;","<").replace("&gt;", ">").replaceAll("<\\!--.*-->", "");
			// Ignore refs for now
			line = line.replaceAll("<ref [^>]*/>","");
			line = line.replaceAll("<ref [^>]*>.*</ref>","");
			
			line = line.replaceAll("\\s*<text[^>]*>","");
			line = line.replaceAll(".*<comment>.*</comment>\\s*", "");
			
			if(titleMatcher.reset(line).matches()) {
				
				printFromPage = !appendixTitleMatcher.reset(line).matches();
				inEnglishSection = false;
				title = titleMatcher.group(1);
				if(title.contains("managee")) {
					sinceLastMatch = 0;
					System.err.println(line);
				}
				//if(!bad && (!bad2||numHashes>1)) 
					writer.print(buf.toString());
				bad = false;
				bad2 = false;
				numHashes = 0;
				buf.setLength(0);
				//if(printFromPage) writer2.println("TITLE:"+title);
				inComment = false;
			}
			if(sinceLastMatch < 40) {
				System.err.println(line);
			}
			

			
			// Eliminate multi-line comments
			if(line.contains("<!--") && !line.contains("-->")) {
				inComment = true;
				if(!line.startsWith("<!--")) {
					line = line.substring(0, line.indexOf("<!--"));	
				}
			}
			if(line.contains("-->") && !line.contains("<!--")) {
				line = line.trim();
				inComment = false;
				if(!line.endsWith("-->")) {
					line = line.substring(line.indexOf("-->")+3);
				}
				else {
					line = "";
				}
			}
			if(inComment) continue;
			
			
			sinceLastMatch++;
			if(printFromPage) {
				if(line.trim().startsWith("[[Category:English plurals")) {
					bad = true;
				}
				if(languageMatcher.reset(line).matches()) {
					inEnglishSection = languageMatcher.group(1).equals("English");
					if(inEnglishSection) {
						buf.append("TITLE:"+title.replace("&amp;", "&")).append("\n");
						inInterestingSubsection = true;
					}
					continue;
				}
			}
			// careful.. this may throw stuff away
			if(line.contains("</text>")) {
				inEnglishSection = false;
				inInterestingSubsection = false;
			}
			if(inEnglishSection) {
				if(typeMatcher.reset(line).matches()) {
					String subsection = typeMatcher.group(1).trim();
					if(!ignoreSet.contains(subsection)) {
						allTerms.add(subsection);
					}
					inInterestingSubsection = !ignoreSet.contains(subsection);
				}
				if(inInterestingSubsection) {
					line = line.trim();
					// Remove collapsible tables
					if(line.startsWith("{{rel-")) continue;
					if(line.startsWith("{{trans-")) continue;
					// Remove always-shown tables
					if(line.equals("{{top4}}")) continue;
					if(line.equals("{{mid4}}")) continue;
					if(line.equals("{{top3}}")) continue;
					if(line.equals("{{mid3}}")) continue;
					if(line.equals("{{top2}}")) continue;
					if(line.equals("{{mid2}}")) continue;
					if(line.equals("{{top}}")) continue;
					if(line.equals("{{mid}}")) continue;
					if(line.equals("{{bottom}}")) continue;
					if(line.equals("{{der-top}}")) continue;
					if(line.equals("{{der-mid}}")) continue;
					if(line.equals("{{der-bottom}}")) continue;
					
					if(line.matches("\\[\\[([a-z]{2,3}|simple|zh-min-nan):.*")) continue;
					// Remove Images
					if(line.startsWith("[[Image:")) continue;
					// Remove audio
					if(line.matches("\\* ?\\{\\{audio.*")) continue;
					// Ignore small wikipedia references
					if(line.matches("\\* ?\\{\\{pedialite"))continue;
					// Ignore large wikipedia references
					if(line.equals("{{wikipedia}}")) continue;
					if(line.startsWith("{{wikipedia|")) continue;
					// Ignore 'no etymology info' template
					if(line.startsWith("{{rfe")) continue;
					
					// Ignore rank info
					if(line.startsWith("{{rank|")) continue;
					
					if(line.matches("\\* ?\\{\\{Wikisource"))continue;
					
					// Ignore worthless categories
					if(line.matches("\\[\\[Category:Dictionary notes\\]\\]")) continue;
					if(line.trim().startsWith("[[Category:English plurals")) continue;
					
					if(line.equals("----")) continue;
					if(line.startsWith("# {{plural of|")) {
						bad2= true;
					}
					if(line.startsWith("# ")) {
						numHashes++;
					}
					// &!
					// replace("\\{\\{italbrac\\|([^\\}]*)\\}\\}","(\1)")
					buf.append(line.replace("'''", "").replace("''","").replace("&amp;", "&").replace("&quot;","\"")).append("\n");
				}
			}
			count++;
			//if(count > 1000 ){// 100000000) {
			//  break;
			//}
		}
		if(!bad) writer.print(buf.toString());
		writer.close();
		reader.close();
		//System.err.println("Num types: " + allClasses.size() + " " + jcount + " " + count);
		List<String> list = new ArrayList<String>(allTerms);
		Collections.sort(list);
		System.err.println("Print:");
		for(String s : list) {
			System.err.println(s);
		}
		System.err.println(list.size());
	}
	
}