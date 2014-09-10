package bewte.transforms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;

import bewte.BE;

/**
 * 
 * Uses a list of known abbreviations.
 * Also, generates likely abbreviations
 * (e.g. All Bahamas Catering <-> ABC)
 */
public class AcronymTransform extends FirstOrLastTransformer {

	public final static String PARAM_KNOWN_ABBREVIATIONS = "ABBREVIATIONS_FILE";
	
	private Map<String, Set<String>> mMappings = new HashMap<String, Set<String>>();
	private Set<String> mCommonNameEndings = new HashSet<String>(Arrays.asList("jr", "sr", "jr.","sr.", "i", "ii", "iii", "iv", "v"));
	private Set<String> mTitles = new HashSet<String>(Arrays.asList("mr.", "mr", "mrs.", "mrs", "ms.", "ms", "dr.", "dr", "mssr.", "mssr", "fr.", "fr"));
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		super.initialize(params);
		System.out.println(PARAM_KNOWN_ABBREVIATIONS+"="+params.get(PARAM_KNOWN_ABBREVIATIONS));
		BufferedReader reader = new BufferedReader(new InputStreamReader(AcronymTransform.class.getResourceAsStream(params.get(PARAM_KNOWN_ABBREVIATIONS))));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("") && !line.startsWith("#")) {
				line = line.toLowerCase();
				String[] split = line.split("\\t+");
				int numSplit = split.length;
				for(int i = 0; i < numSplit; i++) {
					String s1 = split[i];
					Set<String> abbrevs1 = mMappings.get(s1);
					if(abbrevs1 == null) {
						mMappings.put(s1, abbrevs1 = new HashSet<String>());
					}
					for(int j = 0; j < numSplit; j++) {
						if(i != j) {
							String s2 = split[j];
							abbrevs1.add(s2);
						}
					}
				}
			}
		}
		reader.close();
	}
	
	private void addMapping(String text, String abbrev) {
		text = text.toLowerCase();
		abbrev = abbrev.toLowerCase();
		Set<String> abbreviations = mMappings.get(text);
		if(abbreviations == null) {
			mMappings.put(text, abbreviations = new HashSet<String>());
		}
		abbreviations.add(abbrev);
		
		Set<String> expansions = mMappings.get(abbrev);
		if(expansions == null) {
			mMappings.put(abbrev, expansions = new HashSet<String>());
		}
		expansions.add(text);
	}

	@Override
	public void reinitialize(List<BE> bes, List<BE> bes2, Set<String> modelLemmaSet) throws Exception {
		super.reinitialize(bes, bes2, modelLemmaSet);
		mMappings.clear();
		for(List<BE> beList : new List[]{bes, bes2}) {
			for(BE be : beList) {
				for(BE.BEPart part : be.getParts()) {
					if(PersonAnnotation.class.getSimpleName().equals(part.type)) {
						String[] split = part.text.split("\\s+");
						if(split.length > 2) {
							String textLower = part.text.toLowerCase();
							
							StringBuilder buf = new StringBuilder();
							for(String s : split) {
								String sLower = s.toLowerCase();
								if(mCommonNameEndings.contains(sLower)) {
									buf.append(' ').append(s);
								}
								else if(!mTitles.contains(sLower)) {
									buf.append(s.charAt(0));
								}
							}
							String mapText = buf.toString();
							if(mapText.length() > 1) {
								addMapping(textLower, mapText);
							}
						}
					}
					else if(OrganizationAnnotation.class.getSimpleName().equals(part.type)) {
						String textLower = part.text.toLowerCase();
						String[] split = part.text.split("\\s+");
						if(split.length > 1) {
							StringBuilder buf = new StringBuilder();
							for(String s : split) {
								String sLower = s.toLowerCase();
								char firstChar = s.charAt(0);
								if(firstChar == '&') {
									buf.append("a");
								}
								else if(s.compareToIgnoreCase("and") == 0) {
									buf.append("a");
								}
								else if(s.compareToIgnoreCase("of") == 0) {
									buf.append("o");
								}
								else if(!sLower.equals("the")) {
									buf.append(firstChar);
									if(s.toUpperCase().equals(s)) {
										for(int i = 1; i < s.length(); i++) {
											buf.append(s.charAt(i));
										}
									}
								}
							}
							Set<String> expansions = mMappings.get(textLower);
							if(expansions == null) {
								mMappings.put(textLower, expansions = new HashSet<String>());
							}
							String abbrev = buf.toString();
							if(abbrev.length() > 1) {
								addMapping(textLower, abbrev.replace("a", "").replace("o", ""));
								addMapping(textLower, abbrev.replace("a", "&"));
								addMapping(textLower, abbrev.replace("a", ""));
								addMapping(textLower, abbrev.replace("a", "&").replace("o", ""));
							}
							
						}
					}
				}
			}
		}
	}

	@Override
	protected Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		String lowerText = text.toLowerCase();
		Set<String> newStrings = mMappings.get(lowerText);
		Map<String, String> result = new HashMap<String, String>();
		if(newStrings != null) {
			newStrings.retainAll(allModelStrings);
			for(String s : newStrings) {
				if(allModelStrings.contains(s)) {
					result.put(s, type);
				}
			}
		}
		return result;
	}
	
		
}