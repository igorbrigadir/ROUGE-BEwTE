package bewte.transforms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import tratz.jwni.IndexEntry;
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.Pointer;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;
import tratz.runpipe.annotations.LocationAnnotation;
import tratz.util.TreebankConstants;

/**
 * 
 * biological -> biology (derived form.. here I'm calling it "pertainym")
 * China -> Chinese (meronym member_of)
 * Chinese -> China (holonym member_of)
 * 
 * probably, this should support biology -> biological as well
 */
public class PertainymsTransform extends FirstOrLastTransformer {
	
	final static Map<POS, String> POS_TO_STRING = new HashMap<POS, String>();
	static {
		POS_TO_STRING.put(POS.NOUN, NOUN_SINGULAR);
		POS_TO_STRING.put(POS.ADJECTIVE, ADJECTIVE);
		POS_TO_STRING.put(POS.VERB, VERB_BASE_FORM);
		POS_TO_STRING.put(POS.ADVERB, ADVERB);
	}
	
	@Override
	public Map<String, String> getNewStrings(String bePartText, String type, Set<String> lemmas, Set<String> allModelStrings) {
		Map<String, String> result = new HashMap<String, String>();
		POS pos = null;
		POS[] targetPoses = null;
		PointerType[] pTypes = null;
		if(TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
			targetPoses = new POS[]{POS.NOUN,POS.NOUN};
			pTypes = new PointerType[]{PointerType.DERIVED_FORM, PointerType.PERTAINYM};
		}
		else if(TreebankConstants.NOUN_LABELS.contains(type) || LocationAnnotation.class.getSimpleName().equals(type)) {
			pos = POS.NOUN;
			targetPoses = new POS[]{POS.ADJECTIVE};
			pTypes = new PointerType[]{PointerType.DERIVED_FORM};
		}
		
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(pos, bePartText);
			if(entry != null) {
				Sense sense = entry.getSenses()[0];
				for(int i = 0; i < pTypes.length; i++) {
					PointerType pType = pTypes[i];
					POS targetPos = targetPoses[i];
					for(Pointer p : sense.getSemPointers(pType)) {
						Sense target = p.getTargetSense();
						if(target.getPOS().equals(targetPos)) {
							for(Key w : target.getKeys()) {
								result.put(w.getLemma(), POS_TO_STRING.get(targetPos));
							}
						}
					}
					int srcIndex = sense.getKeyIndex(entry.getLemma());
					for(LexPointer p : sense.getLexPointers(pType)[srcIndex]) {
						Sense target = p.getTargetSense();
						if(target.getPOS().equals(targetPos)) {
							result.put(target.getKeys()[p.getTargetWordIndex()].getLemma(), POS_TO_STRING.get(targetPos));
						}
					}
				}
			}
		}
		return result;
	}

}