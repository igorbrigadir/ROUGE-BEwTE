package bewte.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;
import tratz.util.TreebankConstants;


import bewte.BE;

/**
 * John+made <-> John+maker 
 *
 */
public class RoleTransform extends AbstractBETransform {
	
	public final static String PARAM_IS_RULES = "IS_RULES";
	public final static String PARAM_DO_RULES = "DO_RULES";
	
	private Set<Integer> mIsRules = new HashSet<Integer>();
	private Set<Integer> mDoRules = new HashSet<Integer>();
	
	@Override
	public void initialize(Map<String, String> params) {
		String[] isRuleStrings = params.get(PARAM_IS_RULES).split(";");
		for(String isRule : isRuleStrings) {
			mIsRules.add(Integer.parseInt(isRule));
		}
		String[] doRuleStrings = params.get(PARAM_DO_RULES).split(";");
		for(String doRule : doRuleStrings) {
			mDoRules.add(Integer.parseInt(doRule));
		}
	}
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> bes = null;
		if(mIsRules.contains(be.getRule())) {
			
			List<BE.BEPart> parts = be.getParts();
			if(parts.size() > 1) {
			BE.BEPart p1 = parts.get(0);
		
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, p1.text);
			if(entry != null) {
				bes = new ArrayList<BE>();
				Set<String> words = new HashSet<String>();
				Sense sense = entry.getSenses()[0];
				int srcIndex = sense.getKeyIndex(entry.getLemma());
				LexPointer[] ptrs = sense.getLexPointers(PointerType.DERIVED_FORM)[srcIndex];
				for(LexPointer p : ptrs) {
					Sense target = p.getTargetSense();
					if(target.getPOS().equals(POS.VERB)) {
						words.add(target.getKeys()[p.getTargetWordIndex()].getLemma().toLowerCase());
					}
				}
				for(String word : words) {
					bes.add(new BE(Arrays.asList(parts.get(1), new BE.BEPart(word, TreebankConstants.VERB_BASE_FORM)), be.getRule(), be.getCoeff()));
				}
			}
			}
		}
		// John+wrote -> writer+John
		else if(mDoRules.contains(be.getRule())) {
			List<BE.BEPart> parts = be.getParts();
			if(parts.size() == 2) {
			BE.BEPart doerPart = parts.get(0);
			BE.BEPart verbPart = parts.get(1);
			
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.VERB, verbPart.text);
			
			if(entry != null) {
				bes = new ArrayList<BE>();
				Set<String> words = new HashSet<String>();
				Sense sense = entry.getSenses()[0];
				int srcIndex = sense.getKeyIndex(entry.getLemma());
				LexPointer[] ptr = sense.getLexPointers(PointerType.DERIVED_FORM)[srcIndex];
				for(LexPointer p : ptr) {
					Sense target = p.getTargetSense();
					if(target.getPOS() == POS.NOUN) {
						words.add(target.getKeys()[p.getTargetWordIndex()].getLemma().toLowerCase());
					}
				}
				// Any derived_form word that is lexically identical to the original verb is almost 
				// definitely not a 'role' form of the verb.
				// May want to include additional restrictions here (e.g. must end in 'or','er','ist','yst',etc.)
				// or perhaps must be a hyponym of 'causal_agent'
				for(Key w : sense.getKeys()) {
					words.remove(w.getLemma());
				}
				for(String word : words) {
						bes.add(new BE(Arrays.asList(new BE.BEPart(word, TreebankConstants.NOUN_SINGULAR), doerPart), be.getRule(), be.getCoeff()));
				}
			}
			}
		}
		return bes;
	}
	
}