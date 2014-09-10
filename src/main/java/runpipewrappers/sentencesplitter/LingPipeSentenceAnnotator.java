package runpipewrappers.sentencesplitter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.Annotator;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class LingPipeSentenceAnnotator implements Annotator {

	public final static String ONLY_WHEN_NECESSARY = "ONLY_WHEN_NECESSARY";
	
	private boolean mOnlyWhenNecessary;
	private TokenizerFactory mTokenizerFactory = new IndoEuropeanTokenizerFactory();
	private SentenceModel mModel = new IndoEuropeanSentenceModel();
	private SentenceChunker mChunker = new SentenceChunker(mTokenizerFactory, mModel);
	
	public void initialize(Map<String, String> params) {
		String result = params.get(ONLY_WHEN_NECESSARY);
		if(result != null) {
			mOnlyWhenNecessary = Boolean.parseBoolean(result);
		}
	}
	
	public void process(TextDocument doc) {
		List sentences = doc.getAnnotationList(Sentence.class);
		if(!mOnlyWhenNecessary || sentences == null) {
		String docText = doc.getText();
		Chunking chunking = mChunker.chunk(docText);
		Set<Chunk> sChunks = (Set<Chunk>)chunking.chunkSet();
		int end = 0;
		for(Chunk chunk : sChunks) {
			int start = chunk.start();
			end = chunk.end();
			if(end - start > 450) {
				System.err.println("Excessive");
				// Excessive Length Sentence. Breaking up based upon periods.  HACKY
				int lastStart = start;
				int periodIndex = docText.indexOf('.', lastStart);
				while(periodIndex >= 0 && periodIndex < end) {
					Sentence sentence = new Sentence(doc);
					sentence.setStart(lastStart);
					sentence.setEnd(periodIndex);
					doc.addAnnotation(sentence);
					System.err.println("Creating sentence from: " + lastStart + " to " + periodIndex);
					lastStart = periodIndex+1;
					periodIndex = docText.indexOf('.', lastStart+1);
				}
				if(lastStart < end) {
					Sentence sentence = new Sentence(doc);
					sentence.setStart(lastStart);
					sentence.setEnd(end);
					System.err.println("Creating sentence from: " + lastStart + " to " + end);
					doc.addAnnotation(sentence);
				}
			}
			else {
				Sentence sentence = new Sentence(doc);
				sentence.setStart(start);
				sentence.setEnd(end);
				doc.addAnnotation(sentence);
			}
		}
		String endText = docText.substring(end, docText.length());
		if(!endText.matches("\\s*")) {
			Sentence sentence = new Sentence(doc);
			sentence.setStart(end);
			sentence.setEnd(docText.length());
			doc.addAnnotation(sentence);
		}
		}
	}
	
}