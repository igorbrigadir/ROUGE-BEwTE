package bewte.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;
import tratz.util.TreebankConstants;


/**
 * Expands among synonyms.
 * demolish -> pulverize
 * 
 */
public class SynonymTransform extends FirstOrLastTransformer {
	
	public final static String PARAM_POS = "POS";
	public final static String PARAM_MIN_SENSE = "MIN_SENSE";
	public final static String PARAM_MAX_SENSE = "MAX_SENSE";
	
	private POS mPOS;
	private Set<String> mTreebankStrings;
	private int mMinSense;
	private int mMaxSense;
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		super.initialize(params);
		String posString = params.get(PARAM_POS);
		if(posString.equals("noun")) {
			mPOS = POS.NOUN;
			mTreebankStrings = TreebankConstants.NOUN_LABELS;
		}
		else if(posString.equals("verb")) {
			mPOS = POS.VERB;
			mTreebankStrings = TreebankConstants.VERB_LABELS;
		}
		else if(posString.equals("adj")) {
			mPOS = POS.ADJECTIVE;
			mTreebankStrings = TreebankConstants.ADJ_LABELS;
		}
		mMinSense = Integer.parseInt(params.get(PARAM_MIN_SENSE));
		mMaxSense = Integer.parseInt(params.get(PARAM_MAX_SENSE));
	}
	
	@Override
	protected Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Set<String> strings = null;
		if(mTreebankStrings.contains(type)) {
			strings = new HashSet<String>();
			addSynonyms(strings, mPOS, text, lemmaSet);
			strings.remove(text);
		}
		Map<String, String> result = null;
		if(strings != null) {
			result = new HashMap<String, String>();
			for(String s : strings) {
				result.put(s, type);
			}
		}
		return result;
	}
	
	private void addSynonyms(Set<String> strings, POS pos, String text, Set<String> validSet) {
		IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, text);
		if(entry != null) {
			String lemma = entry.getLemma();
			Sense[] senses = entry.getSenses();
			if(senses != null) {
				int numSenses = senses.length;
				for(int i = mMinSense-1; i < Math.min(numSenses, mMaxSense); i++) {
					Sense sense = senses[i];
					Key[] words = sense.getKeys();
					if(words != null) {
						for(Key word : words) {
							String wordLemma = word.getLemma();
							if(!lemma.equals(wordLemma) && validSet.contains(wordLemma)) {
								strings.add(wordLemma);
							}
						}
					}
				}
			}
		}
	}
	
}