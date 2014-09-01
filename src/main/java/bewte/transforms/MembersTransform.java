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
 * China <-> Chinese
 */
public class MembersTransform extends FirstOrLastTransformer {
	
	public final static String PARAM_CAP = "CAPITALIZED";
	
	final static Map<POS, String> POS_TO_STRING = new HashMap<POS, String>();
	static {
		POS_TO_STRING.put(POS.NOUN, NOUN_SINGULAR);
		POS_TO_STRING.put(POS.ADJECTIVE, ADJECTIVE);
		POS_TO_STRING.put(POS.VERB, VERB_BASE_FORM);
		POS_TO_STRING.put(POS.ADVERB, ADVERB);
	}
	
	private boolean mCapitalized;
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		super.initialize(params);
		mCapitalized = new Boolean(params.get(PARAM_CAP));
	}
	
	@Override
	public Map<String, String> getNewStrings(String bePartText, String type, Set<String> lemmas, Set<String> allModelStrings) {
		Map<String, String> result = new HashMap<String, String>();
		POS pos = null;
		POS[] targetPoses = null;
		PointerType[] pTypes = null;
		if(TreebankConstants.NOUN_LABELS.contains(type) || LocationAnnotation.class.getSimpleName().equals(type)) {
			pos = POS.NOUN;
			targetPoses = new POS[]{POS.NOUN, POS.NOUN};
			pTypes = new PointerType[]{PointerType.MEMBER_MERONYM, PointerType.MEMBER_HOLONYM};
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
								String lemma = w.getLemma();
								boolean notCap = lemma.toLowerCase().equals(lemma);
								if((!mCapitalized && notCap) || (mCapitalized && !notCap)) {
									result.put(w.getLemma(), POS_TO_STRING.get(targetPos));
								}
							}
						}
					}
					int srcIndex = sense.getKeyIndex(entry.getLemma());
					LexPointer[] ptrs = sense.getLexPointers(pType)[srcIndex];
					for(LexPointer p : ptrs) {
						Sense target = p.getTargetSense();
						if(target.getPOS().equals(targetPos)) {
							String lemma = target.getKeys()[p.getTargetWordIndex()].getLemma();
							boolean notCap = lemma.toLowerCase().equals(lemma);
							if((!mCapitalized && notCap) || (mCapitalized && !notCap)) {
								result.put(lemma, POS_TO_STRING.get(targetPos));
							}
						}
					}
				}
			}
		}
		return result;
	}

}