package bewte.transforms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;

/**
 * be -> {is,are,was,...}
 * jump -> {jumped, jumping, jumps}
 * etc.
 */
public class DeLemmaTransform extends FirstOrLastTransformer {
	
	private final Matcher ES_ENDING = Pattern.compile(".*(s|z|x|ch|sh)").matcher("");
	private final Matcher VOWEL_Y_ENDING = Pattern.compile(".*[^aeiou]y").matcher(""); 
	
	public final static String PARAM_VERB_EXCEPTIONS_FILE = "VERB_EXCEPTIONS";
	public final static String PARAM_NOUN_EXCEPTIONS_FILE = "NOUN_EXCEPTIONS";
	public final static String PARAM_ADJ_EXCEPTIONS_FILE = "ADJ_EXCEPTIONS";
	
	private Map<String, Set<String>> mVerbExceptionsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> mNounExceptionsMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> mAdjExceptionsMap = new HashMap<String, Set<String>>();
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		super.initialize(params);
		readExceptionsList(mVerbExceptionsMap, params.get(PARAM_VERB_EXCEPTIONS_FILE));
		readExceptionsList(mNounExceptionsMap, params.get(PARAM_NOUN_EXCEPTIONS_FILE));
		readExceptionsList(mAdjExceptionsMap, params.get(PARAM_ADJ_EXCEPTIONS_FILE));
	}
	
	private void readExceptionsList(Map<String, Set<String>> exceptionsMap, String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("")) {
				String[] split = line.split("\\s+");
				Set<String> exceptions = exceptionsMap.get(split[1]);
				if(exceptions == null) {
					exceptionsMap.put(split[1], exceptions = new HashSet<String>());
				}
				exceptions.add(split[0]);
			}
		}
		reader.close();
	}
	
	@Override
	protected Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> modelBEParts) {
		String inputText = text;
		Set<String> newStrings = null;
		if(VERB_LABELS.contains(type)) {
			// Since the exceptions map doesn't carry all versions of the verbs that it does carry
			// we also add the versions that are produced by the normal rules
			text = text.toLowerCase();
			WordNet wni = WordNet.getInstance();
			IndexEntry entry = wni.lookupIndexEntry(POS.VERB, text);
			if(entry != null) {
				text = entry.getLemma();
			}
		
			newStrings = mVerbExceptionsMap.get(text);
			boolean containsIngWord = false;
			boolean containsEdWord = false; 
			boolean containsSWord = false;
			if(newStrings == null) {
				newStrings = new HashSet<String>();
			}
			else {
				newStrings = new HashSet<String>(newStrings);
				for(String s : newStrings) {
					if(s.endsWith("ing")) {
						containsIngWord = true;
					}
					else if(s.endsWith("ed")) {
						containsEdWord = true;
					}
					else if(s.endsWith("s")) {
						containsSWord = true;
					}
				}
			}
			if(text.endsWith("ee")) {
				newStrings.add(text + "s");
				if(!containsIngWord)newStrings.add(text + "ing");
				if(!containsEdWord)newStrings.add(text + "d");
			}
			else {
				int textLength = text.length();
			
				if(!containsSWord && textLength > 1) {
					char lastChar = text.charAt(textLength-1);
					if(lastChar == 'e') {
						newStrings.add(text + "s");
						text = text.substring(0, textLength-1);
					}
					else if(lastChar == 's') {
						newStrings.add(text + "es");
					}
				}
				if(!containsIngWord)newStrings.add(text + "ing");
				if(!containsEdWord)newStrings.add(text + "ed");
			}
		}
		else if(NOUN_LABELS.contains(type)) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, text);
			if(entry != null) {
				text = entry.getLemma();
			}
			newStrings = mNounExceptionsMap.get(text);
			if(newStrings == null) {
				newStrings = new HashSet<String>();
				if(ES_ENDING.reset(text).matches()) {
					newStrings.add(text + "es");
				}
				else if(VOWEL_Y_ENDING.reset(text).matches()) {
					newStrings.add(text.substring(text.length()-1) + "ies");
				}
				else {
					newStrings.add(text + "s");
				}
			}
			else {
				newStrings = new HashSet<String>(newStrings);
			}
		}
		else if(ADJ_LABELS.contains(type)) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.ADJECTIVE, text);
			if(entry != null) {
				text = entry.getLemma();
			}
			newStrings = mAdjExceptionsMap.get(text);
			if(newStrings == null) {
				newStrings = new HashSet<String>();
				if(text.endsWith("e")) {
					newStrings.add(text + "r");
					newStrings.add(text + "st");
				}
				else {
					newStrings.add(text + "er");
					newStrings.add(text + "est");	
				}
			}
			else {
				newStrings = new HashSet<String>(newStrings);
			}
		}
		Map<String, String> result = new HashMap<String, String>();
		if(newStrings != null) {
			//newStrings.retainAll(lemmaSet);
			newStrings.retainAll(modelBEParts);
			newStrings.remove(inputText);
			for(String newString : newStrings) {
				result.put(newString, type);
			}
		}
		return result;
	}
	

	
}