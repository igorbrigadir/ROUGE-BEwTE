package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;
import tratz.runpipe.annotations.Sentence;

import bewte.BEConstants;

public class SummaryDocReader implements TextDocumentReader {
	
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
			if(!l.equals("")) {
				l = l.replace("''", "\"").replace("``", "\"").replace("`", "'").replace(" _ ", " - ").replace(" -- ", " - ").replace("- -", " - ").replaceAll(" ' (s|re|ve)([^a-zA-Z])?", "'$1$2").replaceAll(" '(s|re|ve)([^a-zA-Z])?", "'$1$2").replaceAll("\\. \\. \\.[ \\.]*", "... ").replace("$ ", "$").replace("' ll", "'ll").replaceAll("n 't([^a-zA-Z])?", "n't$1").replaceAll(" n't","n't").replace("( ", "(").replace(" )", ")").replace("...", ".");
				l = l.replace(BEConstants.BE_SEPARATOR_CHAR, ' ');
				l = l.replaceAll("([a-z]\\.)([A-Z])", "$1.\n$2");
				l = l.replaceAll("([a-z]\\?)([A-Z])", "$1?\n$2");
				l = l.replaceAll("([a-z]\\!)([A-Z])", "$1!\n$2");
				lines.addAll(Arrays.asList(l.split("\\n")));
			}
		}
		if(lines.size() == 1) {
			// Sentences not already split.  Sentence splitting needed.
			doc.setText(lines.get(0));
		}
		else {
			int x = 1;
			for(String line : lines) {
				int length = line.length();
				Sentence newSentence = new Sentence(doc, start, start+length);
				newSentence.setSentenceNum(x++);
				doc.addAnnotation(newSentence);
				buf.append(line);
				start+=length;				
			}
			doc.setText(buf.toString());
		}
		
		reader.close();
	}
}