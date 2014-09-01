package bewte.annotations;

import java.util.HashMap;
import java.util.Map;

import tratz.runpipe.TextDocument;

public class CDAnnotation extends CanonicalizingAnnotation {
	
	public static final long serialVersionUID = 1;
	
	final static Map<String, Long> STRING_TO_LONG = new HashMap<String, Long>();
	static {
		STRING_TO_LONG.put("million", new Long(1000000));
		STRING_TO_LONG.put("billion", new Long(1000000000));
		STRING_TO_LONG.put("thousand", new Long(1000));
		STRING_TO_LONG.put("hundred", new Long(100));
		STRING_TO_LONG.put("ninety", new Long(90));
		STRING_TO_LONG.put("eighty", new Long(80));
		STRING_TO_LONG.put("seventy", new Long(70));
		STRING_TO_LONG.put("sixty", new Long(60));
		STRING_TO_LONG.put("fifty", new Long(50));
		STRING_TO_LONG.put("forty", new Long(40));
		STRING_TO_LONG.put("thirty", new Long(30));
		STRING_TO_LONG.put("twenty", new Long(20));
		STRING_TO_LONG.put("ten", new Long(10));
		STRING_TO_LONG.put("eleven", new Long(10));
		STRING_TO_LONG.put("twelve", new Long(10));
		STRING_TO_LONG.put("thirteen", new Long(10));
		STRING_TO_LONG.put("fourteen", new Long(10));
		STRING_TO_LONG.put("fifteen", new Long(10));
		STRING_TO_LONG.put("sixteen", new Long(10));
		STRING_TO_LONG.put("seventeen", new Long(10));
		STRING_TO_LONG.put("eighteen", new Long(10));
		STRING_TO_LONG.put("nineteen", new Long(10));
		STRING_TO_LONG.put("dozen", new Long(12));
		STRING_TO_LONG.put("one", new Long(1));
		STRING_TO_LONG.put("two", new Long(2));
		STRING_TO_LONG.put("three", new Long(3));
		STRING_TO_LONG.put("four", new Long(4));
		STRING_TO_LONG.put("five", new Long(5));
		STRING_TO_LONG.put("six", new Long(6));
		STRING_TO_LONG.put("seven", new Long(7));
		STRING_TO_LONG.put("eight", new Long(8));
		STRING_TO_LONG.put("nine", new Long(9));
		
	}
	
	public CDAnnotation(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
	
	public String getCanonicalString() {
		String canonicalString = getAnnotText().trim().toLowerCase();
		canonicalString = canonicalString.replaceAll(",", "");
		boolean startsWithDollarSign = canonicalString.startsWith("$");
		if(!canonicalString.endsWith("%")) {
		if(canonicalString.length() > 1) {
			
			if(startsWithDollarSign) {
				canonicalString = canonicalString.substring(1);
			}
			if(canonicalString.endsWith("m")) {
				canonicalString = canonicalString.replace("m", " million");
			}
			else if(canonicalString.endsWith("k")) {
				canonicalString = canonicalString.replace("k", " thousand");
			}
			else if(canonicalString.endsWith("b")) {
				canonicalString = canonicalString.replace("b", " billion");
			}
			else {
				canonicalString = canonicalString.replaceAll("([0-9])((tr|b|m)illion)", "$1 $2");
			}
			String[] split = canonicalString.split("\\s+");
			if(split.length == 2) {
				Long part1 = null;
				double value = -1;
				boolean v1Extracted = false;
				if(!split[0].matches("[0-9\\.]+")) {
					part1 = STRING_TO_LONG.get(split[0]);
					if(part1 != null) {
						value = part1;
						v1Extracted = true;
					}
				}
				else {
					value = Double.parseDouble(split[0]);
					v1Extracted = true;
				}
				Long part2 = STRING_TO_LONG.get(split[1]);
				if(v1Extracted && part2 != null) {
					canonicalString = Long.toString(Math.round(value * part2));
				}
			}
			else if(split.length == 1) {
				if(!canonicalString.matches("[0-9\\.]+")) {
					Long part1 = STRING_TO_LONG.get(split[0]);
					if(part1 != null) {
						canonicalString = part1.toString();
					}
				}
			}
		}
		}
		return (startsWithDollarSign? "$" + canonicalString : canonicalString);
	}
	
	public String getCanonicalType() {
		return CDAnnotation.class.getSimpleName();
	}
	
}