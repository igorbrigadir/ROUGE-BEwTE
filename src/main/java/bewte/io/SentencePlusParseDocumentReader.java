package bewte.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;
import tratz.runpipe.annotations.Sentence;




public class SentencePlusParseDocumentReader implements TextDocumentReader {
	
	public void initialize(Map<String, String> params) {
		
	}
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int start = 0;
		StringBuilder buf = new StringBuilder();
		while((line = reader.readLine()) != null) {
			Sentence sentence = new Sentence(doc, start, start + line.length());
			String parse = reader.readLine();
			sentence.setParseString(parse);
			doc.addAnnotation(sentence);
			buf.append(line);
			start += line.length();
		}
		reader.close();
		doc.setText(buf.toString());
	}
	
	
}