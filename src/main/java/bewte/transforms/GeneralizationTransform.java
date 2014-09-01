package bewte.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.Pointer;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;
import tratz.runpipe.annotations.LocationAnnotation;
import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;


/**
 * newspaper <-> press
 * tiger <-> carnivore
 */
public class GeneralizationTransform extends FirstOrLastTransformer {
	
	public final static String PARAM_POS = "POS";
	
	private POS mPos;
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		super.initialize(params);
		String posString = params.get(PARAM_POS);
		if(posString.equals("noun")) {
			mPos = POS.NOUN;
		}
		else if(posString.equals("verb")) {
			mPos = POS.VERB;
		}
	}
	
	@Override
	public Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Set<String> strings = null;
		if(type.equals(PersonAnnotation.class.getSimpleName())) {
			type = NOUN_PROPER_SINGULAR;
			text = "person";
		}
		else if(type.equals(OrganizationAnnotation.class.getSimpleName())) {
			type = NOUN_PROPER_SINGULAR;
			text = "organization";
		}
		else if(type.equals(LocationAnnotation.class.getSimpleName())) {
			type = NOUN_PROPER_SINGULAR;
			text = "location";
		}
		if((mPos == POS.NOUN && NOUN_LABELS.contains(type))||
				(mPos == POS.VERB && VERB_LABELS.contains(type))) {
			strings = collectStrings(text.toLowerCase(), mPos, lemmaSet);
		}
		Map<String, String> mapping = new HashMap<String, String>();
		if(strings != null) {
			for(String s : strings) {
				mapping.put(s, type);
			}
		}
		return mapping;
	}
	
	public Set<String> collectStrings(String text, POS pos, Set<String> lemmaSet) {
		Set<String> strings = new HashSet<String>();
		IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, text);
		if(entry != null) {
			addLinkedSenses(entry.getSenses()[0], strings, lemmaSet, PointerType.HYPERNYM,0);
			addLinkedSenses(entry.getSenses()[0], strings, lemmaSet, PointerType.HYPONYM,0);
		}
		return strings;
	}
	
	public void addLinkedSenses(Sense sense, Set<String> strings, Set<String> lemmaSet, PointerType type, int level) {
		if(level < 1) {
			for(Pointer p : sense.getSemPointers(type)) {
				Sense target = p.getTargetSense();
				for(Key key : target.getKeys()) {
					String lemma = key.getLemma().toLowerCase();
					if(lemmaSet.contains(lemma)) {
						strings.add(lemma);
					}
			
				}			
				addLinkedSenses(target, strings, lemmaSet, type, level+1);
			}
		}
	}
	
}