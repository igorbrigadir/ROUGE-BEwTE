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

package tratz.semantics.psd.training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;
import tratz.runpipe.annotations.Sentence;

public class PrepDocReader implements TextDocumentReader {
	
	private String highDoubleQuotes;
	private String highSingleQuotes;
	public void initialize(Map<String, String> params) {
//		 145-148 are ANSI quotes, 132 is double low quotes
		// 8216-8223 quotes are supported by HTML (?Unicode?), 8242-8247 are multiple "primes"
		int[] doubleQuoteInts = new int[]{132,  171,187,147,148,8220,8221,8222,8223, 8243,8244,8246,8247};
		int[] singleQuoteInts = new int[]{145,146, 8216,8217,8218,8219, 8242,8245};
		StringBuilder buf = new StringBuilder();
		for(int i : doubleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		highDoubleQuotes = buf.toString();
		buf = new StringBuilder();
		for(int i : singleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		highSingleQuotes = buf.toString();
	}
	
	public void hydrateDocument(InputStream istream, TextDocument textDoc) throws IOException {
		
		int start = 0;
		StringBuilder docText = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("<instance")) {
				int idStartIndex = line.indexOf("id=\"");
				int idEnd = line.indexOf("\"", idStartIndex+4);
				String id = line.substring(idStartIndex+4, idEnd);
				
				String nextLine = reader.readLine().trim();
				String sense = null;
				// If this was a training instance, we will hit the <answer .../> line
				if(nextLine.startsWith("<answer")) {
					String senseAttribStart = "senseid=\"";
					int senseIndex = nextLine.indexOf(senseAttribStart);
					int endOfSenseIndex = nextLine.indexOf("\"", senseIndex+senseAttribStart.length());
					sense = nextLine.substring(senseIndex+senseAttribStart.length(), endOfSenseIndex);
					
					String contextLine = reader.readLine().trim();
					if(!contextLine.startsWith("<context")) {
						throw new RuntimeException("failed to parse psd instances file... corrupted? edited?");
					}
				}
				// If this was a test instance, we will hit the <context> line
				else if(!nextLine.startsWith("<context")) {
					throw new RuntimeException("failed to parse psd instances file... corrupted? edited?");
				}
				
				String contextText = reader.readLine().trim();
				contextText = contextText.replace("`", "``");
				contextText = contextText.replace("\"", "''");
				contextText = " " + contextText + " ";
				
				int headTagIndex = contextText.indexOf("<head>");
				int headTagEndIndex = contextText.indexOf("</head>");
				if(!contextText.contains("<head></head>")) {
				
					String preHeadTagText = contextText.substring(0, headTagIndex).trim();
					String postHead = contextText.substring(headTagEndIndex+7).trim();
					String headText = contextText.substring(headTagIndex+6, headTagEndIndex).trim();
				
				String tagRemovedText = preHeadTagText + " " + headText + " " + postHead;
				
				HeadAnnotation headAnnot = new HeadAnnotation(textDoc, start + preHeadTagText.length()+1, start + preHeadTagText.length()+1 + headText.length());
				headAnnot.setId(id);
				headAnnot.setSenseId(sense);
				//headAnnot.setOriginalSentence(contextText.replace("  ", " <head>" + preposition + "</head> "));
				//System.err.println(headAnnot.getOriginalSentence());
				textDoc.addAnnotation(headAnnot);
				
				//contextText = contextText.replace("  ", " " + preposition + " ");
				
				/* tracking down bug
				docText.append(contextText);
				textDoc.addAnnotation(new Sentence(textDoc, start, start+contextText.length()));
				
				start += contextText.length();*/
				
				docText.append(tagRemovedText);
				textDoc.addAnnotation(new Sentence(textDoc, start, start+tagRemovedText.length()));
				
				start += tagRemovedText.length();
				}
			}
		}
		reader.close();
		textDoc.setText(docText.toString());
		
		/*	Document doc = builder.build(istream);
			Element root = doc.getRootElement();
			String preposition = root.getAttributeValue("item");
			List<Element> instances = root.getChildren("instance");
			StringBuilder docText = new StringBuilder();
			int start = 0;
			for(Element instance : instances) {
				String id = instance.getAttributeValue("id");
				Element answerAttrib = instance.getChild("answer");
				String senseid = "-1";
				if(answerAttrib != null) {
					senseid = answerAttrib.getAttributeValue("senseid");
				}
				Element context = instance.getChild("context");
				String contextText = context.getText().trim();
				//contextText = canonicalizeString(contextText);
				//contextText = contextText.replaceAll("\"\\s+([^\\s\"`']+)\\s+\"", "`` $1 ''");
				//contextText = contextText.replaceAll("`\\s+([^\\s\"`']+)\\s+\"", " `` $1 ''");
				contextText = contextText.replace("`", "``");
				contextText = contextText.replace("\"", "''");
				
				int doubleSpace = contextText.indexOf("  ");
				int count = 0;
				int orig = doubleSpace;
				//System.err.println(doubleSpace);
				while((doubleSpace = contextText.indexOf("  ", doubleSpace+2)) > -1) {
					count++;
					System.err.println(doubleSpace);
				}
				if(count != 0 || orig < 0) {
					contextText = "  " + contextText;
					doubleSpace = 0;
					//System.err.println("YARG " + count + " ");
					//System.err.println(contextText);
				}
				
					
					HeadAnnotation headAnnot = new HeadAnnotation(textDoc, start + orig + 1, start + orig + 1 + preposition.length());
					headAnnot.setId(id);
					headAnnot.setSenseId(senseid);
					//headAnnot.setOriginalSentence(contextText.replace("  ", " <head>" + preposition + "</head> "));
					//System.err.println(headAnnot.getOriginalSentence());
					textDoc.addAnnotation(headAnnot);
					
					contextText = contextText.replace("  ", " " + preposition + " ");
					docText.append(contextText);
					textDoc.addAnnotation(new Sentence(textDoc, start, start+contextText.length()));
					
					start += contextText.length();
				
			}
			textDoc.setText(docText.toString());*/
		
	}
	
}