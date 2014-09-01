package bewte;

import java.util.List;

import tratz.util.TreebankConstants;


/**
 * Represents a single BE 
 *
 */
public class BE {
	
	public static class BEPart {
		public final String text;
		public final String type;
		public BEPart(String text, String type) {
			this.text = text;
			this.type = type;
		}
	}
	
	private List<BEPart> mParts;
	private int mRule;
	private int mEquivalentId;
	private int mCoeffIndex;
	
	public BE(List<BEPart> parts, int rule, int coeffIndex) {
		mParts = parts;
		mRule = rule;
		mEquivalentId = toEquivString().hashCode();
		mCoeffIndex = coeffIndex;
	}
	
	public int getRule() {
		return mRule;
	}
	
	public int getCoeff() {
		return mCoeffIndex;
	}
	
	public List<BEPart> getParts() {
		return mParts;
	}

	private String toEquivString() {
		StringBuilder buf = new StringBuilder();
		for(BEPart part : mParts) {
			buf.append(part.text).append(BEConstants.BE_SEPARATOR_CHAR);
			int type = 1;
			if(TreebankConstants.VERB_LABELS.contains(part.type)) {
				type = 2;
			}
			else if(TreebankConstants.ADJ_LABELS.contains(part.type)){
				type = 3;
			}
			else if(TreebankConstants.ADV_LABELS.contains(part.type)) {
				type = 4;
			
			}
			buf.append(type).append(BEConstants.BE_SEPARATOR_CHAR);			
		}
		return buf.toString().toLowerCase();
	}
	
	/**
	 * Returns a String representation of the text of this BE
	 */
	public String toTextString() {
		StringBuilder buf = new StringBuilder();
		for(BEPart part : mParts) {
			buf.append(part.text).append(BEConstants.BE_SEPARATOR_CHAR);
		}
		return buf.toString().toLowerCase();
	}
	
	/**
	 * Returns a String representation of the BE
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(BEPart part : mParts) {
			buf.append(part.text).append(BEConstants.BE_SEPARATOR_CHAR).append(part.type).append(BEConstants.BE_SEPARATOR_CHAR);
		}
		return buf.toString();
	}
	
	public int getEquivalentId() {
		return mEquivalentId;
	}
	
	@Override
	public int hashCode() {
		return mEquivalentId;
	}
	
	@Override
	public boolean equals(Object obj) {
		BE be2 = (BE)obj;
		return mEquivalentId == be2.mEquivalentId;
	}
	
}