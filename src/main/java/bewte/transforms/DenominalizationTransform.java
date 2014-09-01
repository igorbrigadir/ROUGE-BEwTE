package bewte.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;

import bewte.BE;

/**
 * rejection of John -> {John+reject, reject+John} 
 */
public class DenominalizationTransform extends AbstractBETransform {
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> bes = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() == 3) {
			BE.BEPart part1 = parts.get(0);
			BE.BEPart part2 = parts.get(1);
			BE.BEPart part3 = parts.get(2);
			
			if((PREPOSITION.equals(part2.type) || TO.equals(part2.type)) && NOUN_LABELS.contains(part1.type) ) {
				IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, part1.text);
				if(entry != null) {
					bes = new ArrayList<BE>();
					Sense[] senses = entry.getSenses();
					for(int i = 0; i < 1; i++) {
						Sense sense = senses[i];
						int srcIndex = sense.getKeyIndex(entry.getLemma());
						LexPointer[] ptrs = sense.getLexPointers(PointerType.DERIVED_FORM)[srcIndex];
						for(LexPointer p : ptrs) {
							Sense target = p.getTargetSense();
							Key word = target.getKeys()[p.getTargetWordIndex()];
							String lemma = word.getLemma();
							List<BE.BEPart> newParts1 = new ArrayList<BE.BEPart>(2);
							newParts1.add(part3);
							newParts1.add(new BE.BEPart(lemma, VERB_BASE_FORM));
							bes.add(new BE(newParts1, be.getRule(), be.getCoeff()));

							List<BE.BEPart> newParts2 = new ArrayList<BE.BEPart>(2);
							newParts2.add(new BE.BEPart(lemma, VERB_BASE_FORM));
							newParts2.add(part3);
							bes.add(new BE(newParts2, be.getRule(), be.getCoeff()));
						}
					}
				}
			}
		}
		return bes;
	}
	
	
}