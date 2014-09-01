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

package tratz.runpipe.impl.endpoints;

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
import tratz.runpipe.annotations.Token;
import tratz.runpipe.annotations.Token.Arc;
import tratz.runpipe.util.RunpipeUtils;

/**
 * Writes out .dot files for visualization
 *
 */
public class DotFileWriter implements EndPoint {

	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	private int mDocCounter;
	
	public void initialize(Map<String,String> args) throws InitializationException {
		String outputDir = args.get(PARAM_OUTPUT_DIR);
		mOutputDir = new File(outputDir);
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
		List<Token> tokens = (List)doc.getAnnotationList(Token.class);
		try {
			int sentenceNo = 0;
			for(Sentence sentence : sentences) {
				PrintWriter writer = new PrintWriter(new FileWriter(new File(mOutputDir, mDocCounter+"-"+sentenceNo+".txt")));
				List<Token> subtokens = RunpipeUtils.getSublist(sentence, tokens);
				writer.println("digraph G {");
				for(Token t : subtokens) {
					Arc parentArc = t.getParentArc();
					if(parentArc != null) {
						Token parent = parentArc.getHead();
						if(parent != null) {
							writer.println("\""+parent.getStart()+":"+parent.getAnnotText()+":"+parent.getPos() + "\" -> \"" + t.getStart() + ":" + t.getAnnotText()+":"+t.getPos() + "\" [label=\"" + parentArc.getDependency() + "\"];");
						}
					}
				}
				writer.println("}");
				writer.close();
				sentenceNo++;
			}
			mDocCounter++;			
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
	}
	
	public void batchFinished() {
		// do nothing
	}
	
}