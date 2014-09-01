package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tratz.parse.util.NLParserUtils;
import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;
import tratz.runpipe.annotations.Sentence;

import bewte.BEConstants;


/**
 * Created for WMT08... See also WMTDocReader which looks like a newer version (but wasn't reference in the build.xml file for some readon...)
 *
 * Expects line-by-line segments
 */
public class TranslatedDocReader implements TextDocumentReader {
	
	public void initialize(Map<String, String> params) {
	
	}
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String l = null;
		StringBuffer buf = new StringBuffer();
		int start = 0;
		List<String> lines = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			l = l.trim();
			l = l.replace("''", "\"").replace("``", "\"").replace("`", "'").replace(" _ ", " - ").replace(" -- ", " - ").replace("- -", " - ").replaceAll(" ' s([^a-zA-Z])?", "'s$1").replaceAll(" 's([^a-zA-Z])?", "'s$1").replaceAll(" ' s ", "'s ").replace(". . .", "...").replace("$ ", "$").replace("' ll", "'ll").replaceAll("n 't([^a-zA-Z])?", "n't$1").replaceAll(" n't","n't").replace("( ", "(").replace(" )", ")").replace("...", ".");
			l = l.replace(BEConstants.BE_SEPARATOR_CHAR, ' ');
			l = l.replaceAll(NLParserUtils.UNUSUAL_DOUBLE_QUOTES, "\"");
			l = l.replaceAll(NLParserUtils.UNUSUAL_SINGLE_QUOTES, "'");
			lines.add(l);
		}
		int max = 0;
		for(int i = lines.size()-1; i >= 0; i--) {
			if(!lines.get(i).trim().equals("")) {
				max = i+1;
				break;
			}
		}
		int x = 1;
		for(int i = 0; i < max; i++) {
			String line = lines.get(i);
			int length = line.length();
			if(!line.trim().equals("")) {
				Sentence newSentence = new Sentence(doc, start, start+length);
				newSentence.setSentenceNum(x);
				doc.addAnnotation(newSentence);
			}
			buf.append(line);
			start+=length;				
			x++;
		}
		doc.setText(buf.toString());
		
		reader.close();
	}
}