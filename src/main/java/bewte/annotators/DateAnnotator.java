package bewte.annotators;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.AnnotatorImpl;

import bewte.annotations.DateAnnotation;

/**
 * Not currently used.  Porbably should be worked on a little more first.
 * Matches dates like January 13th, 1999  Jan 13, Jan 99, Jan 1999, Jan. 13th, etc.
 * Needs to be expanded to support 1-13-99, 1-13-1999, etc.
 */
public class DateAnnotator extends AnnotatorImpl {
	
	
	private final static String MONTH = "(January|February|March|April|May|June|July|August|September|October|November|December)";
	private final static String MONTH_ABBREVIATION = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\.?";
	private final static String MONTH_OR_ABBREV = "(" + MONTH + "|" + MONTH_ABBREVIATION+")";
	private final static String DATE = "(([1-3][0-9]|[1-9])(st|th|nd|rd)?)";
	private final static String YEAR = "(([1-9][0-9]{3})|([0-9]{2}))";
	
	private final static Map<String, String> ABBREV_TO_MONTH = new HashMap<String, String>();
	static {
		ABBREV_TO_MONTH.put("Jan", "January");
		ABBREV_TO_MONTH.put("Jan.", "January");
		ABBREV_TO_MONTH.put("Feb", "February");
		ABBREV_TO_MONTH.put("Feb.", "February");
		ABBREV_TO_MONTH.put("Mar", "March");
		ABBREV_TO_MONTH.put("Mar.", "March");
		ABBREV_TO_MONTH.put("Apr", "April");
		ABBREV_TO_MONTH.put("Apr.", "April");
		ABBREV_TO_MONTH.put("May", "May");
		ABBREV_TO_MONTH.put("May.", "May");
		ABBREV_TO_MONTH.put("Jun", "June");
		ABBREV_TO_MONTH.put("Jun.", "June");
		ABBREV_TO_MONTH.put("Jul", "July");
		ABBREV_TO_MONTH.put("Jul.", "July");
		ABBREV_TO_MONTH.put("Aug", "August");
		ABBREV_TO_MONTH.put("Aug.", "August");
		ABBREV_TO_MONTH.put("Sep", "September");
		ABBREV_TO_MONTH.put("Sep.", "September");
		ABBREV_TO_MONTH.put("Sept", "September");
		ABBREV_TO_MONTH.put("Sept.", "September");
		ABBREV_TO_MONTH.put("Oct", "October");
		ABBREV_TO_MONTH.put("Oct.", "October");
		ABBREV_TO_MONTH.put("Nov", "November");
		ABBREV_TO_MONTH.put("Nov.", "November");
		ABBREV_TO_MONTH.put("Dec", "December");
		ABBREV_TO_MONTH.put("Dec.", "December");
	}
	
	  private static Pattern mDatePattern = Pattern.compile(MONTH_OR_ABBREV + "\\s*" + DATE + "?,?\\s*" + YEAR + "?");
	
	// need to modify to handle dashes like thirty-seven
	@Override
	public void process(TextDocument doc) {
		String docText = doc.getText();
		if(docText != null) {
			Matcher matcher = mDatePattern.matcher(docText);
			while(matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				String matchedText = docText.substring(start, end);
				// Verify that it really is a group
				if((matchedText.indexOf(' ') != matchedText.lastIndexOf(' '))) {
					String year = matcher.group(7);
					if(year != null && year.length() < 4) {
						year = "19" + year;
					}
					String month = matcher.group(1);
					if(ABBREV_TO_MONTH.keySet().contains(month)) {
						month = ABBREV_TO_MONTH.get(month);
					}
					String date = matcher.group(5);
					if(year == null && date != null) {
						try {
							if(Integer.parseInt(date) > 31) {
								year = date;
								date = null;
							}
						}
						catch(NumberFormatException nfe) {
							//ignore
						}
					}
					doc.addAnnotation(new DateAnnotation(doc, start, end, month, date, year));
				}
			}
		}
	}

}