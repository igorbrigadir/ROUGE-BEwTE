package bewte.transforms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.util.TreebankConstants;

import bewte.BE;

/**
 * Abstract BE Transformation class
 */
abstract public class AbstractBETransform implements BETransform, TreebankConstants {
	
	private String mName;
	
	public AbstractBETransform() {
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void initialize(Map<String, String> params) throws Exception {		
	}
	
	public void reinitialize(List<BE> peerBEs, List<BE> referenceBEs, Set<String> modelLemmaSet) throws Exception {

	}
	
	abstract public List<BE> transform(BE be, Set<String> allModelStrings);
	
		
}