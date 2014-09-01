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
import tratz.jwni.Sense.Key;



import bewte.BE;

/**
 * New transform.
 * Not currently used. Opposite of AdverbVerbToAdjNounTransform
 * quick+at+creating -> create+quickly
 * quick+creation -> create+quickly
 * quick+at+creation -> create+quickly
 */
public class AdjpToAdverbVerbTransform extends AbstractBETransform {

	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> newBEs = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() > 1) {
			BE.BEPart adjPart = parts.get(0);
			if(ADJ_LABELS.contains(adjPart.type)) {
				// ADJ IN VERB (quick+at+creating -> create+quickly)
				if (parts.size() == 3 && VERB_LABELS.contains(parts.get(2).type)) {
					newBEs = new ArrayList<BE>();			
					BE.BEPart verbPart = parts.get(2);
					convert(newBEs, adjPart, verbPart, be.getRule(), be.getCoeff());
				} 
				else {
					newBEs = new ArrayList<BE>();
					BE.BEPart nounPart = null;
					// 	ADJ+NOMINALIZATION (quick+creation -> create+quickly)
					if (parts.size() == 2) {
						nounPart = parts.get(1);	
					}
					// ADJ+IN+NOMINALIZATION (quick+at+creation -> create+quickly)
					else if(parts.size() == 3) {
						nounPart = parts.get(2);
					}
					if(NOUN_LABELS.contains(nounPart.type)) {
						Set<String> verbSet = new HashSet<String>();
						IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, nounPart.text);
						if(entry != null) {
							Sense sense = entry.getSenses()[0];
							int srcIndex = sense.getKeyIndex(entry.getLemma());
							if(srcIndex == -1){
								System.err.println("Entry: " + entry.getLemma());
								for(Key key : sense.getKeys()) {
									System.err.println("Key: " + key.getLemma());
								}
							}
							LexPointer[] ptrs = sense.getLexPointers(PointerType.DERIVED_FORM)[srcIndex];
							for(LexPointer p : ptrs) {
								Sense target = p.getTargetSense();
								verbSet.add(target.getKeys()[p.getTargetWordIndex()].getLemma().toLowerCase());
							}
						}
						for(String verb : verbSet) {
							convert(newBEs, adjPart, new BE.BEPart(verb, VERB_BASE_FORM), be.getRule(), be.getCoeff());
						}
					}
				}
			}
		}
		return newBEs;
	}
	
	private void convert(List<BE> newBes, BE.BEPart adjPart, BE.BEPart verbPart, int beRule, int beCoeff) {
		IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.ADJECTIVE, adjPart.text);
		if (entry != null) {
			Set<String> adjWords = new HashSet<String>();
			adjWords.add(entry.getLemma());
			// WordNet has no facility for finding Adverbs from the Adjectives they are related to
			// (the reverse links are available however)
			Set<String> advWords = createLikelyAdverbs(adjWords);

			for (String possibleAdvString : advWords) {
				IndexEntry advEntry = WordNet.getInstance().getIndexEntry(POS.ADVERB, possibleAdvString);
				if (advEntry != null) {
					List<BE.BEPart> newParts = new ArrayList<BE.BEPart>();
					newParts.add(verbPart);
					newParts.add(new BE.BEPart(advEntry.getLemma(), ADVERB));
					newBes.add(new BE(newParts, beRule,	beCoeff));
				}
			}
		}
	}
	
	/**
	 * Given the input set of words, creates a set of likely possible adverbs
	 * This is done because there are no links in WordNet from adjectives to
	 * their derived_form adverbs 
	 */
	private Set<String> createLikelyAdverbs(Set<String> adjWords) {
		Set<String> advWords = new HashSet<String>();
		for (String adj : adjWords) {
			// traditional -> traditionally
			advWords.add(adj + "ly");
			if (adj.length() > 1) {
				// true -> truly
				if (adj.endsWith("e")) {
					String substring = adj.substring(0, adj.length() - 1);
					advWords.add(substring + "ly");
					advWords.add(substring + "y");
				} 
				else if (adj.endsWith("l")) {
					advWords.add(adj + "y");
				} 
				else if (adj.endsWith("y")) {
						advWords.add(adj.substring(0,
									adj.length() - 1)
									+ "ily");
				}
			}
		}
		return advWords;
	}

	
}