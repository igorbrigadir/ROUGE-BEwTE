package bewte.transforms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;

import bewte.BE;

/**
 * New transform
 * Not currently used. Opposite of AdjpToAdverbVerbTransform
 * create+quickly -> quick+creation 
 * create+quickly -> quick+in+create
 */
public class AdverbVerbToAdjNounTransform extends AbstractBETransform {
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> bes = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() == 2) {
			BE.BEPart part1 = parts.get(0);
			BE.BEPart part2 = parts.get(1);
			if(VERB_LABELS.contains(part1.type) && ADV_LABELS.contains(part2.type)) {
				bes = convert(be, part1, part2, POS.VERB, POS.ADVERB, PointerType.DERIVED_FORM, PointerType.PERTAINYM);
			}
		}
		return bes;
	}
	
	private List<BE> convert(BE be, 
							 BE.BEPart verbPart, 
							 BE.BEPart advPart, 
							 POS pos1, 
							 POS pos2, 
							 PointerType pType1, 
							 PointerType pType2) {
		List<BE> bes = null;
		IndexEntry advEntry = WordNet.getInstance().lookupIndexEntry(POS.ADVERB, advPart.text);
		if(advEntry != null) {
			IndexEntry verbEntry = WordNet.getInstance().lookupIndexEntry(POS.VERB, verbPart.text);
			if(verbEntry != null) {
				Sense[] advSenses = advEntry.getSenses();
				Set<String> adjs = new HashSet<String>();
				for(int i = 0; i < 1; i++) {
					Sense advSense = advSenses[i];
					int advIndex = advSense.getKeyIndex(advEntry.getLemma());
					LexPointer[] ptrs = advSense.getLexPointers(pType2)[advIndex];
					for(LexPointer p : ptrs) {
						Sense target = p.getTargetSense();
						adjs.add(target.getKeys()[p.getTargetWordIndex()].getLemma().toLowerCase());
					}
				}
				if(adjs.size() > 0) {
					bes = new ArrayList<BE>();
					for(String adj : adjs) {
						List<BE.BEPart> newParts = new ArrayList<BE.BEPart>(2);
						newParts.add(new BE.BEPart(adj, ADJECTIVE));
						newParts.add(new BE.BEPart("in", PREPOSITION));
						newParts.add(new BE.BEPart(verbPart.text, verbPart.type));
						bes.add(new BE(newParts, be.getRule(), be.getCoeff()));
					}
					Set<String> noms = new HashSet<String>();
					Sense verbSense = verbEntry.getSenses()[0];
					int srcIndex = verbSense.getKeyIndex(verbEntry.getLemma());
					LexPointer[] pointers = verbSense.getLexPointers(pType1)[srcIndex];
					for(LexPointer p : pointers) {
						Sense target = p.getTargetSense();
						if(target.getPOS() == POS.NOUN) {
							noms.add(target.getKeys()[p.getTargetWordIndex()].getLemma().toLowerCase());
						}
					}
					if(noms.size() > 0) {
						for(String nom : noms) {
							for(String adj : adjs) {
								List<BE.BEPart> newParts = new ArrayList<BE.BEPart>(2);
								newParts.add(new BE.BEPart(adj, ADJECTIVE));
								newParts.add(new BE.BEPart(nom, NOUN_SINGULAR));
								bes.add(new BE(newParts, be.getRule(), be.getCoeff()));
								
								List<BE.BEPart> newParts2 = new ArrayList<BE.BEPart>(2);
								newParts2.add(new BE.BEPart(adj, ADJECTIVE));
								newParts2.add(new BE.BEPart("in", PREPOSITION));
								newParts2.add(new BE.BEPart(nom, NOUN_SINGULAR));
								bes.add(new BE(newParts2, be.getRule(), be.getCoeff()));
							}
						}
					}
				}
			}
		}
		return bes;
	}
	
}