package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

/**
 * Created for WMT data
 * Reads lines of form
 */
public class WMTDocReader extends CanonicalizingTextDocumentReader {
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String l = null;
		List<String> lines = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			lines.add(canonicalizeString(l));
		}
		
		// Must not leave a blank sentence at end or something weird happens (I forget what now that I'm
		// making this comment
		int max = 0;
		for(int i = lines.size()-1; i >= 0; i--) {
			if(!lines.get(i).trim().equals("")) {
				max = i+1;
				break;
			}
		}
		
		StringBuffer docText = new StringBuffer();
		int start = 0;
		int sentenceNum = 1;
		for(int i = 0; i < max; i++) {
			String line = lines.get(i);
			int length = line.length();
			if(!line.trim().equals("")) {
				Sentence newSentence = new Sentence(doc, start, start+length);
				newSentence.setSentenceNum(sentenceNum);
				doc.addAnnotation(newSentence);
			}
			docText.append(line);
			start+=length;				
			sentenceNum++;
		}
		doc.setText(docText.toString());
		
		reader.close();
	}
}