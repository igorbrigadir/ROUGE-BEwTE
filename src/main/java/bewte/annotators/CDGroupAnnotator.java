package bewte.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.AnnotatorImpl;

import bewte.annotations.CDAnnotation;

/**
 * Not currently used.  Groups a number/dollar amount together.
 * Some refinement still needed including the ability to handle dashes like "thirty-five"
 */
public class CDGroupAnnotator extends AnnotatorImpl {
	
	static private Pattern mCDGroupPattern = Pattern.compile("-?[0-9\\.]+[0-9\\.,]*\\s?%|((\\$\\s*)?((([0-9\\.][0-9\\.,]*)?[0-9])|(twenty|thirty|fourty|fifty|sixty|seventy|eighty|ninety|hundred|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen))+(\\s*(million|billion|trillion|hundred|thousand|dozen|K|M|B))?)", Pattern.CASE_INSENSITIVE);

	@Override
	public void process(TextDocument doc) {
		String docText = doc.getText();
		if(docText != null) {
			Matcher matcher = mCDGroupPattern.matcher(docText);
			while(matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				String matchedText = docText.substring(start, end);
				// Verify that it really is a group
				if(matchedText.startsWith("$") || (matchedText.indexOf(' ') != matchedText.lastIndexOf(' ')) || matchedText.endsWith("%")) {
					CDAnnotation newAnnot = new CDAnnotation(doc, start, end);
					doc.addAnnotation(newAnnot);
				}
			}
			
		}
	}

}
