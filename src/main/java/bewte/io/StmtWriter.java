package bewte.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

import foal.map.IntObjectHashMap;


public class StmtWriter implements EndPoint {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDir = new File(params.get(PARAM_OUTPUT_DIR));
		mOutputDir.mkdirs();
	}
	
	public void process(TextDocument doc) throws ProcessException {
		String uri = doc.getUri();
		String docFilename = uri.substring(uri.lastIndexOf(File.separatorChar)+1);
		String language = docFilename.substring(0,2);
		String task = docFilename.substring(docFilename.indexOf(".")+1, docFilename.lastIndexOf("."));
		String system = docFilename.substring(docFilename.lastIndexOf(".")+1) + "_" + language;
		//String[] parts = docFilename.split("\\+");
		//String docId = parts[1]
		//String system = parts[2];
		
		List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
		try {
			IntObjectHashMap<List<Sentence>> keyToSentences = new IntObjectHashMap<List<Sentence>>();
			int minKey = Integer.MAX_VALUE;
			int maxKey = Integer.MIN_VALUE;
			for(int i = 0; i < sentences.size(); i++) {
				Sentence sentence = sentences.get(i);
				int key = sentence.getSentenceNum();
				List<Sentence> sentenceList = keyToSentences.get(key);
				if(sentenceList == null) {
					keyToSentences.put(key, sentenceList = new ArrayList<Sentence>(1));
				}
				sentenceList.add(sentence);
				if(key > maxKey) {
					maxKey = key;
				}
				if(key < minKey) {
					minKey = key;
				}
			}
			
			for(int key = minKey; key < maxKey; key++) {
				List<Sentence> sentenceList = keyToSentences.get(key);
				String outputFilename = key + task + "." + system;
				//String outputFilename = key + "+" + docFilename;
				PrintWriter writer = new PrintWriter(new FileWriter(new File(mOutputDir, outputFilename)));
				if(sentenceList != null) {
					for(Sentence sentence : sentenceList) {
						writer.println(sentence.getAnnotText());
						writer.println(sentence.getParseString());
					}
				}
				else {
					writer.println();
					writer.println();
				}
				writer.close();
			}
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
	}
	
	public void batchFinished() {
		// Do nothing
	}
	
}