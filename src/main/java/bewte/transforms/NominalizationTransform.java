package bewte.transforms;

import java.util.ArrayList;
import java.util.LinkedList;
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
 * Gerbils hibernate -> hibernation of gerbils 
 * ... rejected John -> rejection of John
*/
public class NominalizationTransform extends AbstractBETransform {

	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> bes = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() == 2) {
			BE.BEPart part1 = parts.get(0);
			BE.BEPart part2 = parts.get(1);
			BE.BEPart verbPart = null;
			BE.BEPart argPart = null;
			if(VERB_LABELS.contains(part1.type)) {
				verbPart = part1;
				argPart = part2;
			}
			else if(VERB_LABELS.contains(part2.type)) {
				verbPart = part2;
				argPart = part1;
			}
			if(verbPart != null) {
				IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.VERB, verbPart.text);
				if(entry != null) {
					bes = new LinkedList<BE>();
					Sense sense = entry.getSenses()[0];
					int srcIndex = sense.getKeyIndex(entry.getLemma());
					LexPointer[] ptr = sense.getLexPointers(PointerType.DERIVED_FORM)[srcIndex];
					for(LexPointer p : ptr) {
						Sense target = p.getTargetSense();
						Key[] words = target.getKeys();
						Key word = words[p.getTargetWordIndex()];
						List<BE.BEPart> newParts = new ArrayList<BE.BEPart>();
						newParts.add(new BE.BEPart(word.getLemma(),NOUN_SINGULAR));
						newParts.add(new BE.BEPart("of", PREPOSITION));
						newParts.add(argPart);
						bes.add(new BE(newParts, be.getRule(), be.getCoeff()));
					}						
				}
			}						
		}
		return bes;
	}
	
	
}