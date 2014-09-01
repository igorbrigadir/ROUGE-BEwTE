package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import tratz.runpipe.TextDocument;

/**
 * Read text document, perform some rudimentary cleanup. 
 *
 */
public class StandardTextDocReader extends CanonicalizingTextDocumentReader {
	
	public void hydrateDocument(InputStream istream, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
		String l = null;
		List<String> sentences = new ArrayList<String>();
		while((l = reader.readLine()) != null) {
			l = l.trim();
			if(!l.equals("")) {
				l = canonicalizeString(l);
				
				// Save input text as array of sentences
				for(String line : l.split("\\n")) {
					sentences.add(line+ " ");
				}
				//sentences.addAll(Arrays.asList());
			}
		}
		if(sentences.size() == 1) {
			// Sentences not already split.  Sentence splitting needed.
			doc.setText(sentences.get(0));
		}
		else {
			StringBuffer docText = new StringBuffer();
			int sentenceNum = 1;
			int start = 0;
			//Tokenizer tokenizer = new Tokenizer();
			for(String sentence : sentences) {
				//sentence = tokenizer.tokenize(sentence);
				int length = sentence.length();
				//Sentence newSentence = new Sentence(doc, start, start+length);
				//newSentence.setSentenceNum(sentenceNum++);
				//doc.addAnnotation(newSentence);
				docText.append(sentence);
				start+=length;				
			}
			doc.setText(docText.toString());
		}
		
		reader.close();
	}
}