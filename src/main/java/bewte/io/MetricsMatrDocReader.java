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
 * Created for MetricsMATR challenge.
 * Reads lines of form "SEGMENT_ID\tSEGMENT"
 */
public class MetricsMatrDocReader extends CanonicalizingTextDocumentReader {
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String l = null;
		List<String> lines = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			lines.add(canonicalizeString(l));
		}
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
			int tabIndex = line.indexOf("\t");
			int segId;
			String segment;
			if(tabIndex == -1) {
				segId = Integer.parseInt(line);
				segment = ".";
			}
			else {
				segId = Integer.parseInt(line.substring(0, line.indexOf("\t")));
				segment = line.substring(line.indexOf("\t")+1);
			}
			int length = segment.length();
			
			if(!line.trim().equals("")) {
				Sentence newSentence = new Sentence(doc, start, start+length);
				newSentence.setSentenceNum(segId);
				doc.addAnnotation(newSentence);
			}
			docText.append(segment);
			start+=length;				
			sentenceNum++;
		}
		doc.setText(docText.toString());
		
		reader.close();
	}
}