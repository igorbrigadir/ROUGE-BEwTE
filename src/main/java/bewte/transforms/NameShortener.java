package bewte.transforms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.annotations.LocationAnnotation;
import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;


import bewte.BE;

/**
 *  Should be renamed to NameTransform or something similar.
 *  
 *  Allows somewhat similar names to match
 *  John B. Smith <-> John Smith
 *  Google Inc <-> Google
 *  Michael <-> Michael Smith
 */
public class NameShortener extends FirstOrLastTransformer {

	private Map<String, Set<String>> mNonHumanNamesMap = new HashMap<String, Set<String>>();
	
	private Map<String, Set<String>> mLastNameSet = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> mFirstNameSet = new HashMap<String, Set<String>>();
	
	private Set<String> mCommonNameEndings = new HashSet<String>(Arrays.asList("jr", "sr", "jr.","sr.", "i", "ii", "iii", "iv", "v"));
	private Set<String> mTitles = new HashSet<String>(Arrays.asList("mr.", "mr", "mrs.", "mrs", "ms.", "ms", "dr.", "dr", "mssr.", "mssr", "fr.", "fr"));
	
	private Set<String> mCompanyEndings = new HashSet<String>(Arrays.asList("corp", "corp.", "co", "co.", "inc", "inc.", "ltd", "ltd.", "assn", "assn.", "association", "corporation", "company", "incoporated", "plc", "limited", "national", "international"));
		
	@Override
	public void reinitialize(List<BE> bes, List<BE> bes2, Set<String> modelLemmaSet) throws Exception {
		super.reinitialize(bes, bes2, modelLemmaSet);
		mLastNameSet.clear();
		mNonHumanNamesMap.clear();
		for(List<BE> beList : new List[]{bes, bes2}) {
			for(BE be : beList) {
				for(BE.BEPart part : be.getParts()) {
					if(part.type.equals(PersonAnnotation.class.getSimpleName())) {
						handleName(part.text.toLowerCase(), true);
					}
					else if(part.type.equals(OrganizationAnnotation.class.getSimpleName())) {
						String text = part.text.toLowerCase();
						String[] split = text.split("\\s+");
						if(split.length > 1 && mCompanyEndings.contains(split[split.length-1])) {
							String preEnding = text.substring(0, text.lastIndexOf(' '));
							Set<String> mappings = mNonHumanNamesMap.get(text);
							if(mappings == null) {
								mNonHumanNamesMap.put(text, mappings = new HashSet<String>());
							}
							mappings.add(preEnding);
							
							
							mappings = mNonHumanNamesMap.get(preEnding);
							if(mappings == null) {
								mNonHumanNamesMap.put(preEnding, mappings = new HashSet<String>());
							}
							mappings.add(text);
						}
					}
					else if(LocationAnnotation.class.getSimpleName().equals(part.type)) {
						String[] split = part.text.split("\\s+");
						if(split.length > 1) {
							String textLower = part.text.toLowerCase();
							StringBuilder buf = new StringBuilder();
							for(String s : textLower.split("\\s+")) {
								if(!s.matches("(south|east|west|north).*")) {
									buf.append(s).append(" ");
								}
							}
							String expansion = buf.toString().trim();
							
							Set<String> mappings = mNonHumanNamesMap.get(textLower);
							if(mappings == null) {
								mNonHumanNamesMap.put(textLower, mappings = new HashSet<String>());
							}
							mappings.add(expansion);
							
							
							mappings = mNonHumanNamesMap.get(expansion);
							if(mappings == null) {
								mNonHumanNamesMap.put(expansion, mappings = new HashSet<String>());
							}
							mappings.add(textLower);
						}
					}
				}
			}
		}
	}
	
	private Set<String> handleName(String text, boolean add) {
		text = text.toLowerCase();
		Set<String> newStrings = new HashSet<String>();
		String canonicalizedString = text.replaceAll("(\\.|\\)|\\(|\\[|\\]|\\\")", "");
		String[] split = canonicalizedString.split("\\s+");
		int numPieces = split.length;
		// comment this stuff...
		if(numPieces > 1) {
			if(mCommonNameEndings.contains(split[numPieces-1].toLowerCase())) {
				// .... Jr.
				action(add, split[numPieces-2], canonicalizedString, mLastNameSet, newStrings);
				if(numPieces > 2) {
					for(int i = 0; i < numPieces-2; i++) {
						// M. Knight Johnson Jr.
						if(split[i].length() > 1) {
							action(add, split[i], canonicalizedString, mFirstNameSet, newStrings);
						}
					}
				}
				else {
					// Bobby Jr.
					action(add, split[numPieces-2], canonicalizedString, mFirstNameSet, newStrings);	
				}
			}
			else {
				action(add,  split[numPieces-1], canonicalizedString, mLastNameSet, newStrings);
				for(int i = 0; i < numPieces-1; i++) {
					if(split[i].length() > 1) {
						action(add, split[i], canonicalizedString, mFirstNameSet, newStrings);
					}
				}
			}
		}
		else {
			action(add, split[0], canonicalizedString, mLastNameSet, newStrings);
			action(add, split[0], canonicalizedString, mFirstNameSet, newStrings);
		}
		return newStrings;
	}
	
	// TODO: stupid method name.. fix this
	private void action(boolean b, String s, String s2, Map<String, Set<String>> set, Set<String> newStrings) {
		if(b) {
			addLink(s, s2, set);
		}
		else {
			Set<String> strings = set.get(s);
			if(strings != null) {
				newStrings.addAll(strings);
			}
		}
	}
	
	private void addLink(String key, String value, Map<String, Set<String>> set) {
		Set<String> stringSet = set.get(key);
		if(stringSet == null) {
			set.put(key, stringSet = new HashSet<String>());
		}
		stringSet.add(value);
	}
	
	@Override
	public Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Set<String> newStrings = null;
		if(type.equals(PersonAnnotation.class.getSimpleName())) {
			text = text.toLowerCase();
			newStrings = handleName(text, false);
			if(newStrings != null) {
				newStrings = new HashSet<String>(newStrings);
				newStrings.retainAll(allModelStrings);
			}
		}
		else {
			newStrings = mNonHumanNamesMap.get(text.toLowerCase());
			if(newStrings != null) {
				newStrings = new HashSet<String>(newStrings);
				newStrings.retainAll(allModelStrings);
			}
		}
		Map<String, String> results = new HashMap<String, String>();
		if(newStrings != null) {
			for(String s : newStrings) {
				results.put(s, type);
			}
		}
		return results;
	}
	
}