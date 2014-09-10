package runpipewrappers.ner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.Annotator;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.LocationAnnotation;
import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;
import tratz.runpipe.annotations.Sentence;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

/**
 * LingPipe interfacing code
 */
public class LingPipeNerWrapper implements Annotator {
	
	public final static String PARAM_MODEL_LOCATION = "ModelLocation";
	
	private final static String TYPE_PERSON = "PERSON";
	private final static String TYPE_ORG = "ORGANIZATION";
	private final static String TYPE_LOCATION = "LOCATION";
	
	private Chunker mChunker;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		ObjectInputStream inStream = null;
		try {
			String modelLocation = params.get(PARAM_MODEL_LOCATION);
			inStream = new ObjectInputStream(new FileInputStream(modelLocation));
			mChunker = (Chunker)inStream.readObject();
		}
		catch (IOException ioe) {
			throw new InitializationException(ioe);
		}
		catch(ClassNotFoundException cnfe) {
			throw new InitializationException(cnfe);
		}
		finally {
			if(inStream != null) {
				try {
					inStream.close();
				}
				catch(IOException ioe) {
					// Ignore
				}
			}
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		String docText = doc.getText();
		List<Sentence> sentenceList = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		if(sentenceList != null) {
			for(Sentence sentence : sentenceList) {
				String sentenceText = sentence.getAnnotText();
				Chunking chunkResults = mChunker.chunk(sentenceText);
				Set chunkSet = chunkResults.chunkSet();
				for(Chunk chk : (Set<Chunk>)chunkSet) {
					String type = chk.type();
					int begin = chk.start()+sentence.getStart();
					int end = chk.end()+sentence.getStart();
					if(begin != end) {
						String text = docText.substring(begin, end);
						if(text.length() > 2 && text.endsWith(" .")) {
							end-=2;
						}
						if(text.length() > 2 && text.endsWith(" ,")) {
							end-=2;
						}
						if(type.equals(TYPE_PERSON)) {
							doc.addAnnotation(new PersonAnnotation(doc, begin, end));
						}
						else if(type.equals(TYPE_ORG)) {
							doc.addAnnotation(new OrganizationAnnotation(doc, begin, end));
						}
						else if(type.equals(TYPE_LOCATION)) {
							doc.addAnnotation(new LocationAnnotation(doc, begin, end));
						}					 
					}
				}
			}
		}
	}
}