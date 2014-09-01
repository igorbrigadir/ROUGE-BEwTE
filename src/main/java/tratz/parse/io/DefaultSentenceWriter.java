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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

public class DefaultSentenceWriter implements SentenceWriter {
	
	public final static String PARAM_OUTPUT_FILE = "output";
	public final static String PARAM_AUTO_FLUSH = "autoflush";
	
	private PrintWriter mWriter;
	private boolean mAutoFlush = true;
	
	@Override
	public void initialize(Map<String, String> args) throws IOException {
		String outputFile = args.get(PARAM_OUTPUT_FILE);
		if(outputFile != null) {
			mWriter = new PrintWriter(new FileWriter(outputFile));
		}
		else {
			mWriter = new PrintWriter(new OutputStreamWriter(System.out));
		}
		String autoflush = args.get(PARAM_AUTO_FLUSH);
		if(autoflush != null) {
			mAutoFlush = Boolean.parseBoolean(autoflush);
		}
	}
	
	@Override
	public void appendSentence(Sentence sentence,
							   Parse parse, 
			                   Arc[] tokenToSemanticHead) {
		List<Token> tokens = sentence.getTokens();
		int numTokens = tokens.size();
		for(int i = 0; i < numTokens; i++) {
			Token token = tokens.get(i);
			Arc arc = parse.getHeadArcs()[token.getIndex()];
			
			if(arc == null) {
				// This is hacky... should come up with a better solution
				arc = new Arc(token, new Token("",0), ParseConstants.ROOT_DEP);
			}
			
			mWriter.print((i+1));
			mWriter.print("\t");
			mWriter.print(token.getText());
			mWriter.print("\t_\t");
			mWriter.print(token.getCoarsePos()==null?"_":token.getCoarsePos());
			mWriter.print("\t");
			mWriter.print(token.getPos()==null?"_":token.getPos());
			mWriter.print("\t_\t");
			mWriter.print(arc.getHead() == null ? 0 : arc.getHead().getIndex());
			mWriter.print("\t");
			mWriter.print(arc.getDependency()+"\t");
			mWriter.print("_\t_");
			
			mWriter.print("\t");
			mWriter.print(token.getLexSense() != null ? token.getLexSense() : "_");
			
			mWriter.print("\t");
			mWriter.print(arc.getSemanticAnnotation() != null ? arc.getSemanticAnnotation() : "_");
			
			mWriter.print("\t");
			Arc semArc = tokenToSemanticHead == null ? null : tokenToSemanticHead[token.getIndex()];
			if(semArc != null && semArc.getSemanticAnnotation() != null) {
				mWriter.print(semArc.getSemanticAnnotation());
				mWriter.print("\t");
				mWriter.print(semArc.getHead() == null ? -1 : semArc.getHead().getIndex());
			}
			else {
				mWriter.print("_\t_");
			}
			
			
			mWriter.println();
		}
		mWriter.println();
		
		if(mAutoFlush) {
			flush();
		}
	}
	
	@Override
	public void appendSentence(Sentence sentence, Parse parse) {
		appendSentence(sentence, parse, null);
	}
	
	@Override
	public void flush() {
		mWriter.flush();
	}
	
	@Override
	public void close() {
		mWriter.close();
	}
}