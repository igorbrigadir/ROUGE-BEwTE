package bewte.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tratz.util.TreebankConstants;

import bewte.BE;

/**
 * Not currently used, it seems unlikely this transform should have a high weight
 * as it is likely to introduce more erroroneous matches than good matches.
 * Swaps around BEs that have a verb part.. potentially could correct
 * active/passive errors
 */
public class VerbNounSwapTransform extends AbstractBETransform {
	
	public List<BE> transform(BE be, Set<String> modelStrings) {
		List<BE> results = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() > 1) {
			String firstPartLabel = parts.get(0).type;
			String secondPartLabel = parts.get(parts.size()-1).type;
			boolean firstPartVerb = TreebankConstants.VERB_LABELS.contains(firstPartLabel)  && !firstPartLabel.equals(TreebankConstants.AUXILLARY);
			boolean secondPartVerb = TreebankConstants.VERB_LABELS.contains(secondPartLabel) && !secondPartLabel.equals(TreebankConstants.AUXILLARY);
			if(firstPartVerb || secondPartVerb) {
				List<BE.BEPart> copy = new ArrayList<BE.BEPart>(parts);
				copy.set(0, parts.get(parts.size()-1));
				copy.set(parts.size()-1, parts.get(0));
				results = new ArrayList<BE>(1);
				results.add(new BE(copy, be.getRule(), be.getCoeff()));
			}
		}
		return results;
	}
	
}