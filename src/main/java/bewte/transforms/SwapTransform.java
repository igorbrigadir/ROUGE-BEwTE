package bewte.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bewte.BE;

/**
 * IS-A relationship BEs like John+king <-> king+John 
 * (such a BE could come from strings like "John, the king of..." or "John is king")
 */
public class SwapTransform extends AbstractBETransform {
	
	public final static String PARAM_RULES = "RULES";
	
	private Set<Integer> mRules = new HashSet<Integer>();
	
	@Override
	public void initialize(Map<String, String> params) {
		String[] ruleStrings = params.get(PARAM_RULES).split(";");
		for(String rule : ruleStrings) {
			mRules.add(Integer.parseInt(rule));
		}
	}
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE> results = null;
		int rule = be.getRule();
		if(mRules.contains(rule)) {
			List<BE.BEPart> parts = be.getParts();
			if(parts.size() > 1) {
				results = new ArrayList<BE>(1);
				BE.BEPart firstPart = parts.get(0);
				BE.BEPart secondPart = parts.get(parts.size()-1);
				results.add(new BE(Arrays.asList(secondPart, firstPart), be.getRule(), be.getCoeff()));
			}
		}
		return results;
	}
	
}