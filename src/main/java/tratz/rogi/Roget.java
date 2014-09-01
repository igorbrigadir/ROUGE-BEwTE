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

package tratz.rogi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for Roget's thesaurus
 *
 */
public class Roget {

	public static Roget init() throws IOException {
		return mRoget = new Roget(Roget.class.getResource("/roget15aMod.txt"));
	}
	
	public static Roget getInstance() {
		return mRoget;
	}
	private static Roget mRoget;
	
	static int count = 0;
	
	public final Matcher NEW_ENTRY_MATCHER = Pattern.compile(
	"\\s*#([0-9ab]+)\\.(.*)--\\s+N\\.(.*)").matcher("");
	
	// &c. 120.
	// &c. (existence in space) 186
	private static final Matcher LINK_MATCHER1 = Pattern
			.compile(
					"(.+)&c(\\.)?\\s*(\\(([^0-9]*)\\))?\\s*([0-9ab]+)\\s*(\\[.*\\])?\\.?")
			.matcher("");

	private static final Matcher LINK_MATCHER2 = Pattern.compile(
			"(.+)&c(\\.)?\\s*(n|v|V|adj|Adj|adv)\\.?").matcher("");

	private static final Matcher LINK_MATCHER3 = Pattern
			.compile("(.+)&c(\\.)?\\s+([^0-9]+).*").matcher("");

	// &c. n.
	
	public final static int Thesaurus = 0, Class = 1, Division = 2,
			Section = 3, Subsection = 4, Unnamed = 5, Number = 6, Numeral = 7,
			Dash = 8;
	
	public static enum POS {N, V, ADJ, ADV, PHR, INT};

	public static class Division {
		private final String mName;
		private final int mType;
		private final Division mParent;
		private final List<Division> mDivisions = new ArrayList<Division>(0);
		private final List<Entry> mEntries = new ArrayList<Entry>(0);
		
		public Division(String name, int type, Division parent) {
			mName = name;
			mType = type;
			mParent = parent;
		}

		public String getName() {
			return mName;
		}
		
		public int getType() {
			return mType;
		}
		
		public Division getParent() {
			return mParent;
		}
		
		public List<Division> getDivisions() {
			return mDivisions;
		}
		
		public List<Entry> getEntries() {
			return mEntries;
		}
	}

	public static class Entry {
		private final Division mDivision;
		private final String mNum;
		private final String mName;

		private final Map<POS, List<Set<Term>>> mPosToTerms = new HashMap<POS, List<Set<Term>>>(); 

		public Entry(Division division, String num, String name) {
			mDivision = division;
			mNum = num;
			mName = name;
			mPosToTerms.put(POS.N, new ArrayList<Set<Term>>());
			mPosToTerms.put(POS.V, new ArrayList<Set<Term>>());
			mPosToTerms.put(POS.ADJ, new ArrayList<Set<Term>>());
			mPosToTerms.put(POS.ADV, new ArrayList<Set<Term>>());
			mPosToTerms.put(POS.PHR, new ArrayList<Set<Term>>());
			mPosToTerms.put(POS.INT, new ArrayList<Set<Term>>());
		}
		
		public Division getDivision() {
			return mDivision;
		}
		
		public String getNum() {
			return mNum;
		}
		
		public String getName() {
			return mName;
		}
		
		public List<Set<Term>> getTerms(POS pos) {
			return mPosToTerms.get(pos);
		}
	}

	public static class Term {
		private final Entry mEntry;
		private final String mTerm;
		private final List<Link> mLinks = new ArrayList<Link>(0);
		
		
		public Term(String term, Entry entry) {
			mTerm = term;
			mEntry = entry;
		}
		
		public Entry getEntry() {
			return mEntry;
		}

		public String getString() {
			return mTerm;
		}
		
		public List<Link> getLinks() {
			return mLinks;
		}
		
		@Override
		public String toString() {
			return mTerm;
		}
		
	}

	public static class Link {
		int linkNum;
		String linkType;
		String linkTermText;
	}	

	private Division mTopDivision;
	private Map<POS, Map<String, Set<Term>>> mPosToTerm = new HashMap<POS, Map<String,Set<Term>>>();
	private Map<String, Set<Term>> mStringToTerm = new HashMap<String,Set<Term>>();
	public Set<Term> getTerms(POS pos, String string) {
		return mPosToTerm.get(pos).get(string);
	}
	
	public Map<String, Set<Roget.Term>> getTermsMap(POS pos) {
		return mPosToTerm.get(pos);
	}
	
	public Set<Term> getTerms(String string) {
		return mStringToTerm.get(string);
	}

	private Roget(URL fileURL) throws IOException {
		if(mRoget == null) {
			mRoget = this;
		}
		Division currentDivision = new Division("Roget's Thesaurus", Thesaurus,	null);
		mTopDivision = currentDivision;

		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
		StringBuilder temp = new StringBuilder();
		boolean notFoundYet = true;
		Entry currEntry = null;
		POS currPOS = POS.N;

		while((line = reader.readLine()) != null) {
			
			if(line.startsWith("*** END OF THE PROJECT GUTENBERG EBOOK")) {
				//Found the end
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				break;
			}
			if(notFoundYet) {
				if(line.matches("\\s*BEGIN THESAURUS\\s*")) {
					notFoundYet = false;
				}
				continue;
			}

			if(line.matches("\\s*<--.*-->\\s*")) { //line.matches("\\s*<--.*p\\..*-->")) {
				continue;
				// Entry break
				// System.err.println(line);
			}
			line = line.replace("[obs3]", "");
			line = line.replace("|", "");
			line = line.replace("!", "");
			line = line.replace("&c,", "&c.");
			line = line.replace("&c..", "&c.");
			if(line.matches("\\s*[0-9]\\..*")) {
				// System.err.println(line);
			}
			if(line.startsWith("%")) { // we have some sort of subsection
				String name = line.substring(1).trim();
				int type;
				if(line.contains("CLASS")) {
					type = Class;
				}
				else if(line.contains("SUBSECTION")) {
					type = Subsection;
				}
				else if(line.contains("SECTION")) {
					type = Section;
				}
				else if(line.contains("DIVISION")) {
					type = Division;
				}
				else if(line.matches("%\\([0-9]*\\).*")) {
					type = Number;
				}
				else if(line.matches("%\\s*\\([iv]*\\).*")) {
					type = Numeral;
				}
				else if(line.startsWith("%-")) {
					type = Dash;
				}
				else {
					type = Unnamed;
				}
				while(type <= currentDivision.mType) {
					currentDivision = currentDivision.mParent;
				}
				Division newDivision = new Division(name, type, currentDivision);
				currentDivision.mDivisions.add(newDivision);
				currentDivision = newDivision;
				// if(line.matches("\\s*(%|SUBSECTION|SECTION|CLASS|DIVISION).*"))
				// {
				// System.err.println(line);
				// }
				continue;
			}

			String lt = line.trim();
			if(lt.startsWith("V.") || lt.startsWith("V[")) {
				if(lt.startsWith("V[")) {
					lt = lt.substring(lt.indexOf("].") + 2);
				}
				else {
					lt = lt.substring(2);
				}
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				
				temp.setLength(0);
				currPOS = POS.V;
				temp.append(lt).append(" ");
			}
			else if(lt.startsWith("Adj.")) {
				lt = lt.substring(4);
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				temp.setLength(0);
				currPOS = POS.ADJ;
				temp.append(lt).append(" ");
			}
			else if(lt.startsWith("Int.")) {
				lt = lt.substring(4);
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				temp.setLength(0);
				currPOS = POS.INT;
				temp.append(lt).append(" ");
			}
			else if(lt.startsWith("Phr.") | lt.startsWith("Phr[")) {
				if(lt.startsWith("Phr[")) {
					lt = lt.substring(lt.indexOf("].") + 2);
				}
				else {
					lt = lt.substring(4);
				}
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				temp.setLength(0);
				currPOS = POS.PHR;
				temp.append(lt).append(" ");
			}
			else if(lt.startsWith("Adv.")
					|| lt.matches("\\s*#[0-9ab]+\\.(.*)--\\s+Adv\\..*")) {
				lt = lt.substring(4);
				currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
				temp.setLength(0);
				currPOS = POS.ADV;
				temp.append(lt).append(" ");
			}
			else if(lt.startsWith("#")) {
				if(currEntry != null) {
					currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
					temp.setLength(0);
				}

				NEW_ENTRY_MATCHER.reset(lt);
				if(!NEW_ENTRY_MATCHER.matches()) {
					//System.err.println(line);
				}
				String g1 = NEW_ENTRY_MATCHER.group(1);
				String g2 = NEW_ENTRY_MATCHER.group(2).trim();
				String g3 = NEW_ENTRY_MATCHER.group(3);
				currEntry = new Entry(currentDivision, g1, g2);
				currentDivision.mEntries.add(currEntry);
				currPOS = POS.N;
				temp.append(g3).append(" ");
			}
			else if(!lt.equals("") && line.startsWith("     ")) {
				if(currEntry == null) {
					//System.err.println("IGNORING: " + line);
				}
				else {

					currEntry.mPosToTerms.get(currPOS).add(createTermSet(temp.toString(), currEntry, currPOS));
					temp.setLength(0);
					temp.append(lt).append(" ");
				}
			}
			else if(!lt.equals("")) {
				temp.append(lt).append(" ");
			}

			// NewEntry encountered

		}
		temp.append(line).append(" ");

		reader.close();
	}

	public Division getTopDivision() {
		return mTopDivision;
	}
	
	private Set<Term> createTermSet(String input, Entry entry, POS pos) {
		String[] pieces = input.split("[,;]");
		Set<Term> termSet = new HashSet<Term>();
		for(String piece : pieces) {
			piece = piece.trim();

			LINK_MATCHER1.reset(piece);
			LINK_MATCHER2.reset(piece);
			LINK_MATCHER3.reset(piece);

			String termPart = piece;

			if(LINK_MATCHER1.matches()) {
				String g1 = LINK_MATCHER1.group(1);
				String g2 = LINK_MATCHER1.group(2);
				String g3 = LINK_MATCHER1.group(3);
				String g4 = LINK_MATCHER1.group(4);
				String g5 = LINK_MATCHER1.group(5);
				if(g5 != null) {
					// System.err.println("3:" + g3 + " 4:" + g4 + " 5:" + g5 +
					// " FROM " + piece);
				}
				// termPart = piece+"{1:"+g1.trim()+"}";
				termPart = g1.trim();
			}
			else if(LINK_MATCHER2.matches()) {
				String g1 = LINK_MATCHER2.group(1);
				String g2 = LINK_MATCHER2.group(2);
				termPart = g1.trim();
				// System.err.println("2:" + g2 + " FROM " + piece);

			}
			else if(LINK_MATCHER3.matches()) {
				// Don't know what to do
				// be an excude &c. n. for
				// worldly &c. minded.
			}
			else if(piece.endsWith("&c.") || piece.endsWith("&c")) {
				// Don't know what to do...
			}
			else if(piece.contains("&c")) {
				// Still don't know what to do
				//System.err.println(piece);
				//System.err.println(count++);
			}
			else if(piece.contains("&c") && !input.matches(".*&c. [^;, ]*\\s*")) {

				if(!piece.startsWith("&c") && !piece.endsWith("&c.")) {
					// System.err.println(piece + " -> " + input);
				}
			}

			if(termPart.endsWith(".") && termPart.length() > 1) {
				termPart = termPart.substring(0, termPart.length() - 1);
			}
			if(!termPart.equals("") && !termPart.equals(".")) {
			Term newTerm = new Term(termPart, entry);
			Set<Term> terms = mStringToTerm.get(termPart);
			if(terms == null) {
				mStringToTerm.put(termPart, terms = new HashSet<Term>());
			}
			terms.add(newTerm);
			
			Map<String, Set<Term>> sToTerm = mPosToTerm.get(pos);
			if(sToTerm == null) {
				mPosToTerm.put(pos, sToTerm = new HashMap<String, Set<Term>>());
			}
			terms = sToTerm.get(termPart);
			if(terms == null) {
				sToTerm.put(termPart, terms = new HashSet<Term>());
			}
			/*if(!newTerm.mTerm.matches("[a-zA-Z0-9 \\]\\[\\)\\(\\*~:;_'\\-\\.\\\"\\&\\?]+")) {
				System.err.println("ARG: " + newTerm.mTerm);
			}*/
			terms.add(newTerm);
			
			termSet.add(newTerm);
			}
		}
		return termSet;
	}

}
