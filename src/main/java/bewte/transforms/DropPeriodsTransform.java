package bewte.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Adds or drops periods
 * U.S.A <-> USA
 * Harry S. Truman -> Harry S Truman
 * US Dept of Energy <-> U.S. Dept of Energy
 */
public class DropPeriodsTransform extends FirstOrLastTransformer {
	
	@Override
	public Map<String, String> getNewStrings(String text, String type, Set<String> lemmaSet, Set<String> allModelStrings) {
		Set<String> newStrings = null;
		if(text.indexOf('.') != -1) {
			String newString = text.replace(".", "");
			if(!newString.equals(text)) {
				newStrings = new HashSet<String>(1);
				newStrings.add(newString);
			}
		}
		else {
			StringBuilder buf = new StringBuilder();
			String[] split = text.split("\\s+");
			for(int i = 0; i < split.length; i++) {
				String s = split[i];
				// Unlikely to need to add periods to something with length 5+
				// 5 is arbitrary
				if(s.length() < 5 && s.toUpperCase().equals(s)) {
					buf.append(s.replaceAll(".", "$0\\."));
				}
				else {
					buf.append(s);
				}
				if(i < split.length-1) {
					buf.append(" ");
				}
			}
			String newString = buf.toString();
			if(!newString.equals(text)) {
				newStrings = new HashSet<String>();
				newStrings.add(newString);
			}
		}
		Map<String, String> result = null;
		if(newStrings != null) {
			result = new HashMap<String, String>(newStrings.size());
			for(String s : newStrings) {
				result.put(s, type);
			}
		}
		return result;
	}
	
}