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

package tratz.pos.featgen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


import tratz.featgen.InitException;
import tratz.featgen.fer.ClosedClassFER;
import tratz.featgen.fer.WordNetPosTypesFER;
import tratz.parse.types.Token;
import tratz.types.ChecksumMap;

/**
 * Default feature generator for English POS tagging.
 * This isn't implemented in a very efficient manner. 
 * In the future it will make sense to cache things. Also there could be some room for generalization, though that will likely hurt running time
 *
 */
public class DefaultEnglishPosFeatureGenerator implements PosFeatureGenerator, Serializable {
	public static final long serialVersionUID = 1;
	
	private transient WordNetPosTypesFER mDerived;
	private transient ClosedClassFER mClosedClass;
	//private Map<String, String> mFeatMap = new HashMap<String, String>();
	private ChecksumMap<String> mFeatMap = new ChecksumMap<String>();//HashMap<String, String>();
	//private ObjectIntHashMap mFeatMap = new ObjectIntHashMap(); 
	public DefaultEnglishPosFeatureGenerator() throws Exception {
		int minOccurrence = 1;//minOccurrenceS == null ? 1 : Integer.parseInt(minOccurrenceS);
		int maxDepth = Integer.MAX_VALUE;//maxDepthS == null ? Integer.MAX_VALUE : Integer.parseInt(maxDepthS);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("data/brownClusters200.gz"))));
			String line = null;
			byte lastByte = 0;
			String lastByteString = "";
			String lastOne = null;
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\\t+");
				String path = split[0];
				String token = split[1];
				int occurrences = Integer.parseInt(split[2]);
				if(occurrences >= minOccurrence) {
					String blarg = path.substring(0, Math.min(path.length(), maxDepth));
					if(!blarg.equals(lastOne)) {
						lastOne = blarg;
						lastByte++;
						lastByteString = Byte.toString(lastByte);
					}
					//mFeatMap.put(token, lastByteString);
					mFeatMap.put(token, lastByte);
				}
			}
			reader.close();
		}
		catch(IOException ioe) {
			throw new InitException(ioe);
		}
		mDerived = new WordNetPosTypesFER();
		mClosedClass = new ClosedClassFER();
	}
	
	@Override
	public void init() {
		
	}
	
	private String getPos(List<Token> tokens, int location) {
		 return (location >= 0) ? tokens.get(location).getPos() : "Þ";
	}
	
	private String getBC(String s) {
		String bc;
		if(s=="Þ") {
			bc = "-2";
		}
		else {
			String numericized = DIGITS.reset(s).replaceAll("ƞ");
			if(!mFeatMap.containsKey(numericized)) {
				String lower = numericized.toLowerCase();
				if(!mFeatMap.containsKey(lower)) {
					bc = "-1";
				}
				else {
					int val = mFeatMap.get(lower);
					bc = Integer.toString(val);
				}
			}
			else {
				int val = mFeatMap.get(numericized);
				bc = Integer.toString(val);
			}
		}
		return bc;
	}
	
	private String getForm(List<Token> tokens, int index) {
		return tokens.get(index).getText();
	}
	
	private transient Matcher NUM_DASH_NUM = null;
	private transient Matcher HAS_DIGIT = null;
	private transient Matcher NUMBER_COMMA_ETC = null;
	private transient Matcher DIGITS = null;
	private transient Matcher HAVE_VERB = null;
	
	// Don't be square ⽅
	// ⼾⼿⽀⽁⽂
	@Override
	public Set<String> getFeats(final List<Token> tokens, final int index) {
		if(NUM_DASH_NUM == null) {
			mDerived = new WordNetPosTypesFER();
			mClosedClass = new ClosedClassFER();
			NUM_DASH_NUM = Pattern.compile("[0-9]+\\-[0-9]+").matcher("");
			HAS_DIGIT = Pattern.compile(".*[0-9].*").matcher("");
			NUMBER_COMMA_ETC = Pattern.compile("[0-9,\\.:]+").matcher("");
			DIGITS = Pattern.compile("[0-9]").matcher("");
			HAVE_VERB = Pattern.compile("having|have|had|has|'ve|'d").matcher("");
		}
		
		Set<String> f = new HashSet<String>();
		synchronized(this) {
		int numTokens = tokens.size();
		String wl4 = (index > 3) ? getForm(tokens,index-4) : "Þ";
		String wl3 = (index > 2) ? getForm(tokens,index-3) : "Þ";
		String wl2 = (index > 1) ? getForm(tokens,index-2) : "Þ";
		String wl1 = (index > 0) ? getForm(tokens,index-1) : "Þ";
		String w0 = getForm(tokens,index);
		String wr1 = (index < numTokens-1) ? getForm(tokens,index+1) : "Þ";
		String wr2 = (index < numTokens-2) ? getForm(tokens,index+2) : "Þ";
		String wr3 = (index < numTokens-3) ? getForm(tokens,index+3) : "Þ";
		String wr4 = (index < numTokens-4) ? getForm(tokens,index+4) : "Þ";
		/*if(index <= 0)  w0 = w0.toLowerCase();*/
		
		
		if(index == 0) w0 = w0.toLowerCase();
		if(index == 1)  wl1 = wl1.toLowerCase();
		if(index == 2)  wl2 = wl2.toLowerCase();
		if(index == 3)  wl3 = wl3.toLowerCase();
		if(index == 4)  wl4 = wl4.toLowerCase();
		boolean es0 = (w0.endsWith("s")||w0.endsWith("S"));
		
		
		String bcl4, bcl3, bcl2, bcl1, bc0, bcr1, bcr2, bcr3, bcr4;
		bcl4 = getBC(wl4);bcl3 = getBC(wl3);bcl2 = getBC(wl2);bcl1 = getBC(wl1);
		bc0 = getBC(w0);
		bcr1 = getBC(wr1);bcr2 = getBC(wr2);bcr3 = getBC(wr3);bcr4 = getBC(wr4);
		
		//if(allUpperCase(w0)) {f.add("⻑"); f.add("⻑"+bc0);};
		
		String pl4 = (index > 3) ? getPos(tokens, index-4) : "Þ";
		String pl3 = (index > 2) ? getPos(tokens, index-3) : "Þ";
		String pl2 = (index > 1) ? getPos(tokens, index-2) : "Þ";
		String pl1 = (index > 0) ? getPos(tokens, index-1) : "Þ";
		
		Token pv = null;
		Token preIN = null;
		//boolean hasPreThat = false;
		for(int i = index-1; i >= 0; i--) {
			Token t = tokens.get(i);
			if(t.getPos().startsWith("VB") || t.getPos().equals("MD")) {
				pv = t; break;
			}
			else if(t.getPos().equals("IN")) {
				preIN = t;
				//if(t.getText().equalsIgnoreCase("that")) {
					//hasPreThat = true;
				//}
			}
		}
		
		if(wr1.equals(".") || wr1.equals(",") || wr1.equals(";") || wr1.equals(":")) {
			// uninterrupted (verbwise) WH-term or IN
			Token whOrIn = null;
			for(int i = index-1; i >= 0; i--) {
				Token t = tokens.get(i);
				String pos = t.getPos();
				if(pos.startsWith("VB")) { 
					break;
				}
				else if(pos.equals("IN") || pos.startsWith("W")) {
					whOrIn = t;
					break; // search interrupted
				}
			}
			if(whOrIn != null){
				f.add("preWhOrIn");
				f.add("⺺⺺"+whOrIn.getText().toLowerCase());
				f.add("⺺⺺"+whOrIn.getText().toLowerCase()+"⺺" + wr1);
			}
		}
		
	//	f.add("⻟"+(preWRB==null?"null":"WRB+"+bc0));
		f.add((preIN==null?"null":preIN.getText().toLowerCase())+"⻠"+bc0);
		
		Token preTwoCommas = null;
		Token postTwoCommas = null;
		if(index > 1) {
			Token prev = tokens.get(index-2);
			if(prev.getText().equals(",")) {
				for(int i = index-3; i >= 1; i--) {
					Token t = tokens.get(i);
					if(t.getText().equals(",")) {
						preTwoCommas = tokens.get(i-1);
						postTwoCommas = tokens.get(i+1);
						break;
					}
				}
			}
		}
		if(preTwoCommas != null) {
			f.add(getBC(preTwoCommas.getText())+"⼗"+bc0);
			f.add(postTwoCommas.getText().startsWith("wh")+"⼕"+bc0);
		}
		
		
		
		boolean pnull = pv==null;
		String pvText = pnull ? "ƥ" : pv.getText().toLowerCase();
		String pvPos = pnull ? "ƥ": pv.getPos();
		f.add(pvText+"⼔"+bc0);
		f.add(pvText+"⼓"+w0);
		f.add(pvPos+"⼒"+bc0);
		f.add(pvPos+"⼑"+bc0+"+"+bcr1);
		// this is not exactly what was intended
		f.add("⼐"+(pnull?"ƥ":HAVE_VERB.reset(pvText).matches()+"+"+bc0));
		f.add(pvPos+"⼏"+pl1+"+"+bc0);
		Token prevVerb2 = null;
		if(pv != null) {
			
			
			for(int i = pv.getIndex()-2; i >= 0; i--) {
				Token t = tokens.get(i);
				if(t.getPos().startsWith("VB") || t.getPos().equals("MD")) {
					prevVerb2 = t; break;
				}
				
			}
			//f.add("⽅"+(prevVerb2==null?"null":prevVerb2.getPos()+","+pv.getPos())+"+"+bc0);
			//f.add("⽄"+(prevVerb2==null?"null":prevVerb2.getText()+","+pv.getPos())+"+"+bc0);
			
			f.add("⼌"+(prevVerb2==null?"null":prevVerb2.getPos())+"+"+bc0);
			//f.add("⼋"+(prevVerb2==null?"null":prevVerb2.getText().toLowerCase())+"+"+bc0);
		}
		if(pv != null && (pl1.equals("CC") || pl1.equals(","))) {
			f.add(pvPos+"⼜"+bc0); // allow help from both CC and , s... probably not too useful
		}
		
		
		// Ends in s combinations
		f.add("⻚"+bc0+es0+bcr1);
		f.add("⻛"+bc0+es0+pl1);
		f.add("⻜"+bc0+es0+wl1);
		f.add("⻝"+bc0+es0+wr1);
		f.add("⻡"+es0+bcr1);
		f.add("⻣"+pl1+es0+bcr1);
		f.add(es0+"⻢"+wr1);
		f.add(wl1+"⻤"+es0+"+"+wr1);
		// for 54
		if(es0) {
			// NNS to left should make NNS unlikely
			
			//f.add(wl1+"⻤⻤"+bcr1);	
			//f.add(pl1+"⻢⻢"+pl2);
			//f.add(pl1+"⻢"+pl2 + "⻢"+pl3);
			//f.add(bcr1+"⻚⻚"+bcr2);
			Token pdt = null; // previous uninterrupted determiner
			for(int i = index-1; i >= 0; i--) {
				Token t = tokens.get(i);
				String pos = t.getPos();
				if(pos.equals("DT")) {
					pdt = t; 
					break;
				}
				else if(!(pos.startsWith("N") || pos.startsWith("J") || pos.startsWith("R") || pos.startsWith("C"))) {
					break; // search interrupted
				}
			}
			String pdt_text = pdt == null ? "n" : pdt.getText().toLowerCase();
			f.add(pdt_text+"⻚");
			// check out the previous determiner and intermediate words, may preclude plural word
			if(pdt_text.equals("a") || pdt_text.equals("an") || pdt_text.equals("this") || pdt_text.equals("that")) {
				f.add("singpdt⻚");
			}
			f.add("⻢"+pvPos);
			f.add("⻢⻢"+(prevVerb2==null?"null":prevVerb2.getPos()));
			f.add("⻢⻢⻢"+pl1);
			// followed by a comma & ambiguous over prev 3 words
			// similar if followed by period
			// next word also ends with 's'
			// preceded by 'and' + prev verb POS
			
			if(preTwoCommas != null) {
				f.add(getBC(preTwoCommas.getText())+"⼗⻢"+bc0);
				f.add(postTwoCommas.getText().startsWith("wh")+"⼕⻢"+bc0);
			}
		}
		
		f.add(wl1+"⻬"+bc0);
		f.add(pl1+"⻭"+bc0+"+"+bcr1);
		f.add(pl2+"⻮"+bc0+"+"+bcr1);
		f.add(pl3+"⻯"+bc0+"+"+bcr1);
		f.add(pl2+"⻰"+pl1+"+"+bc0);
		// useful methinks...
		f.add(wl2+"⻱"+pl1);
		f.add(wl2+"⻲"+pl1+"+"+bc0);
		
		// For 13
		f.add(w0+"⻳"+bcr1); // about six
		f.add(wl2+"⼈"+bcl1+"+"+bc0);
		f.add(wl1+"⼉"+bc0+"+"+wr1);
		f.add(wl1+"⼊"+bc0+"+"+bcr1);
		
		// For 12
		f.add(wl2+"⼙"+wl1+"+"+bc0);
		// what about wl2+pl1+bc0 ?
		f.add(wl2+"⼚"+wl1+"+"+bc0+"+"+bcr1);
		f.add(wl2+"⼛"+pl1+"+"+bc0+"+"+bcr1);
		
		if((pl1.equals("MD") || pl2.equals("MD") || pl3.equals("MD") || pl4.equals("MD"))
				&& (!pl1.startsWith("VB") && !pl2.startsWith("VB") && !pl3.startsWith("VB") && !pl4.startsWith("VB"))) {
			f.add("⺺"+bc0); // 
			f.add("⺺t"); // 
		}
		
		Set<String> wl1ders = mDerived.getProductions(wl1, "", new HashSet<String>());
		Set<String> w0ders = mDerived.getProductions(w0, "", new HashSet<String>());
		Set<String> wr1ders = mDerived.getProductions(wr1, "", new HashSet<String>());
		Set<String> wr2ders = mDerived.getProductions(wr2, "", new HashSet<String>());
		Set<String> wr3ders = mDerived.getProductions(wr3, "", new HashSet<String>());
		
		mClosedClass.getProductions(wl1, "", wl1ders);
		mClosedClass.getProductions(w0,  "", w0ders);
		mClosedClass.getProductions(wr1, "", wr1ders);
		mClosedClass.getProductions(wr2, "", wr2ders);
		mClosedClass.getProductions(wr3, "", wr3ders);
		
		List<String> dev = new ArrayList<String>(w0ders);
		Collections.sort(dev);
		int devSize = dev.size();
		for(int i = 0; i < devSize; i++) {
			String s1 = dev.get(i);
			for(int j = i+1; j < devSize; j++) {
				f.add("⺶"+s1+","+dev.get(j));	
			}
		}
		//⺸⺹⼦⼧⼨⼩⼪⼫⼬⼭⼮⼯
		
		for(String wl1d : wl1ders) f.add("⺷"+wl1d);
		for(String w0d  : w0ders)  f.add("⺶"+w0d); 
		for(String wr1d : wr1ders) f.add("⺵"+wr1d); 
		for(String wr2d : wr2ders) f.add("⺴"+wr2d);
		for(String wr3d : wr3ders) f.add("⺴⺵"+wr3d);
		
		// AFFIX FEATURES
		final int w0length = w0.length();
		final int prefixDepth = Math.min(5, w0length);
		for(int i = 1; i < prefixDepth; i++) {
			f.add("⺭"+w0.substring(0, i));
		}
		final int suffixDepth = Math.min(9, w0length);
		for(int i = suffixDepth; i >= 0; i--) {
			f.add("⺬"+w0.substring(w0length-i, w0length));
		}
		f.add(Boolean.toString(w0.endsWith("ed") || w0.endsWith("n")));
		
		char firstChar = ' ';
		firstChar = w0.length() > 0 ? w0.charAt(0) : ' ';
		boolean isUppercase = Character.isUpperCase(firstChar);
		f.add("⺫"+isUppercase); // 
		f.add("⺪"+allLowerCase(w0)); // 
		f.add("⺩"+allUpperCase(w0)); // 
		
		int dashIndex = w0.lastIndexOf("-");
		f.add("⺦"+(dashIndex > -1));
		f.add("⺢"+w0.contains("."));
		f.add("⺨"+NUM_DASH_NUM.reset(w0).matches());
		f.add("⺡"+HAS_DIGIT.reset(w0).matches());
		
		f.add("⺠"+(isUppercase&&es0));
		f.add("⺟"+NUMBER_COMMA_ETC.reset(wr1).matches()); 
		f.add("⺞"+NUMBER_COMMA_ETC.reset(wr2).matches()); 
		f.add("⺝"+Character.isUpperCase(wl1.length() > 0 ? wl1.charAt(0) : 'n'));  
		
		if(dashIndex > -1) {
			f.add("⺜"+w0.substring(0, dashIndex));
			if(dashIndex < w0.length()-1) {
				String dashString = w0.substring(dashIndex+1);
				String endDashBC = getBC(dashString);
				f.add(endDashBC+"⺛"+bc0);
				f.add(endDashBC+"⺛"+bc0+"+"+bcr1);
			}
			int firstDash = w0.indexOf('-');
			if(firstDash > -1 && firstDash < w0.length()-1) {
				String preDash = w0.substring(0, dashIndex);
				f.add("⼥"+preDash); // duplicate feat! should delete!
			}
			if(firstDash != dashIndex) {
				int secondDash = w0.indexOf('-',firstDash+1);
				//feats.add("inDash:"+secondDash);
			}
		}
		
		Token lastToken = tokens.get(numTokens-1);
		if(lastToken.getText().equals(".")) f.add("l.");
		if(lastToken.getText().equals("!")) f.add("l!");
		if(lastToken.getText().equals("?")) f.add("l?");
		
		// Unigram POSes
		f.add("⺼"+pl4);f.add("⺻"+pl3);f.add("⺽"+pl2);f.add("⻁"+pl1);
		// Ambiguous to left POS
		f.add("⼝"+pl1); f.add("⼝"+pl2);
		
		// Bigram POS
		f.add("⺮"+pl2+"|"+pl1);
		// new!
		//f.add("⺮"+pl3+"|"+pl2+"|"+pl1);
		// What about pl2+pl3?, what about pl3+pl2+pl1???
		if(w0.equalsIgnoreCase("that")) {
			//f.add("⻄⻄"+wl4);f.add("⻄⻄"+wl3);f.add("⻄⻄"+wl2);f.add("⻄⻄"+wl1);
		}
		
		// Unigram Words 
		f.add("⻄"+wl3);f.add("⻅"+wl2);f.add("⻆"+wl1);
		f.add("⻇"+w0);
		f.add("⻉"+wr3);f.add("⻊"+wr2);f.add("⻋"+wr1);
		
		// ambiguous unigram words (new.. may be a bad thing)
		//f.add("⻉⻋"+wr1);f.add("⻉⻋"+wr2);f.add("⻉⻋"+wr3);
		
		// Unigram BCs 
		f.add("⻏"+bcl4);f.add("⻓"+bcl3);f.add("⻔"+bcl2);f.add("⻙"+bcl1);
		f.add("⻑"+bc0);
		f.add("⻒"+bcr4);f.add("⻘"+bcr3);f.add("⻕"+bcr2);f.add("⻗"+bcr1);
		
		// Ambiguous (2 in front, 2 in back)
		f.add("⻩"+bcr1);f.add("⻩"+bcr2);
		f.add("⻪"+bcl2);f.add("⻪"+bcl1);
		// Ambiguous (3 in front, 3 in back)
		f.add("⻌"+bcr1);f.add("⻌"+bcr2);f.add("⻌"+bcr3);
		f.add("⻍"+bcl1);f.add("⻍"+bcl2);f.add("⻍"+bcl3);
		// Ambiguous (4 in front, 4 in back) had no impact
		
		// Bigram Words
		// Consecutive
		f.add(wl2+"⺎"+wl1);
		f.add(wl1+"⺏"+w0); 
		f.add(w0+"⺑"+wr1); 
		f.add(wr1+"⺒"+wr2);
		// Sandwich
		f.add(wl1+"⺐"+wr1);
		
		// Bigram BCs
		// Consecutive
		f.add(bcl2+"⻥"+bcl1);
		f.add(bcl1+"⻦"+bc0);
		f.add(bc0+"⻧"+bcr1);
		f.add(bcr1+"⻨"+bcr2);
		// Sandwich
		f.add(bcl1+"⻫"+bcr1);
		// Ambiguous (window=2)
		
		// Readded as a test
		//f.add("⻩"+bc0+"+"+bcr1);f.add("⻩"+bc0+"+"+bcr2);
		//f.add("⻪"+bcl2+"+"+bc0);f.add("⻪"+bcl1+"+"+bc0);
		
		// Ambiguous (window=3) (noticeable slowdown in training time)
		//f.add("⻌"+bc0+"+"+bcr1);f.add("⻌"+bc0+"+"+bcr2);f.add("⻌"+bc0+"+"+bcr3);
		//f.add("⻍"+bc0+"+"+bcl1);f.add("⻍"+bc0+"+"+bcl2);f.add("⻍"+bc0+"+"+bcl3);
		
		// Trigram Words
		// Consective
		f.add(wl2+"⺓"+wl1+"|"+w0);
		f.add(wl1+"⺕"+w0+"|"+wr1);
		f.add(w0+"⺙"+wr1+"|"+wr2);
		// Skips
		f.add(wl2+"⺔"+wl1+"|"+wr1);
		f.add(wl1+"⺘"+wr1+"|"+wr2); 
		
		// Trigram BCs
		// Consective
		f.add(bcl2+"⼟"+bcl1+"+"+bc0);
		f.add(bcl1+"⼡"+bc0+"+"+bcr1);
		f.add(bc0+"⼢"+bcr1+"+"+bcr2);
		// Skips
		f.add(bcl2+"⼣"+bcl1+"+"+bcr1);
		f.add(bcl1+"⼤"+bcr1+"+"+bcr2);
		
		
		
		
		// TEST
		//f.add("⼤"+bcl2+"+"+bcl1+"+"+bcr1+"+"+bcr2);
		
		//f.add("⼢⼢"+bcr1+"+"+bcr2+"+"+bcr3);
		
		// for 53
		//f.add(wr1+"⺒⺒"+wr2.substring(wr2.length()-Math.min(wr2.length(), 3)));
		//f.add(wr1+"⺒⼤"+wr2.substring(0, Math.min(wr2.length(), 3)));
		}
		return f;
	}
	
	private boolean allUpperCase(String s) {
		final int len = s.length();
		boolean allUpper = true;
		for(int i = 0; i < len; i++) {
			boolean isUpper = Character.isUpperCase(s.charAt(i));
			if(!isUpper) {
				allUpper = false;
				break;
			}
		}
		return allUpper;
	}
	
	private boolean allLowerCase(String s) {
		final int len = s.length();
		boolean allLower = true;
		for(int i = 0; i < len; i++) {
			boolean isLower = Character.isLowerCase(s.charAt(i));
			if(!isLower) {
				allLower = false;
				break;
			}
		}
		return allLower;
	}
}