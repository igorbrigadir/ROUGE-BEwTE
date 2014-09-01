package bewte.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import tratz.runpipe.InitializationException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;
import tratz.runpipe.annotations.Sentence;


public class BySentenceDocumentReader implements TextDocumentReader {
	
	private Map<String, String> mReplacementMap = new HashMap<String, String>();
	
	public void initialize(Map<String, String> params) throws InitializationException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("conf/transformations/diacriticAndLigatureCharacters.txt"));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line.trim().equals("")) continue;
				// For some reason, some funky characters have caused issues with 
				// some version of OpenJDK on a Linux box, so let's avoid regexs for now...
				String[] split = line.split("\\t+");
				String chars = split[0];
				String output = split[1];
				final int numChars = chars.length();
				for(int i = 0; i < numChars; i++) {
					mReplacementMap.put(chars.substring(i, i+1), output);
				}
			}
			reader.close();
		}
		catch(IOException ioe) {
			throw new InitializationException(ioe);
		}
	}
	
	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int start = 0;
		StringBuilder buf = new StringBuilder();
		while((line = reader.readLine()) != null) {
			String parse = reader.readLine();
			for(String key : mReplacementMap.keySet()) {
				line = line.replace(key, mReplacementMap.get(key));
				if(parse != null) parse = parse.replace(key, mReplacementMap.get(key));
			}
			Sentence s = new Sentence(doc, start, start + line.length());
			s.setParseString(parse);
			doc.addAnnotation(s);
			buf.append(line);
			start += line.length();
		}
		reader.close();
		doc.setText(buf.toString());
	}
	
	
}