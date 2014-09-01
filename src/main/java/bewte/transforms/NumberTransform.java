package bewte.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import foal.list.DoubleArrayList;
import foal.map.DoubleIntHashMap;

import bewte.BE;

/**
 * An attempt at transforming similar numbers into each other.
 */
public class NumberTransform extends FirstOrLastTransformer {

	private DoubleArrayList mDoubleList;
	
	@Override
	public void reinitialize(List<BE> bes, List<BE> bes2, Set<String> modelLemmaSet) throws Exception {
		super.reinitialize(bes, bes2, modelLemmaSet);
		DoubleIntHashMap mDoubleSet = new DoubleIntHashMap();
		for(List<BE> beList : new List[] {bes, bes2}) {
			for(BE be : beList) {
				for(BE.BEPart part : be.getParts()) {
					String text = part.text;
					text = text.replace("$", "");
					text = text.replace("%", "");
					try {
						double d = Double.parseDouble(text);
						mDoubleSet.put(d, 1);
					}
					catch(NumberFormatException nfe) {
						// do nothing
					}
				}
			}
		}
		mDoubleList = mDoubleSet.keys();
	}
	
	@Override
	protected Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Set<String> strings = null;
		text = text.replace("$", "").replace("%", "");
		try {
			double d = Double.parseDouble(text);
			strings = new HashSet<String>();
			int numDoubles = mDoubleList.size();
			for(int i = 0; i < numDoubles; i++) {
				double other = mDoubleList.get(i);
				// arbitrary cutoffs, probably needs some refinement
				if(d < 10) {
					if(Math.abs(d-other) < .5) {
						String dString = Double.toString(other);
						strings.add(dString);
						strings.add("$" + dString);
						strings.add(dString + "%");
					}
				}
				else if(d < 20) {
					if(Math.abs(d-other) < 1) {
						String dString = Double.toString(other);
						strings.add(dString);
						strings.add("$" + dString);
						strings.add(dString + "%");
					}
				}
				else if(d < 100) {
					if(other < 1.05 * d && other > .95 * d) {
						String dString = Double.toString(other);
						strings.add(dString);
						strings.add("$" + dString);
						strings.add(dString + "%");
					}
				}
				else {
					if(other < 1.15 * d && other > .85 * d) {
						String dString = Double.toString(other);
						strings.add(dString);
						strings.add("$" + dString);
						strings.add(dString + "%");
					}
				}
			}
			strings.retainAll(lemmaSet);
		}
		catch(NumberFormatException nfe) {
			// do nothing
		}
		Map<String, String> results = null;
		if(strings != null) {
			results = new HashMap<String, String>();
			for(String s : strings) {
				results.put(s, type);
			}
		}
		return results;
	}
	
	
	
}