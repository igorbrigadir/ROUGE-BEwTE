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

package tratz.parse.featgen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import tratz.featgen.InitException;
import tratz.parse.ml.ParseModel;
import tratz.parse.types.Arc;
import tratz.parse.types.Token;
import tratz.parse.types.TokenPointer;
import tratz.parse.util.ParseConstants;
import tratz.types.ChecksumMap;

/**
 * Feature generation class for parsing. 
 * A bit on the ugly side...
 * It seems as though the order in which the features are added to the Set 
 * may affect training, which is strange... that would only make sense if either 
 * 1) there is a bug or 2) there is some sort of numerical issue creeping up
 */
public class DefaultEnParseFeatureGenerator implements Serializable, ParseFeatureGenerator {
	
	public final static long serialVersionUID = 1;
	
	private final static String DUMMY_PART_OF_SPEECH = "na";
	transient TokenPointer mDummy = new TokenPointer(new Token(null, DUMMY_PART_OF_SPEECH, 0), null, null);
	
	
	public DefaultEnParseFeatureGenerator() throws Exception {
		loadMap();
	}
	
	// load the Brown clusters
	private void loadMap() throws Exception {
		mFeatMap2 = new ChecksumMap<String>();
		int minOccurrence = 1;
		int maxDepth = Integer.MAX_VALUE;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("data/brownClusters175.gz"))));
			String line = null;
			mBCs = new ArrayList<String>();
			String lastClass = null;
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\\t+");
				String path = split[0];
				String token = split[1];
				int occurrences = Integer.parseInt(split[2]);
				if(occurrences >= minOccurrence) {
					String clazz = Integer.toHexString(Integer.parseInt(path.substring(0, Math.min(path.length(), maxDepth)),2));
					if(!clazz.equals(lastClass)) {
						mBCs.add(clazz);
						lastClass = clazz;
					}
					mFeatMap2.put(token, mBCs.size()-1);
				}
			}
			reader.close();
		}
		catch(IOException ioe) {
			// TODO: This should not be an FERInitException...
			throw new InitException(ioe);
		}
	}
	
	private String getBC(String s) {
		if(mFeatMap2 == null) {
			try {
				// "Strange... didn't save the BC map... problem seems fixed now,
				// must have been a serialization issue. Can probably take this out now
				loadMap();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		String retValue;
		if(s==null) {
			retValue = "-2";
		}
		else {
			String numericized = s.replaceAll("[0-9]", "ƞ");
			if(s.equals("an")) {
				s = "a";
			}
			if(!mFeatMap2.containsKey(numericized)) {
				String lower = numericized.toLowerCase();
				if(!mFeatMap2.containsKey(lower)) retValue = "-1";
				else retValue = mBCs.get(mFeatMap2.get(lower));
			}
			else retValue = mBCs.get(mFeatMap2.get(numericized));
		}
		return retValue;
	}
	
	private List<String> mBCs;
	private ChecksumMap<String> mFeatMap2 = new ChecksumMap<String>();
	
	private String getTag(TokenPointer ptr) {
		return getTag(ptr.tok);
	}
	
	private String getTag(Token tok) {
		String tag = tok.getPos();
		// WDTs as well?
		if(tag.equals("IN")||tag.equals("WRB")||tag.equals("WP")) tag = tag+getForm(tok);
		return tag;
	}
	
	private String getForm(TokenPointer ptr) {
		return getForm(ptr.tok);
	}
	
	private String getForm(Token tok) {
		String text = tok.getText();
		if("\"".equals(text)) {
			text = tok.getPos();
		}
		return	text == null ? text : text.toLowerCase();
	}
	
	private int length(TokenPointer ptr, Token[] kids) {
		int leftside = (kids[0] == mDummy.tok) ? ptr.tok.getIndex() : kids[0].getIndex();
		int rightside = (kids[1] == mDummy.tok) ? ptr.tok.getIndex() : kids[1].getIndex();
		return rightside-leftside;
	}
	
	private String getCpos(Token t) {
		String pos = t.getPos();
		return (pos.length() > 2 && !pos.endsWith("$")) ? pos.substring(0,2) : pos;
	}
	
	private final static int L2=0,L1=1,L0=2,R0=3,R1=4,R2=5;
	private final static int NTOK = 6;
	private final static Set<String> inbetweenWordsOfInterest = new HashSet<String>(Arrays.asList(new String[]{",","``",";",":","--","(",")"}));
	
	
	private final String[] tt = new String[]{"⼿","⽀","⽁","⽂","⽃"};
	private final String[] ww = new String[]{"⽄","⽕","⽖","⽗","⽘"};
	 private final String[] wt = new String[]{"⽙","⽚","⽛","⽜","⽭"};
	 private final String[] tw = new String[]{"⽮","⽯","⽰","⽱","⽲"};
	 private final String[] ttlclc = new String[]{"⽳","⽴","⾅","⾆","⾇"};
	 private final String[] ttlcrc = new String[]{"⾈","⾉","⾊","⾋","⾌"};
	 private final String[] ttrclc = new String[]{"⾝","⾞","⾟","⾠","⾡"};
	 private final String[] ttrcrc = new String[]{"⾢","⾣","⾤","⾵","⾶"};
	 private final int[] lseq = new int[]{L1,L1,L0,R0,L0};
	 private final int[] rseq = new int[]{R0,L0,R0,R1,R1};
	// unigram
	 private final String[] uniFrmPrefixes = new String[]{"⻯","⻰","⼍","⼎","⼏","⼐"};
	
	 private final String[] uniCposPrefixes = new String[]{"⼩","⼪","⼫","⼬","⼽","⼾"};
	// Childless indicators (nc_p)
	 private final String[] NCP = new String[]{"⺴","⺵","⺶","⺷","⺸","⺹"};
	// Length/childless
	 private final String[] lenP = new String[]{"⺺","⺻","⺼","⺽","⺾","⺿"};
	 private final String[] deltaP = new String[]{"⻀","⻑","⻒","⻓","⻔"};
	 private final String[] dt = new String[]{"⻕","⻖","⻗","⻘","⻩"};
	 private final String[] uniTagPrefixes = new String[]{"⼑","⼒","⼓","⼔","⼥","⼦"};
	// private final String[] = new String[]{"騚", "騛", "騜", "騝", "騞", "騟"};
	
	public void genFeats(Set<String> fts, ParseModel model, List<Token> tokens, TokenPointer ptr, List[] currentArcs) {
		if(mDummy == null) {
			mDummy = new TokenPointer(new Token(null, "na", 0), null, null);
		}
		// Word forms, tags, pos, cpos
		final String[] frm = new String[NTOK], tag = new String[NTOK];
		final String[] pos = new String[NTOK], cpos = new String[NTOK];
		
		final Token[][] kids = new Token[NTOK][];
		// Child pos
		final String[] lcpos = new String[NTOK], rcpos = new String[NTOK];
		// Child forms
		final String[] rcfrm = new String[NTOK];
		final String[] rccpos = new String[NTOK];
		
		final String[] bc = new String[NTOK];
		final String[] bcrc = new String[NTOK];
		
		//if(dumby == null) dumby = new TokenPointer(new Token(null, "na", 0), null, null);
		
		final TokenPointer[] tp = new TokenPointer[6];
		tp[L2] = (ptr.prev != null && ptr.prev.prev != null) ? ptr.prev.prev : mDummy;
		tp[L1] = (ptr.prev != null) ? ptr.prev : mDummy; 
		tp[L0] = ptr;
		tp[R0] = (ptr.next != null) ? ptr.next : mDummy;
		tp[R1] = (ptr.next != null && ptr.next.next != null) ? ptr.next.next : mDummy;
		tp[R2] = (ptr.next != null && ptr.next.next != null && ptr.next.next.next != null) ? ptr.next.next.next : mDummy;
		
		// Fill out arrays and features that are created for all tokens
		for(int i = 0; i < NTOK; i++) {
			frm[i] = getForm(tp[i]);
			tag[i] = getTag(tp[i]);
			pos[i] = tp[i].tok.getPos();
			cpos[i] = getCpos(tp[i].tok);
			kids[i] = getLRchildren(tp[i].tok, currentArcs); 
			lcpos[i] = kids[i][0].getPos();rcpos[i] = kids[i][1].getPos();
			rcfrm[i] = getForm(kids[i][1]);
			rccpos[i] = getCpos(kids[i][1]);
			bc[i] = getBC(frm[i]);
			bcrc[i] = getBC(rcfrm[i]);
			
			String posi = pos[i];
			// noun vs verb pos tag error possibility indicator
			if(posi.equals("NNS") || posi.equals("VBZ")) fts.add("⼩nz"+i);
			if(posi.equals("NN") || posi.equals("VB")) fts.add("⼩nv"+i);
			
			// vbd vs vbn POS tag error possibility indicator
			if(posi.equals("VBN") || posi.equals("VBD")) fts.add("⼑1"+i); else fts.add("⼑2"+i);
			
			if(posi.equals("DT")) {
				// is article indicator
				if(frm[i].equals("the") || frm[i].equals("a") || frm[i].equals("an")) {
					fts.add("⼓1"+i); 
				}
				else {
					fts.add("⼓2"+i);
				}
			}
			
			// Superlative indicator
			if(posi.equals("RBS") || posi.equals("JJS")) fts.add(i+"⼩s");
			// Comparative indicator
			if(posi.equals("RBR") || posi.equals("JJR")) fts.add(i+"⼩c");
			// Possessive pronoun indicator
			if(posi.equals("WP$") || posi.equals("PRP$")) fts.add(i+"⼩p");
			
			
			// Features
			fts.add(tag[i]+uniTagPrefixes[i]);
			fts.add(cpos[i]+uniCposPrefixes[i]);
			fts.add(bc[i]+uniTagPrefixes[i]);
			
			if(frm[i] != null) {
				fts.add(frm[i]+uniFrmPrefixes[i]);
			}
			//fts.add(soP[i]+(verbsPoses.contains(tag[i]) ? hasSoModifier(tp[i].tok, currentArcs) : false));
			fts.add(tag[i]+(kids[i][0] == mDummy.tok && kids[i][1] == mDummy.tok)+NCP[i]);	
			fts.add(tag[i]+lenP[i]+length(tp[i], kids[i]));
			
			
		}
		
		for(int i = 0; i < NTOK-1; i++) {
			boolean delta = Math.abs(tp[i].tok.getIndex()-tp[i+1].tok.getIndex())>1;
			fts.add(deltaP[i]+delta);
			fts.add(delta+"+"+tag[i]+dt[i]+tag[i+1]);
		}
		
		// Dependency relation features: note: l2 and r2 seem to be useful
		Set<String> deprelsl2 = addAllDeprels("㋟", tp[L2].tok, currentArcs);
		Set<String> deprelsl1 = addAllDeprels("⻪", tp[L1].tok, currentArcs);
		Set<String> deprelsl0 = addAllDeprels("⻫", tp[L0].tok, currentArcs);
		Set<String> deprelsr0 = addAllDeprels("⻬", tp[R0].tok, currentArcs);
		Set<String> deprelsr1 = addAllDeprels("⻭", tp[R1].tok, currentArcs);
		Set<String> deprelsr2 = addAllDeprels("㋠", tp[R2].tok, currentArcs);
		for(String s : deprelsl2) fts.add(s);
		for(String s : deprelsl1) fts.add(s);
		for(String s : deprelsl0) fts.add(s);
		for(String s : deprelsr0) fts.add(s);
		for(String s : deprelsr1) fts.add(s);
		for(String s : deprelsr2) fts.add(s);
		
		// in betweens (potentially big).. should be limited to a fixed window for theoretical
		// complexity reasons
		int l0i = tp[L0].tok.getIndex();
		int r0i = tp[R0].tok.getIndex();
		int rldiff = r0i-l0i;
		if(l0i != 0 && r0i != 0 && rldiff > 0) {
			final int minIndex = Math.max(l0i, r0i-10);
			for(int i = r0i; i > minIndex; i--) {
				Token t = tokens.get(i-1);
				String text = getForm(t);
				fts.add(getTag(t)+"⺎");
				if(inbetweenWordsOfInterest.contains(text)) {
					fts.add(text+"⺎");
				}
			}
		}
		
		// no features of interest for pos[L2].equals("CC") (yet :) )
		
		if("CC".equals(pos[L1])) {
			addL1CCFeats(kids[L1][1], fts, frm, pos, cpos, tag, rcpos, lcpos);
		}
		
		if("CC".equals(pos[L0])) {
			addL0CCFeats(kids[L0][1], fts, frm, pos, cpos, rcpos);
		}
		
		if("CC".equals(pos[R0])) {
			addR0CCFeats(kids[R0][1], fts, currentArcs, deprelsl2, deprelsl1, deprelsl0, frm, pos, cpos, rcpos, rccpos, rcfrm, tp);
		}
		
		if("CC".equals(pos[R1])) {
			addR1CCFeats(kids[R1][1], 
					     fts, currentArcs, deprelsl1, deprelsl0, deprelsr0, tag, frm, rcpos, rccpos, cpos, pos, rcfrm);
		}
		
		if("CC".equals(pos[R2])) {
			addR2CCFeats(kids[R2][1], 
					     fts, 
					     currentArcs, 
					     deprelsl0, 
					     deprelsr0, 
					     frm, 
					     rcfrm[R2], 
					     rccpos[R2], 
					     rcpos[R2], 
					     cpos, 
					     pos);
		}
		
		Arc r0whadvmod = getDeprel(tp[R0].tok, currentArcs, ParseConstants.WH_ADVERBIAL_DEP);
		if(r0whadvmod != null) {
			String prefix = getForm(r0whadvmod.getChild())+"鬵";
			fts.add(prefix+frm[L0]);fts.add(prefix+tag[L0]);
			fts.add(prefix+bc[L0]);
			fts.add(frm[L0]+"⿉");fts.add(tag[L0]+"鬻");fts.add(bc[L0]+"鬴");
		}
		Arc r1whadvmod = getDeprel(tp[R1].tok, currentArcs, ParseConstants.WH_ADVERBIAL_DEP);
		if(r1whadvmod != null) {
			String prefix = getForm(r1whadvmod.getChild())+"顁";
			fts.add(prefix+frm[R0]);fts.add(prefix+tag[R0]);
			fts.add(prefix+bc[R0]);
			fts.add(frm[R0]+"⿋");fts.add(tag[R0]+"魙");	fts.add(bc[R0]+"鬮");
		}
		final int numl0Deprels = deprelsl0.size();
		if(numl0Deprels > 1) {
			List<String> deprelsListl0 = new ArrayList<String>(deprelsl0);
			Collections.sort(deprelsListl0);
			for(int i = 0; i < numl0Deprels; i++) {
				String newFeat = "⿅"+deprelsListl0.get(i);
				fts.add("ᅄ"+tag[L0]+"+"+deprelsListl0.get(i));
				fts.add("ᅄ"+bc[L0]+"+"+deprelsListl0.get(i));
				for(int j = 0; j < numl0Deprels; j++) {
					if(i != j) {
						fts.add(newFeat + deprelsListl0.get(j));
					}
				}
			}
		}
		final int numr0Deprels = deprelsr0.size();
		if(numr0Deprels > 1) {
			List<String> deprelsListr0 = new ArrayList<String>(deprelsr0);
			Collections.sort(deprelsListr0);
			for(int i = 0; i < numr0Deprels; i++) {
				String newFeat = "⿆"+deprelsListr0.get(i);
				fts.add("ᄹ"+tag[R0]+"+"+deprelsListr0.get(i));
				fts.add("ᄹ"+bc[R0]+"+"+deprelsListr0.get(i));
				for(int j = 0; j < numr0Deprels; j++) {
					if(i != j) {
						fts.add(newFeat + deprelsListr0.get(j));
					}
				}
			}
		}
		for(String deprell0 : deprelsl0) {
			// l0 x r0
			String newFeat = deprell0+"⿁";
			fts.add(deprell0+tag[R0]+"⿇");
			fts.add(deprell0+bc[R0]+"⿇");
			String l0tdr0 = tag[L0]+"⿈";
			for(String deprelr0 : deprelsr0) {
				// l0t x dp(r0)
				fts.add(l0tdr0+deprelr0);
				fts.add(bc[L0]+"+"+deprelr0);
				// dp(l0) x dp(r0)
				fts.add(newFeat+deprelr0);
			}
			String newFeat2 = deprell0+"⿂";
			for(String deprell1 : deprelsl1) {
				// dp(l1) x dp(l0) 
				fts.add(newFeat2+deprell1);
			}
		}
		for(String deprelr1 : deprelsr1) {
			String prefix = deprelr1+"⿃";
			for(String deprelr0 : deprelsr0) {
				// dp(r0) x dp(r1)
				fts.add(prefix+deprelr0);
			}
		}

		if(r0i < tokens.size()-1) {
			String tag1 = getTag(tokens.get(r0i));
			String tag2 = getTag(tokens.get(r0i+1));
			//String tag3 = r0i < tokens.size()-2 ? getTag(tokens.get(r0i+2)) : "na";
			String twotags = tag1+"+"+tag2;
			fts.add("㑆r"+tag1);
			fts.add("㑆r"+twotags);
			//㑇
		}
		
		// Find new characters for X and Y
		fts.add("X"+tag[L0]+"+"+tag[R0]+"+"+bcrc[R0]);
		fts.add("Y"+tag[L0]+"+"+tag[R0]+"+"+bcrc[L0]);
		// coarse tags
		
		// Ambiguous L1,L2 cpos and bc feats 
		fts.add("⼧"+cpos[L2]);fts.add(bc[L2]+"⼧");
		fts.add("⼧"+cpos[L1]);fts.add(bc[L1]+"⼧");
		
		// BUG! Fix me (hmm... on second thought it doesn't look like a bug)
		fts.add("⼨"+cpos[R1]);fts.add(bc[R1]+"⼨");
		fts.add("⼨"+cpos[R2]);fts.add(bc[R2]+"⼨");
		
		if(!cpos[L2].equals("VB") && !cpos[L1].equals("VB") && 
				!cpos[R1].equals("VB") && !cpos[R2].equals("VB")) {
			if(!cpos[L0].equals("VB") && cpos[R0].equals("VB")) {
				fts.add("r0LonelyVerb");
			}
			else if(cpos[L0].equals("VB") && !cpos[R0].equals("VB")) {
				fts.add("l0LonelyVerb");
			}
		}
		
		// Might be useless....
		if(tag[L0].equals("JJ") || tag[L0].equals("NN") || tag[L0].equals("NNS") || tag[L0].equals("NNP") || tag[L0].equals("NNPS")) {
			if(hasTooModifier(tp[L0].tok, currentArcs)) fts.add("ᅃ");
		}
		// no unigram tag+child tag entries..... interesting
		
		
		for(int i = 0; i < NTOK-1; i++) {
			int lseqi = lseq[i], rseqi = rseq[i];
			String lf = frm[lseqi], rf = frm[rseqi], lt = tag[lseqi], rt = tag[rseqi];
			String t2 = lt+"+"+rt+"+";
			// tag-tag and word-word
			fts.add(tt[i]+t2); fts.add(lf+ww[i]+rf);
			// tag-word / word-tag
			fts.add(lf+wt[i]+rt); fts.add(rf+tw[i]+lt);
			fts.add(tt[i]+bc[lseqi]+"+"+bc[rseqi]);
			fts.add(t2+lcpos[lseqi]+ttlclc[i]+lcpos[rseqi]);
			fts.add(t2+lcpos[lseqi]+ttlcrc[i]+rcpos[rseqi]);
			fts.add(t2+rcpos[lseqi]+ttrclc[i]+lcpos[rseqi]);
			fts.add(t2+rcpos[lseqi]+ttrcrc[i]+rcpos[rseqi]);
		}
		// More tag-tag
		fts.add("⿌"+tag[L1]+"+"+tag[R1]);

		// ROOT-related features
		if(tag[L1].equals("na") && tag[R1].equals("na")) {
			fts.add("VR"+bc[L0]+"+"+bc[R0]);
			fts.add("⿍"+tag[L0]+"+"+tag[R0]);
		}
		
		addPrepositionFeats(fts, cpos, tag, frm, rcpos, rcfrm, bc, bcrc);
	}
	
	private void addL1CCFeats(Token kidL1_1,
							  Set<String> fts, 
			 				  String[] frm, 
			 				  String[] pos, 
			 				  String[] cpos,
			 				  String[] tag,
			 				  String[] rcpos,
			 				  String[] lcpos) {
		fts.add(frm[L1]+"⺒"+rcpos[L1]);
		fts.add("l1CChasChild"+(kidL1_1 == mDummy.tok));
		
		// May not hurt to change these equivalence features to be simple combo features
		// L0 CC L2 (matches of pos, cpos, frm)
		fts.add(pos[L0].equals(pos[L2])+"⿐"+tag[R0]);
		fts.add(cpos[L0].equals(cpos[L2])+"⿑"+tag[R0]);
		fts.add(frm[L0].equals(frm[L2])+"⿒"+tag[R0]);
		
		// R0 CC L2 (matches of pos, cpos, frm) AMBIGUOUS with above features - did I mean them to be???
		fts.add(pos[R0].equals(pos[L2])+"⿐"+tag[R0]);
		fts.add(cpos[R0].equals(cpos[L2])+"⿑"+tag[R0]);
		if(frm[R0] != null) fts.add(frm[R0].equals(frm[L2])+"⿒"+tag[R0]);
		
		// What is this feature for? Shouldn't it probably be rcpos[L1], not rcpos[L2]! Hmm...
		fts.add(tag[L2]+"+"+tag[L0]+"+"+tag[R0]+"+"+rcpos[L2]+"⺖"+lcpos[L2]);
			
		// EXTREMELY PRODUCTIVE.. achieves its goal.. and more(perhaps some not so good things?)
		fts.add("⺙"+tag[L2]+"+"+tag[L0]+"+"+tag[R0]);
	}
	
	private void addL0CCFeats(Token kidL0_1, Set<String> fts, String[] frm, String[] pos, String[] cpos, String[] rcpos) {
		fts.add(frm[L0]+"⺓"+rcpos[L0]);
		fts.add("l0HasChild"+(kidL0_1 == mDummy.tok));

		// ?/l2 CC/l1 ?/l0 ?/r0 ?/r1 ?/r2
		fts.add(pos[L2]+"+"+pos[L1]+"+"+pos[R0]);
		
		fts.add(cpos[L2]+"⺢"+cpos[R0]);
		
		fts.add(pos[R0].equals(pos[L1])+"⿔");fts.add(cpos[R0].equals(cpos[L1])+"頤");fts.add((frm[R0]!=null&&frm[R0].equals(frm[L1]))+"頤頤");
		fts.add(pos[R0].equals(pos[L2])+"⿓");fts.add(cpos[R0].equals(cpos[L2])+"頦");fts.add((frm[R0]!=null&&frm[R0].equals(frm[L2]))+"⿓⿓");
		
		fts.add(pos[R1].equals(pos[L1])+"⿕");fts.add(cpos[R1].equals(cpos[L1])+"⿕2");fts.add((frm[R1]!=null&&frm[R1].equals(frm[L1]))+"⿕3");
		fts.add(pos[R1].equals(pos[L2])+"々");fts.add(cpos[R1].equals(cpos[L2])+"々2");fts.add((frm[R1]!=null&&frm[R1].equals(frm[L2]))+"々3");
		
		fts.add(pos[R2].equals(pos[L1])+"〇");fts.add(cpos[R2].equals(cpos[L1])+"頣");fts.add((frm[R2]!=null&&frm[R2].equals(frm[L1]))+"頣3");
		fts.add(pos[R2].equals(pos[L2])+"〣");fts.add(cpos[R2].equals(cpos[L2])+"頡");fts.add((frm[R2]!=null&&frm[R2].equals(frm[L2]))+"頡3");
	}
	
	private void addR0CCFeats(Token kidR0_1, 
			Set<String> fts, 
			List[] currentArcs, 
			Set<String> deprelsl2, 
			Set<String> deprelsl1, 
			Set<String> deprelsl0, 
			String[] frm,
			String[] pos,
			String[] cpos,
			String[] rcpos,
			String[] rccpos,
			String[] rcfrm,
			TokenPointer[] tp) {
		fts.add(frm[R0]+"⺔"+rcpos[R0]);
		// r0 has child
		fts.add((kidR0_1 != mDummy.tok)+"頩");
		if(kidR0_1 != mDummy.tok) {
			Set<String> d = new HashSet<String>();
			Set<String> r0cDeprels = addAllDeprels("", kidR0_1, currentArcs);
			//Set<String> deprelsl2 = addAllDeprels("",  tp[L2].tok, currentArcs);
			for(String dep : r0cDeprels) {
				for(String l0deprel : deprelsl0) fts.add(dep+"㐲"+l0deprel);
				for(String l1deprel : deprelsl1) fts.add(dep+"㐁"+l1deprel);
				for(String l2deprel : deprelsl2) fts.add(dep+"㐴"+l2deprel);
			}
		}
		
		// ?/l2 ?/l1 Z/l0 CC/r0<-Z
		// encourage l0<-r0
		// GOOD FEATURES
		if(rcpos[R0].equals(pos[L0])) fts.add("ZC==");
		if(rccpos[R0].equals(cpos[L0])) fts.add("ZC~~");
		
		// X/l0 CC/r0<-X
		// encourage attaching CC now
		if(frm[L0].equals(rcfrm[R0])) fts.add("ZC11");
		
		// strong negative
		if(frm[L1] != null && frm[L1].equals(rcfrm[R0])) fts.add("ZC7");
		
		Token r0RCd = getDeterminer(kidR0_1, currentArcs);
		Token l0d = getDeterminer(tp[L0].tok, currentArcs);
		Token l2d = getDeterminer(tp[L2].tok, currentArcs);
		
		boolean isDefiniter0RCd = isDefinite(r0RCd);
		boolean isDefinitel0d = isDefinite(l0d);
		boolean isDefinitel2d = isDefinite(l2d);
		
		boolean reql0 = r0RCd!=null&&l0d!=null&&r0RCd.getText()!=null&&r0RCd.getText().equalsIgnoreCase(l0d.getText());
		boolean reql2 = r0RCd!=null&&l2d!=null&&r0RCd.getText()!=null&&r0RCd.getText().equalsIgnoreCase(l2d.getText());
		// ZC15:,16,17,18 // ONLY AFFECT cc
		// 'true' is POSITIVE for cc-right (good, same determiner=>they are a good match)
		if(reql0) fts.add("⺞"+reql0);
		// 'true' is VERY NEGATIVE for cc-right (good, next one has the same determiner=>wait a bit)
		if(reql2) fts.add("⺟"+reql2);
		// VERY NEGATIVE FOR cc-right (good, next one has same determiner but immediate left does not=>definitely wait)
		if(reql2 && !reql0) fts.add("⺠");
		
		// Affects many things, appears to be useful
		fts.add("⺡" + isDefinitel2d + "+"+isDefinitel0d+"+"+isDefiniter0RCd);
	}
	
	private void addR1CCFeats(Token kidsR1_1, 
		      Set<String> fts, 
		      List[] currentArcs, 
		      Set<String> deprelsl1,
		      Set<String> deprelsl0, 
		      Set<String> deprelsr0,
		      String[] tag,
		      String[] frm,
		      String[] rcpos,
		      String[] rccpos,
		      String[] cpos,
		      String[] pos,
		      String[] rcfrm) {
			fts.add(frm[R1]+"⺕"+rcpos[R1]);
			// r1 has child
			fts.add((kidsR1_1 != mDummy.tok) + "頴");
			if(kidsR1_1 != mDummy.tok) {
				Set<String> r1cDeprels = addAllDeprels("", kidsR1_1, currentArcs);
				for(String dep : r1cDeprels) {
					for(String r0deprel : deprelsr0) fts.add(dep+"㑀"+r0deprel);
					for(String l0deprel : deprelsl0) fts.add(dep+"㑁"+l0deprel);
					for(String l1deprel : deprelsl1) fts.add(dep+"㑃"+l1deprel);
				}
			}
			
			// X/l1 Y/l0 Z/r0 and/r1<-X
			// encourage attaching [Y<-Z]
			if(rcfrm[R1] != null && rcfrm[R1].equals(frm[L1])) fts.add("ZC10");
			
			// ?/l1 Y/l0 ?/r0 CC/r1<-Y
			// encourage l0<-r0
			// USEFUL FEATS
			if(rcpos[R1].equals(pos[L0])) fts.add("ZC==2");
			if(rccpos[R1].equals(cpos[L0])) fts.add("ZC~~2");
			
			// X/l0 U/r0 or/r1 Y/r2
			// X/l0 Y/r0 or/r1<-Y
			// SOMEWHAT USEFUL FEATS
			fts.add("ZC8"+frm[R0].equals(frm[R2]));
			fts.add("ZC9"+frm[R0].equals(rcfrm[R1]));
			
			// ****VERY PRODUCTIVE RULE***** MAYBE TOO MUCH SO... ROOM FOR IMPROVEMENT?
			fts.add(tag[L0]+"+"+tag[R0]+"+"+rcpos[R1]+"⺛"+tag[R2]);
			
			// A/l0 X/r0 or/r1 A/r2
			// A/l0 X/r0 or/r1<-A
			if(frm[L0].equals(frm[R2]))fts.add("ZC5");
			if(frm[L0].equals(rcfrm[R1]))fts.add("ZC6");
			
			// Very productive...
			fts.add(pos[L0]+"+"+pos[R0]+"⿏"+pos[R2]);
		
	}
	
	private void addR2CCFeats(Token kidsR2_1, 
						      Set<String> fts, 
						      List[] currentArcs, 
						      Set<String> deprelsl0, 
						      Set<String> deprelsr0,
						      String[] frm,
						      String rcfrmR2,
						      String rccposR2,
						      String rcposR2,
						      String[] cpos,
						      String[] pos) {
		if(kidsR2_1 != mDummy.tok) {
			Set<String> r2cDeprels = addAllDeprels("",  kidsR2_1, currentArcs);
			for(String dep : r2cDeprels) {
				for(String r0deprel : deprelsr0) fts.add(dep+"顂"+r0deprel);
				for(String l0deprel : deprelsl0) fts.add(dep+"顄"+l0deprel);
			}
			
			// ? ? . ? ? CC->x
			if(rcfrmR2.equals(frm[L0])) fts.add("ZC12frml0");
			// ? ? ? . ? CC->x
			if(rcfrmR2.equals(frm[R0])) fts.add("ZC12");
			// ? ? ? ? . CC->x
			if(rcfrmR2.equals(frm[R1])) fts.add("ZC12frm1");
			
			
			if(cpos[R1].equals(rccposR2)) {
				fts.add("dCCcpos1");
				if(pos[R1].equals(rcposR2)) {
					fts.add("dCCpos1");
				}
			}
			if(rccposR2.equals(cpos[R0])) {
				fts.add("dCCcpos0");
				if(rcposR2.equals(pos[R0])) {
					fts.add("dCCpos0");
				}
			}
			if(rccposR2.equals(cpos[L0])) {
				fts.add("dCCcposl0");
				if(rcposR2.equals(pos[L0])) {
					fts.add("dCCposl0");
				}
			}
		}
		else {
			fts.add("dCCr1");
		}
	}
	
	private void addPrepositionFeats(Collection<String> fts, String[] cpos, String[] tag, String[] frm, 
			String[] rcpos, String[] rcfrm, String[] bc, String[] bcrc) {
		if(tag[L0].startsWith("IN")) {
			fts.add(getSubstring(frm[L1], 4)+"+"+frm[L0]+"⾷"+rcpos[L0]);
			fts.add(frm[L0]+"+"+tag[L1]+"⾸"+rcfrm[L0]);
			fts.add("⾷"+bc[L1]+"+"+bc[L0]+"+"+bcrc[L0]);
			fts.add(tag[L1]+"+"+bc[L0]+"⾸"+bcrc[L0]);
		}
		if(tag[R0].startsWith("IN")) {
			// Ambiguous for L1 and L2
			fts.add(getSubstring(frm[L2], 4)+"+"+frm[R0]+"⾹"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L2]+"⾺"+rcfrm[R0]);
			fts.add(getSubstring(frm[L1], 4)+"+"+frm[R0]+"⾹"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L1]+"⾺"+rcfrm[R0]);
			
			fts.add(getSubstring(frm[L0], 4)+"+"+frm[R0]+"⾻"+rcpos[R0]);	fts.add(frm[R0]+"+"+tag[L0]+"⾼"+rcfrm[R0]);
			
			fts.add("⾻"+bc[L0]+"+"+bc[R0]+"+"+bcrc[R0]);	fts.add("⾼"+tag[L0]+"+"+bc[R0]+"+"+bcrc[R0]);
			
			if(!rcpos[R0].equals("na")) {
				fts.add(getSubstring(frm[L1], 4)+"+"+cpos[L0]+"ᅍ"+frm[R0]);
				fts.add(getSubstring(frm[L1], 4)+"+"+tag[L0]+"ᅎ"+frm[R0]);
				fts.add("haschildᅏ");
				fts.add(getSubstring(frm[L0], 4)+"+"+frm[R0]+"ᅐ"+tag[R1]);
				
				// Ambiguous
				fts.add(getSubstring(frm[L2], 4)+"⾹"+frm[R0]);	fts.add(tag[L2]+"⾺"+frm[R0]);
				fts.add(getSubstring(frm[L1], 4)+"⾹"+frm[R0]);	fts.add(tag[L1]+"⾺"+frm[R0]);
				
				fts.add(frm[L0]+"⾻"+frm[R0]);	fts.add(tag[L0]+"⾼"+frm[R0]);
			}
		}
		if(tag[R1].startsWith("IN")) {
			fts.add(getSubstring(frm[R0], 4)+"+"+frm[R1]+"⾽"+rcpos[R1]);	fts.add(frm[R1]+"+"+tag[R0]+"⾾"+rcfrm[R1]);
			fts.add(getSubstring(frm[L0], 4)+"+"+frm[R1]+"⾿"+rcpos[R1]);	fts.add(frm[R1]+"+"+tag[L0]+"⿀"+rcfrm[R1]);
			
			//fts.add("⾿"+bc[L0]+"+"+bc[R1]+"+"+bc[R1]);	
			//fts.add("⿀"+tag[L0]+"+"+bc[R1]+"+"+bc[R1]);
			// Was this supposed to have the bc of the right child instead?
			fts.add("⾿"+bc[L0]+"+"+bc[R1]);	
			fts.add(tag[L0]+"⿀"+bc[R1]);
			fts.add(bc[L0]+"⾿"+bc[R1]+"+"+bcrc[R1]);	
			fts.add(tag[L0]+"⿀"+bc[R1]+"+"+bcrc[R1]);
		}
	}
	
	private String getSubstring(String s, int size) {
		if(s == null) {
			return null;
		}
		else {
			return s.length() > 0 ? s.substring(0, Math.min(size, s.length())) : " ";
		}
	}
	
	
	
	private boolean hasTooModifier(Token t, List[] currentArcs) {
		boolean hasTooModifier = false;
		List<Arc> arcs = currentArcs[t.getIndex()];
		if(arcs != null) {
			for(Arc arc : arcs) {
				String text = arc.getChild().getText().toLowerCase();
				if(text.equals("too") || text.equals("enough")) {
					hasTooModifier = true;
					break;
				}
			}
		}
		return hasTooModifier;
	}
	
	private Arc getDeprel(Token t, List[] currentArcs, String deprelString) {
		Arc deprel = null;
		List<Arc> arcs = currentArcs[t.getIndex()];
		if(arcs != null) {
			for(Arc a : arcs) {
				if(a.getDependency().equals(deprelString)) {
					deprel = a;
					break;
				}
			}
		}
		return deprel;
	}
	
	
	// TODO: need to fix handling of possessives, arbitrary possessors aren't being identified as definite!
	private Set<String> definiteDeterminers = new HashSet<String>(Arrays.asList(new String[]{"the","these","those","this","that","'s","'","what","whose"}));
	private boolean isDefinite(Token t) {
		return (t == null) ? false : definiteDeterminers.contains(t.getText().toLowerCase()) || t.getPos().equals("PRP$");
	}
	private Token getDeterminer(Token t, List[] currentArcs) {
		Token determiner = null;
		List<Arc> arcs = currentArcs[t.getIndex()];
		int ti = t.getIndex();
		if(arcs != null) {
			for(Arc arc : arcs) {
				String arcDependency = arc.getDependency();
				if(arcDependency.equals(ParseConstants.DETERMINER_DEP) || arcDependency.equals(ParseConstants.POSSESSOR_DEP)) {
					Token child = arc.getChild();
					if(child.getIndex() < ti) {
						String childPos = child.getPos();
						if(childPos.endsWith("DT")) {
							determiner = child; break;
						}
						else if(childPos.equals("PRP$")||childPos.equals("WP$")) {
							determiner = child; break;
						}
						else {
							List<Arc> grandChildArcs = currentArcs[child.getIndex()];
							if(grandChildArcs != null) {
								for(Arc grandDependent : grandChildArcs) {
									if(grandDependent.getDependency().equals(ParseConstants.POSSESSIVE_MARKER)){
										determiner = grandDependent.getChild(); break;
									}
								}
							}
						}
					}
				}
			}
		}
		return determiner;
	}
	
	private final static Set<String> COMMON_RELATIVE_PRON_POSITIONS = new HashSet<String>(Arrays.asList(new String[]{ParseConstants.NOMINAL_SUBJECT_DEP,ParseConstants.DIRECT_OBJECT_DEP,ParseConstants.INDIRECT_OBJECT_DEP,ParseConstants.PREP_MOD_DEP}));
	private Set<String> addAllDeprels(String prefix, 
									 Token t, 
									 List[] tokenToArcs) {
		Set<String> additions = new HashSet<String>();
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		if(arcs != null) {
			String ldep = "l"+prefix;
			String rdep = "r"+prefix;
			int tIndex = t.getIndex();
			for(Arc arc : arcs) {
				String dep = arc.getDependency();
				Token child = arc.getChild();
				String childPos = child.getPos();
				String feat = (tIndex<arc.getChild().getIndex()?rdep:ldep)+dep;
				if(dep.equals(ParseConstants.PUNCTUATION_DEP)||dep.equals(ParseConstants.PREP_MOD_DEP)){
					feat = feat + getForm(child);
				}
				else if(dep.equals(ParseConstants.ADVERBIAL_DEP)) {
					feat = feat + getBC(getForm(child));
				}
				else if(dep.equals(ParseConstants.NOMINAL_SUBJECT_DEP)) {
					String childForm = getForm(child);
					if(childForm.equalsIgnoreCase("it")) {
						feat = feat + "it";
					}
				}
				if( (COMMON_RELATIVE_PRON_POSITIONS.contains(dep) &&
				  (childPos.equals("WP") || childPos.equals("WDT") || hasSecondLevelRelativePronoun(child, tokenToArcs)))) {
					additions.add(prefix+"⿎");
				}
				additions.add(feat);
			}
		}
		return additions;
	}
	
	private boolean hasSecondLevelRelativePronoun(Token t, List[] tokenToArcs) {
		boolean hasSecondLevelRelativePronoun = false;
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		if(arcs != null) {
			outer:
			for(Arc a : arcs) {
				String dependency = a.getDependency();
				if(dependency.equals(ParseConstants.POSSESSOR_DEP)) {
					if(a.getChild().getPos().equals("WP$")) {
						hasSecondLevelRelativePronoun = true;
					}
					break;
				}
				else if(dependency.equals(ParseConstants.PREP_MOD_DEP)) {
					List<Arc> subarcs = tokenToArcs[a.getChild().getIndex()];
					if(subarcs != null) {
						for(Arc subarc : subarcs) {
							String subArcChildPos = subarc.getChild().getPos();
							if(subArcChildPos.equals("WP") || subArcChildPos.equals("WDT")) {
								hasSecondLevelRelativePronoun = true;
								break outer;
							}
						}
					}
				}
			}
		}
		return hasSecondLevelRelativePronoun;
	}
	
	private Token[] getLRchildren(Token t, List[] tokenToArcs) {
		Token[] result = new Token[]{mDummy.tok, mDummy.tok};
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		Arc leftmostArc = null;
		Arc rightmostArc = null;
		if(arcs != null && arcs.size() > 0) {
			// if we knew Arcs were sorted, we could avoid this
			final int tokenIndex = t.getIndex();
			for(Arc arc : arcs) {
				final int arcChildIndex = arc.getChild().getIndex();
				if(arcChildIndex < tokenIndex) {
					if((leftmostArc == null || arcChildIndex < leftmostArc.getChild().getIndex())) {
						// Farther left than leftmost child
						leftmostArc = arc;
					}
				}
				else {
					if((rightmostArc == null || arcChildIndex > rightmostArc.getChild().getIndex())) {
						// Farther right than rightmost child
						rightmostArc = arc;
					}
				}
			}
		}
		if(leftmostArc != null) {
			result[0] = leftmostArc.getChild();
		}
		if(rightmostArc != null) {
			result[1] = rightmostArc.getChild();
		}
		return result;
	}
	
	@Override
	public int getContextWidth() {
		return 6;
	}
	
	
}