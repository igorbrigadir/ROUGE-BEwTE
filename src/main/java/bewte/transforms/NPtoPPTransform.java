package bewte.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;

import bewte.BE;

/**
 * science book <-> book of science
 */
public class NPtoPPTransform extends AbstractBETransform {

	public final static String PARAM_RULES = "RULES";
	
	private Set<Integer> mRuleSet = new HashSet<Integer>();
	
	@Override
	public void initialize(Map<String, String> params) {
		String[] rules = params.get(PARAM_RULES).split(";");
		for(String rule : rules) {
			mRuleSet.add(Integer.parseInt(rule));
		}
	}
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> bes = null;
		if(mRuleSet.contains(be.getRule())){
			List<BE.BEPart> parts = be.getParts();
			BE.BEPart part1 = null;
			BE.BEPart part2 = null;
			if(parts.size() > 1) {
				
			
			if(parts.size() == 2) {
				part1 = parts.get(0);
				part2 = parts.get(1);
			}
			else {
				part1 = parts.get(2);
				part2 = parts.get(0);
			}
			bes = new ArrayList<BE>(1);
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, part2.text);
			if(entry != null) {
				BE newBe = null;
				if(parts.size() == 3) {
					// PP -> NP
					newBe = new BE(Arrays.asList(part1, part2), be.getRule(), be.getCoeff());
				}
				else {
					// NP -> PP
					newBe = new BE(Arrays.asList(part2, new BE.BEPart("of", PREPOSITION), part1), be.getRule(), be.getCoeff());
				}
				bes.add(newBe);				
			}
			}
		}
		return bes;
	}
	
}