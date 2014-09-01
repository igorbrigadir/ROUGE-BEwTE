/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package tratz.parse.io;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import tratz.jwni.WordNet;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.util.NLParserUtils;
import tratz.parse.util.ParseConstants;

/**
 * Writes out an XML document of sentences
 *
 */
public class XMLSentenceWriter implements SentenceWriter {
	
	public final static String PARAM_OUTPUT_FILE = "output";
	
	public final static String SENTENCE_TAG = "s";
	public final static String WORDS_TAG = "words";
	public final static String WORD_TAG = "word";
	
	private XMLStreamWriter mXmlWriter;
	
	private int sentenceNum = 1;
	
	public void initialize(Map<String, String> params) throws Exception {
		String outputFilename = params.get(PARAM_OUTPUT_FILE);
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
        mXmlWriter = xof.createXMLStreamWriter(new FileWriter(outputFilename));
        mXmlWriter.writeStartDocument("1.0");
        mXmlWriter.writeStartElement("sentences");
	}

	@Override
	public void appendSentence(Sentence sentence, Parse parse) {
		appendSentence(sentence, parse, null);
	}
	
	@Override
	public void appendSentence(Sentence sentence, Parse parse, Arc[] tokenToSemanticHead) {
		
		List<Token> tokens = sentence.getTokens();
		try {
		// Start of sentence
		mXmlWriter.writeStartElement(SENTENCE_TAG);
		mXmlWriter.writeAttribute("id", ""+sentenceNum);
		
		// Start of words
		mXmlWriter.writeStartElement(WORDS_TAG);
		
		for(Token t : tokens) {
			// Start of word
			mXmlWriter.writeStartElement(WORD_TAG);
			mXmlWriter.writeAttribute("ind", ""+t.getIndex());
			mXmlWriter.writeAttribute("pos", ""+t.getPos());
			mXmlWriter.writeAttribute("lemma", ""+NLParserUtils.getLemma(t, WordNet.getInstance()));
			mXmlWriter.writeCharacters(t.getText());
			// End of word
			mXmlWriter.writeEndElement();
		}
		
		// Start of dependencies
		mXmlWriter.writeStartElement("dependencies");
		mXmlWriter.writeAttribute("style", "typed");
		for(Token t : tokens) {
			Arc headarc = parse.getHeadArcs()[t.getIndex()];
			
			if(headarc == null) {
				headarc = new Arc(t, parse.getRoot(), ParseConstants.ROOT_DEP);
			}
			
			// Start of dependency
			mXmlWriter.writeStartElement("dep");
			mXmlWriter.writeAttribute("type", headarc.getDependency());
			
			// The governor
			mXmlWriter.writeStartElement("governor");
			mXmlWriter.writeAttribute("idx", "" + headarc.getHead().getIndex());
			mXmlWriter.writeCharacters(headarc.getHead().getText());
			mXmlWriter.writeEndElement();
			
			// The dependent
			mXmlWriter.writeStartElement("dependent");
			mXmlWriter.writeAttribute("idx", ""+headarc.getChild().getIndex());
			mXmlWriter.writeCharacters(headarc.getChild().getText());
			mXmlWriter.writeEndElement();
			
			// End of dependency
			mXmlWriter.writeEndElement();

		}
		// End of dependencies
		mXmlWriter.writeEndElement();
		
		// End of words
		mXmlWriter.writeEndElement();
		// End of sentence
		mXmlWriter.writeEndElement(); 
		sentenceNum++;
		
		}
		catch(XMLStreamException xse) {
			// Not sure what to do here.. let's throw a runtime exception
			throw new RuntimeException(xse);
		}
		
	}

	@Override
	public void close() {
		try {
			mXmlWriter.writeEndElement();
			mXmlWriter.writeEndDocument();
			mXmlWriter.close();
		}
		catch(XMLStreamException xse) {
			// Not sure what to do here.. let's throw a runtime exception
			throw new RuntimeException(xse);
		}
	}

	@Override
	public void flush() {
		try {
			mXmlWriter.flush();
		}
		catch(XMLStreamException xse) {
			// Not sure what to do here.. let's throw a runtime exception
			throw new RuntimeException(xse);
		}
	}
	
}