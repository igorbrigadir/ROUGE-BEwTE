package bewte.transforms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;
import tratz.util.TreebankConstants;

import bewte.BE;

/**
 *	he/she/etc<->"John Smith"
 *  they/them<->NNS/NNPS  // may make sense to split this part into a separate transform
 */
public class PronounTransform extends FirstOrLastTransformer{
	
	private static Set<String> PERSONAL_PRONOUNS = new HashSet<String>(Arrays.asList("he","she","him","her"));
	private static Set<String> POSSESSIVE_PERSONAL_PRONOUNS = new HashSet<String>(Arrays.asList("his","her"));
	private static Set<String> GROUP_PRONOUNS = new HashSet<String>(Arrays.asList("they", "them"));
	
	private Set<String> mPersonStrings = new HashSet<String>();
	private Set<String> mOrganizationStrings = new HashSet<String>();
	private Set<String> mPluralStrings = new HashSet<String>();
	
	@Override
	public void reinitialize(List<BE> bes, List<BE> bes2, Set<String> modelLemmaSet) throws Exception {
		super.reinitialize(bes, bes2, modelLemmaSet);
		mPersonStrings.clear();
		mOrganizationStrings.clear();
		mPluralStrings.clear();
		String personAnnotationType = PersonAnnotation.class.getSimpleName();
		String organizationAnnotationType = OrganizationAnnotation.class.getSimpleName();
		for(List<BE> beList : new List[]{bes/*, bes2*/}) {
			for(BE be : beList) {
				for(BE.BEPart part : be.getParts()) {
					if(part.type.equals(personAnnotationType)) {
						mPersonStrings.add(part.text.toLowerCase());
					}
					else if(part.type.equals(organizationAnnotationType)) {
						mOrganizationStrings.add(part.text.toLowerCase());
					}
					else if(part.type.equals(TreebankConstants.NOUN_PLURAL) || part.type.equals(TreebankConstants.NOUN_PROPER_PLURAL)) {
						mPluralStrings.add(part.text.toLowerCase());
					}
				}
			}
		}
	}
	
	@Override
	public Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> modelStrings) {
		Set<String> newStrings = null;
		if(mPersonStrings.contains(text)) {
			newStrings = new HashSet<String>(PERSONAL_PRONOUNS);
			newStrings.addAll(POSSESSIVE_PERSONAL_PRONOUNS);
			newStrings.retainAll(lemmaSet);
		}
		else if(mOrganizationStrings.contains(text)) {
			newStrings = new HashSet<String>(GROUP_PRONOUNS);
			newStrings.retainAll(lemmaSet);
		}
		else if(mPluralStrings.contains(text)) {
			newStrings = new HashSet<String>(GROUP_PRONOUNS);
		}
		else if(PERSONAL_PRONOUNS.contains(text.toLowerCase()) || POSSESSIVE_PERSONAL_PRONOUNS.contains(text.toLowerCase())) {
			newStrings = new HashSet<String>(mPersonStrings);
		}
		else if(GROUP_PRONOUNS.contains(text.toLowerCase())) {
			newStrings = new HashSet<String>(mOrganizationStrings);
			newStrings.addAll(mPluralStrings);
		}
		Map<String, String> results = null;
		if(newStrings != null) {
			results = new HashMap<String, String>();
			for(String s : newStrings) {
				results.put(s, type);
			}
		}
		return results;
	}
	
}