package bewte.transforms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;

/**
 * 
 * was -> be
 * jumps -> jump
 * dogs -> dog
 */
public class LemmaTransform extends FirstOrLastTransformer {

	@Override
	public Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Map<String, String> results = new HashMap<String, String>();
		POS pos = null;
		if (NOUN_LABELS.contains(type)) {
			pos = POS.NOUN;
		} 
		else if (VERB_LABELS.contains(type)) {
			pos = POS.VERB;
		}
		else if(ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
		}
		if (pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, text);//.getMorpho().lookupBaseEntry(pos, text);
			if (entry != null) {
				String lemma = entry.getLemma();
				if (!lemma.equals(text)) {
					results.put(lemma, type);
				}
			}
		}
		return results;
	}	

}