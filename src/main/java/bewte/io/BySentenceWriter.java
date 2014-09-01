package bewte.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

import foal.map.IntObjectHashMap;

public class BySentenceWriter implements EndPoint {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	private DecimalFormat mOutputFolderFormat = new DecimalFormat();
	
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDir = new File(params.get(PARAM_OUTPUT_DIR));
		mOutputDir.mkdirs();
		mOutputFolderFormat.setGroupingUsed(false);
		mOutputFolderFormat.setMinimumIntegerDigits(8);
		mOutputFolderFormat.setMaximumFractionDigits(0);
	}
	public final static int MAX_PER_DIR = 200;
	private int mNumInCurrentDir;
	private File mCurrentDir;
	private int mNumDirs;	
	
	
	public void process(TextDocument doc) throws ProcessException {
		String uri = doc.getUri();
		int tabIndex = uri.indexOf('\t');
		if(tabIndex >= 0) {
			uri = uri.substring(0, tabIndex);
		}
		String docFilename = uri.substring(uri.lastIndexOf(File.separatorChar)+1);
		
		mNumInCurrentDir++;
		if(mNumInCurrentDir > MAX_PER_DIR || (mNumDirs == 0 && mNumInCurrentDir == 1)) {
			mNumInCurrentDir = 0;
			mCurrentDir = new File(mOutputDir, mOutputFolderFormat.format(mNumDirs));
			mCurrentDir.mkdirs();
			mNumDirs++;
		}
		
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
			
			for(int key = minKey; key <= maxKey; key++) {
				List<Sentence> sentenceList = keyToSentences.get(key);
				String[] split = docFilename.split("\\+");
				String setid = split[0];
				String docid = split[1];
				String system = split[2];
				String outputFilename = setid + "+" + docid + "+" + key + "+" + system;
				
				if(sentenceList != null) {
					PrintWriter writer = new PrintWriter(new FileWriter(new File(mCurrentDir, outputFilename)));
					for(Sentence sentence : sentenceList) {
						writer.println(sentence.getAnnotText());
						writer.println(sentence.getParseString());
					}
					writer.close();
				}
				else {
					//writer.println();
					//writer.println();
				}
				
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