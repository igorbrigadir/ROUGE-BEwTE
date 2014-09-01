package bewte.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bewte.BE;

/**
 * 
 * Abstract class for transforms that affect either the first
 * or last BE part.  The PARAM_LEFT_OR_RIGHT determines which part is transformed.
 * The getNewStrings method needs to be implemented.
 */
abstract public class FirstOrLastTransformer extends AbstractBETransform {
	public final static String PARAM_LEFT_OR_RIGHT = "LEFT_OR_RIGHT";
	public final static String LEFT = "left";
	public final static String RIGHT = "right";
	
	private boolean mLeft;
	protected Set<String> mModelSet;
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		String val = params.get(PARAM_LEFT_OR_RIGHT);
		if(val != null && val.equals(LEFT)) {
			mLeft = true;
		}
	}
	
	@Override
	public void reinitialize(List<BE> peerBEs, List<BE> referenceBEs, Set<String> modelLemmaSet) throws Exception {
		mModelSet = modelLemmaSet;
	}
	
	@Override
	public List<BE> transform(BE be, Set<String> allModelStrings) {
		List<BE.BEPart> parts = be.getParts();
		int index;
		if(mLeft) {
			index = 0;
		}
		else {
			index = parts.size()-1;
			if(index == 0) {
				return null;
			}
		}
		List<BE> bes = null;
		BE.BEPart part = parts.get(index);
		Map<String, String> newStrings = getNewStrings(part.text, part.type, mModelSet, allModelStrings);
		if(newStrings != null) {
			bes = new ArrayList<BE>(newStrings.size());
			for(String newString : newStrings.keySet()) {
				List<BE.BEPart> newParts = new ArrayList<BE.BEPart>(parts);
				newParts.set(index, new BE.BEPart(newString, newStrings.get(newString)));
				bes.add(new BE(newParts, be.getRule(), be.getCoeff()));
			}
		}
		return bes;
	}
	
	abstract protected Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings);
	
}