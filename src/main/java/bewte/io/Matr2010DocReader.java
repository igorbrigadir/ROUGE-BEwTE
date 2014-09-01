package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

public class Matr2010DocReader extends CanonicalizingTextDocumentReader {
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		// read the docid
		String firstLine = reader.readLine();
		//String oldUri = doc.getUri();
		//doc.setUri(oldUri.substring(oldUri.lastIndexOf('/')+1)+"\t"+firstLine);
		
		String l = null;
		List<String> lines = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			String sNum = l.substring(0, l.indexOf(' '));
			String rest = l.substring(l.indexOf(' '));
			lines.add(sNum + " " + canonicalizeString(rest)+" ");
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
		
		for(int i = 0; i < max; i++) {
			String line = lines.get(i);
			
			
			if(!line.trim().equals("")) {
				int segmentNum = Integer.parseInt(line.substring(0, line.indexOf(' ')));
				line = line.substring(line.indexOf(' '));
				Sentence newSentence = new Sentence(doc, start, start+line.length());
				newSentence.setSentenceNum(segmentNum);
				doc.addAnnotation(newSentence);
			}
			docText.append(line);
			start+=line.length();				
		}
		doc.setText(docText.toString());
		
		reader.close();
	}
}