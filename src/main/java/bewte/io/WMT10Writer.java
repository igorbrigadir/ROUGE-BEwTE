package bewte.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

public class WMT10Writer implements EndPoint {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDir = new File(params.get(PARAM_OUTPUT_DIR));
		mOutputDir.mkdirs();
	}
	
	public void process(TextDocument doc) throws ProcessException {
		String uri = doc.getUri();
		
		String outputFilename = uri.substring(0, uri.indexOf('\t'));
		List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(new File(mOutputDir, outputFilename)));
			for(Sentence sentence : sentences) {
				writer.println(sentence.getAnnotText());
				writer.println(sentence.getParseString());
			}
			writer.close();
			
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
	}
	
	public void batchFinished() {
		// Do nothing
	}
	
}