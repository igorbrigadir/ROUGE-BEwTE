package bewte.transforms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import bewte.BE;

public interface BETransform {
	public void setName(String name);
	public String getName();
	
	/**
	 * Initializes the transform.  Only called once.
	 */
	public void initialize(Map<String, String> params) throws Exception;
	
	/**
	 * Reinitializes the transform. Called once for each peer.  It is called before 'transform' is ever called. 
	 */
	public void reinitialize(List<BE> peerBEs, List<BE> referenceBEs, Set<String> modelLemmaSet) throws Exception;
	
	/**
	 * Transforms the given BE.  Returns a list of transformed versions of the BE. 
	 */
	public List<BE> transform(BE be, Set<String> allModelStrings);
}