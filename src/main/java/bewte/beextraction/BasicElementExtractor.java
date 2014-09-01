package bewte.beextraction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.Annotation;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.LocationAnnotation;
import tratz.runpipe.annotations.OrganizationAnnotation;
import tratz.runpipe.annotations.PersonAnnotation;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.annotations.Token.Arc;
import tratz.runpipe.impl.EndPointImpl;
import tratz.runpipe.util.RunpipeUtils;
import tratz.util.TreebankConstants;

import bewte.BEConstants;
import bewte.annotations.CDAnnotation;
import bewte.annotations.CanonicalizingAnnotation;
import bewte.annotations.DateAnnotation;

public class BasicElementExtractor extends EndPointImpl {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDirectory;
	
	
	@Override
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDirectory = new File(params.get(PARAM_OUTPUT_DIR));
	}

	private int mDirectoryCount;
	private int mCurrentDirectoryCount;
	private File mCurrentDirectory;
	public final static int MAX_PER_DIR = 200;
	
	private class Entry implements Comparable<Entry>{
		private String mType;
		private List<Annotation> mAnnots = new ArrayList<Annotation>();
		public Entry(String type) {
			mType = type;
		}
		public Entry(String type, Token ... tokens) {
			mType = type;
			for(Token token : tokens) {
				add(token);
			}
		}
		public Entry(String type, List<Annotation> tokens) {
			mType = type;
			mAnnots = tokens;
		}
		public void add(Annotation token) {
			mAnnots.add(token);
		}
		
		public int compareTo(Entry other) {
			return mType.compareTo(other.mType);
		}
		
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(0);
		format.setMinimumIntegerDigits(8);
		
		mCurrentDirectoryCount++;
		if(mCurrentDirectoryCount > MAX_PER_DIR || (mDirectoryCount == 0 && mCurrentDirectoryCount == 1)) {
			mCurrentDirectoryCount = 0;
			mCurrentDirectory = new File(mOutputDirectory, format.format(mDirectoryCount));
			mCurrentDirectory.mkdirs();
			mDirectoryCount++;
		}
		
		PrintWriter writer = null;
		try {
			Set<String> centerLemmaSet = new HashSet<String>();
			Set<String> rightLemmaSet = new HashSet<String>();
			Set<String> leftLemmaSet = new HashSet<String>();
			String uri = doc.getUri();
			writer = new PrintWriter(new FileWriter(new File(mCurrentDirectory,uri.substring(uri.lastIndexOf('/')+1))));
			List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
			List<Token> tokens = (List<Token>)doc.getAnnotationList(Token.class);
			if(sentences != null && tokens != null) {
				for(Sentence sentence : sentences) {
					List<Token> sentenceTokens = RunpipeUtils.getSublist(sentence, tokens);
					// Why is this code here? Looks like a temporary workaround that no longer
					// serves a purpose.
					for(Token t : sentenceTokens) {
						Token.Arc arc = t.getParentArc();
						if(arc != null && !arc.getHead().getDependentArcs().contains(arc)) {
							arc.getHead().addDependent(arc);
						}
					}
				}
				
				
				for(Sentence sentence : sentences) {
					Set<Entry> entries = new HashSet<Entry>();
					List<Token> sentenceTokens = RunpipeUtils.getSublist(sentence, tokens);
					Collections.sort(sentenceTokens);
					for(Token t : sentenceTokens) {
						Token.Arc parentArc = t.getParentArc();
						String tokenDependency = parentArc == null ? null : parentArc.getDependency();
						Token tokParent = parentArc == null ? null : parentArc.getHead();
						String pos = t.getPos();
						if(pos.matches("NN|NNS|NNP|NNPS|CD|\\$|RBS|RBR")) {
							//if(!(pos.matches("NNPS?") && "nn".equals(tokenDependency) && 
							 //  tokParent!=null && tokParent.getPos().matches("NNPS?"))) {
								entries.add(new Entry("r1", t));	
							//}
						}
						else if(pos.matches("VB.*") 
								&& !"aux".equals(tokenDependency)//
								&& !"auxpass".equals(tokenDependency)
								&& !t.getAnnotText().toLowerCase().matches("be|am|are|was|were|is|'re|'m|'s|been|being")) {
							Token prt = null;
							List<Arc> dependentArcs = t.getDependentArcs();
							if(dependentArcs != null) {
								for(Arc childArc : dependentArcs) {
									if(childArc.getDependency().equals("prt")) {
										prt = childArc.getChild(); 
										break;
									}
								}
							}
							if(prt == null) {
								entries.add(new Entry("r2", t));
							}
							else {
								entries.add(new Entry("r3", t, prt));
							}
						}
						else if(pos.matches("JJ.*")) {
							entries.add(new Entry("r4", t));
						}
						if(tokenDependency == null) { // ROOT
							//System.err.println("odd");
						}
						/*
						 bad idea
						 else if(tokenDependency.equals("aux")) {
							// not really a r45!
							entries.add(new Entry("r45",t, tokParent));
						}*/
						else if(tokenDependency.equals("num") || tokenDependency.equals("amod")
								) {
							entries.add(new Entry("r5",t, tokParent));
						}
						else if(tokenDependency.equals("poss")) {
							if(t.getPos().equals("PRP$")) {
								entries.add(new Entry("r6", t, tokParent));
							}
							else if(t.getPos().equals("WP$")) {
								Arc gparentArc = tokParent.getParentArc();
								if(gparentArc != null) {
									if("rcmod".equals(gparentArc.getDependency())) {
										entries.add(new Entry("r6", gparentArc.getHead(), tokParent));
									}
									else {
										Arc ggparentArc = gparentArc.getHead().getParentArc();
										if(ggparentArc != null) {
											if("rcmod".equals(ggparentArc.getDependency())) {
												entries.add(new Entry("r6", ggparentArc.getHead(), tokParent));
											}
										}
									}
								}
							}
							else {
								entries.add(new Entry("r7", t, tokParent));
							}
						}
						else if(tokenDependency.equals("nn") ) {//&& !t.getPos().matches("NNP.*")
							entries.add(new Entry("r8",t , tokParent));
						}
						else if(tokenDependency.equals("appos")) {
							entries.add(new Entry("r10",t,tokParent));
						}
						else if(tokenDependency.matches("nsubj.*|csubj.*|expl") 
								&& !t.getPos().matches("WP|WDT|IN")) {
							if(tokParent.getPos().matches("VBN")) {
								boolean hasBeAux = false;
								boolean hasHaveAux = false;
								List<Arc> dependents = tokParent.getDependentArcs();
								if(dependents != null) {
								for(Arc siblingArc : dependents) {
									if(siblingArc.getDependency().equals("aux") && siblingArc.getChild().getAnnotText().toLowerCase().matches("be|is|was|were|are|am|been|'s|'re")) {
										hasBeAux = true; 
									}
									else if(siblingArc.getDependency().equals("aux") && siblingArc.getChild().getAnnotText().toLowerCase().matches("have|had|has|'ve")) {
										hasHaveAux = true; 
									}
								}
								}
								if(hasBeAux || tokenDependency.endsWith("pass")) {
									entries.add(new Entry("r23", tokParent, t));
								}
								else if(hasHaveAux) {
									entries.add(new Entry("r11", t, tokParent));
								}
								else {
									// may be bad
									entries.add(new Entry("r24", tokParent, t));
								}
							}
							else {
								//Token 
								if(!t.getPos().equals("DT") || !"rcmod".equals(tokParent)) {
									entries.add(new Entry("r11", t, tokParent));
								}
							}
						}
						else if(tokenDependency.equals("prep") && t.getAnnotText().toLowerCase().equals("by") && tokParent.getPos().equals("VBN")) {
							Token pcomp = getFirstChild(t, "pcomp|pobj");
							if(pcomp != null) {
								entries.add(new Entry("r13", pcomp, tokParent));
							}
						}
						else if(tokenDependency.equals("rcmod")) {
							if(!t.getPos().equals("VBN")) {
								entries.add(new Entry("r14", tokParent, t));
							}
							else if(t.getPos().equals("VBN")) {
								boolean hasBeAux = false;
								List<Arc> dependents = t.getDependentArcs();
								if(dependents != null) {
								for(Arc childarc : dependents) {
									if(childarc.getDependency().equals("auxpass") || (childarc.getDependency().equals("aux") && childarc.getChild().getAnnotText().toLowerCase().matches("is|was|were|are|am|been|'s"))) {
										hasBeAux = true;
									}
								}
								}
								entries.add(new Entry(hasBeAux ? "r18" : "r15", t, tokParent));
							}
							List<Arc> dependentArcs = t.getDependentArcs();
							if(dependentArcs != null) {
							for(Arc childArc : dependentArcs) {
								if(childArc.getChild().getPos().equals("CC")) {
									List<Arc> grandChildArcs = childArc.getChild().getDependentArcs();
									if(grandChildArcs != null) {
									for(Arc gchildArc : grandChildArcs) {
										if(gchildArc.getChild().getPos().startsWith("VB")) {
											boolean hasSubj = false;
											List<Arc> greatGrandArcs = gchildArc.getChild().getDependentArcs();
											if(greatGrandArcs != null) {
												for(Arc ggchildArc : greatGrandArcs) {
													if(ggchildArc.getDependency().matches("nsubj.*|csubj.*|expl"))  {
														hasSubj = true; break;
													}
												}
											}
											if(!hasSubj) {
												entries.add(new Entry("r16", gchildArc.getChild(), tokParent));
											}
										}
									}
									}
								}
							}
							}
						}
						else if(tokenDependency.matches("dobj|iobj|objcomp")) {
							entries.add(new Entry("r21", tokParent, t));//& r22
						}
						else if(tokenDependency.equals("cop")) {
							Token parent = tokParent;
							Token subj = null;
							List<Arc> childArcs = parent.getDependentArcs();
							if(childArcs != null) {
								for(Arc childArc : childArcs) {
									if(childArc.getDependency().matches("nsubj|csubj|expl")) {
										subj = childArc.getChild();
										break;
									}
								}
							}
							if(subj != null /*&& !t.getPos().matches("JJ.*")*/) {
								// 25 is for ADJPs
								entries.add(new Entry( "r47", t, subj));
							}
							if(t.getPos().matches("JJ.*") ) {
								entries.add(new Entry("r25", tokParent, t));
							}
							if(t.getPos().matches("NN.*|CD")) {
								entries.add(new Entry("r21", tokParent, t));
							}
							
						}
						else if(tokenDependency.equals("prep")) {
							Token pcomp = getFirstChild(t, "pobj|pcomp");
							if(pcomp != null) {
								if(pcomp.getPos().matches("WP|WDT")) {
									Arc grandparentArc = tokParent.getParentArc();
									if(grandparentArc != null) {
										if(grandparentArc.getDependency().matches("rcmod|infmod")) {
											entries.add(new Entry("r35", tokParent, t, grandparentArc.getHead()));
										}
									}
								}
								else /*if(!pcomp.getPos().equals("WP") || !tokParent.getPos().equals("DT"))*/ {
									// 27, 30,31,32,33,34,37,38
									// r36?
									entries.add(new Entry("r26", tokParent, t, pcomp));
									Token cc = getFirstChild(pcomp, "cc");
									if(cc != null) {
										Token conj = getFirstChild(cc, "conj");
										if(conj != null) {
											entries.add(new Entry("r30", tokParent, t, conj));
										}
									}
								}
							}
						}
						else if(tokenDependency.equals("ccomp")) {
							Token whadvmod = getFirstChild(t, "whadvmod");
							if(whadvmod != null) {
								// r41, r42
								entries.add(new Entry("r40", tokParent, whadvmod, t));
							}
							else {
								// not really 46 or 52
								entries.add(new Entry("r46", tokParent, t));
							}
						}
						else if(tokenDependency.equals("infmod")) {
							entries.add(new Entry("r46",t, tokParent));	// not really 46 or 52....
						}
						else if(tokenDependency.equals("measure")) {	
							entries.add(new Entry("r5",t,tokParent));// not really 5...
						}
						else if(tokenDependency.equals("xcomp")) {
							// may not be 44 if one or both of child and parent are not verbs
							entries.add(new Entry("r44",tokParent,t));
						}
						else if(tokenDependency.equals("advcl")) {
							Token mark = getFirstChild(t, "mark|whadvmod");
							if(mark != null) {
								// r41
								entries.add(new Entry("r42",tokParent,mark,t));
							}
							else {
								// Should be careful of pseudopreps...
								boolean alreadyCounted = false;
								if(t.getPos().equals("VBG") && !t.getAnnotText().toLowerCase().matches("following|including|according")) {
									Token subj = getFirstChild(t, "nsubj.*|csubj.*|expl");
									if(subj == null) {
										Token parentSubj = getFirstChild(tokParent, "nsubj.*|csubj.*");//|expl
										if(parentSubj != null) {
											entries.add(new Entry("r50",parentSubj,t));
											alreadyCounted = true;
										}
									}
								}
								// should this be an 'else'?
								// r44 ?
								if(!alreadyCounted) {
									entries.add(new Entry("r44", tokParent, t));
								}
							}
						}
						else if(tokenDependency.matches("purpcl|sufcomp")) {
							// r44 ?
							entries.add(new Entry("r44",tokParent, t));
						}
						else if(tokenDependency.matches("advmod|neg|tmod") && tokParent.getPos().startsWith("VB")) {
//							// r45?
							entries.add(new Entry("r46", tokParent, t));
						}
						else if(tokenDependency.equals("partmod") && t.getAnnotText().toLowerCase().matches("entitled|titled|named|called|labeled|designated|baptized|baptised|christend|termed|styled|dubbed|nicknamed|tagged")) {
							Token dobj = getFirstChild(t, "dobj");
							if(dobj != null) {
								entries.add(new Entry("r48", tokParent, dobj));
							}
						}
						else if(tokenDependency.equals("partmod")) {
							if(t.getPos().matches("VBN")) {
								entries.add(new Entry("r24",t,tokParent));
							}
							else if(t.getPos().matches("VBG")) {
								entries.add(new Entry("r12",tokParent, t));
							}
						}
						/*else if(tokenDependency.matches("number|prt|postloc|complm|infmark|combo|ccinit|conj|cc|dep|possessive|punct|det|aux|pobj|predet|parataxis|mark|pobj|pcomp")) {
							// don't care
						}
						else if(tokenDependency.matches("quantmod")) {
							System.err.println(tokenDependency+":"+t.getAnnotText()+":"+tokParent.getAnnotText());
						}
						else {
							System.err.println(tokenDependency);
						}*/

					}
					
					// Replace with overlapping tokens
					List<Entry> modSet = new ArrayList<Entry>();
					for(Entry entry : entries) {
						List<Annotation> newList = new ArrayList<Annotation>();
						for(Annotation annot : entry.mAnnots) {
							Annotation newAnnot = replaceWithOverlappingAnnotations(doc, annot);
							if(!newList.contains(newAnnot)) {
								newList.add(newAnnot);
							}
						}
						modSet.add(new Entry(entry.mType, newList));
					}
					Collections.sort(modSet);
					// Remove duplicate entries if they exist (could happen after replacement)
					int numEntries = modSet.size();
					List<Entry> entriesToRemove = new ArrayList<Entry>();
					for(int i = 0; i < numEntries; i++) {
						Entry e1 = modSet.get(i);
						for(int j = i+1; j < numEntries; j++) {
							Entry e2 = modSet.get(j);
							if(e1.mAnnots.size() == e2.mAnnots.size()) {
								Set<Annotation> set1 = new HashSet<Annotation>(e1.mAnnots);
								Set<Annotation> set2 = new HashSet<Annotation>(e2.mAnnots);
								set1.removeAll(set2);
								if(set1.size() == 0) {
									entriesToRemove.add(e2);
								}
							}
						}
					}
					modSet.removeAll(entriesToRemove);
					
					Map<String, List<Entry>> entryMap = new HashMap<String, List<Entry>>();
					
					for(Entry entry : modSet) {
						List<Entry> set = entryMap.get(entry.mType);
						if(set == null) {
							entryMap.put(entry.mType, set = new ArrayList<Entry>());
						}
						set.add(entry);
					}
					List<String> keys = new ArrayList<String>(entryMap.keySet());
					Collections.sort(keys, new Comparator<String>() {
						public int compare(String s1, String s2) {
							int i1 = Integer.parseInt(s1.substring(1));
							int i2 = Integer.parseInt(s2.substring(1));
							return i1-i2;
						}
					});
					
					writer.println("#Sentence: " + sentence.getAnnotText());
					writer.print("#");
					for(int i = 0; i < sentenceTokens.size(); i++) {
						Token tok = sentenceTokens.get(i);
						Arc parentArc = tok.getParentArc();
						String dep = parentArc == null ? null : parentArc.getDependency();
						writer.print(tok.getAnnotText() + "/" + tok.getPos()+"/"+dep+"/"+(dep==null?"0":parentArc.getHead().getAnnotText()) +" ");
					}
					writer.println();
					for(String key : keys) {
						List<Entry> annotSetList = entryMap.get(key);
						if(annotSetList != null && annotSetList.size() > 0) {
							writer.println("#Rule:" + key);
							for(int i = 0; i < annotSetList.size(); i++) {
								List<Annotation> annots = new ArrayList<Annotation>(annotSetList.get(i).mAnnots);
								int numAnnots = annots.size();
								writer.print(key);
								for(int j = 0; j < numAnnots; j++) {
									Annotation annot = annots.get(j);
									Set<String> lemmaSet = null;
									if(j == 0) {
										lemmaSet = leftLemmaSet;
									}
									else if(j == 1 && numAnnots == 3) {
										lemmaSet = centerLemmaSet;
									}
									else if(j == 2 && numAnnots == 3) {
										lemmaSet = rightLemmaSet;
									}
									else if(j == 1 && numAnnots == 2) {
										lemmaSet = rightLemmaSet;
									}
									else if(numAnnots == 4) {
										lemmaSet = centerLemmaSet;
									}
									
									String annotText = annot.getAnnotText();
									String type = null;
									if(annot instanceof Token) {
										type = ((Token)annot).getPos();
									
										Token tok = (Token)annot;
										lemmaSet.add(tok.getLemma());
									}
									else {
										type = annot.getClass().getSimpleName();
									}
									if(annot instanceof CanonicalizingAnnotation) {
										annotText = ((CanonicalizingAnnotation)annot).getCanonicalString();
										type = ((CanonicalizingAnnotation)annot).getCanonicalType();
									}
									
									// may need to do more lowercase-izing than this
									if(TreebankConstants.CLOSED_CLASS_TAGS.contains(type) || TreebankConstants.VERB_LABELS.contains(type)) {
										annotText = annotText.toLowerCase();
									}
									
									
									String[] parts = annotText.split("\\s+");
									//if(parts.length >= 1) {
										String lastPart = parts[parts.length-1];
										
										// Strip off ending periods
										int periodIndex = lastPart.indexOf('.');
										if(periodIndex == lastPart.length()-1 && periodIndex != 0) {
											annotText = annotText.substring(0, periodIndex);
										}
										writer.print(BEConstants.BE_SEPARATOR_CHAR + annotText + BEConstants.BE_SEPARATOR_CHAR + type);
										lemmaSet.add(annotText);
									/*}
									else {
										System.err.println("Empty token? Why? " + annot.getStart() + " " + annot.getEnd());
										String docText = doc.getText();
										docText = docText.substring(0, annot.getStart()) + "*%*" + docText.substring(annot.getStart(), docText.length());
										System.err.println(docText);
									}*/
									
										
								}
								writer.println();
							} // end for
							writer.println();
						} // end if
					} // end outer for
				}
			}
			// This stuff should be removed now that WordNet access is so much faster
			writer.print("@lemmas-left\t");
			for(String lemma : leftLemmaSet) {
				writer.print(lemma);writer.print('\t');
			}
			writer.println();
			writer.print("@lemmas-right\t");
			for(String lemma : rightLemmaSet) {
				writer.print(lemma);writer.print('\t');
			}
			writer.println();
			writer.print("@lemmas-center\t");
			for(String lemma : centerLemmaSet) {
				writer.print(lemma);writer.print('\t');
			}
		}
		catch(Exception e) {
			throw new ProcessException(e);
		}
		writer.close();
	}
	
	private Token getFirstChild(Token t, String depend) {
		Token comp = null;
		List<Arc> dependentArcs = t.getDependentArcs();
		if(dependentArcs != null) {
			for(Arc childArc : t.getDependentArcs()) {
				if(childArc.getDependency().matches(depend)) {
					comp = childArc.getChild();break;
				}
				else if(childArc.getDependency().matches("combo")) {
					comp = getFirstChild(childArc.getChild(), depend);
					if(comp != null) break;
				}
			}
		}
		return comp;
	}
	
	@Override
	public void batchFinished() {

	}
	
	private static Annotation findOverlappingAnnot(Annotation in, List<? extends Annotation> list) {
		if(list != null) {
			int begin = in.getStart();
			int end = in.getEnd();
			for(Annotation annot : list) {
				int annotBegin = annot.getStart();
				int annotEnd = annot.getEnd();
				if(annotBegin <= begin && annotEnd >= end) {
					return annot;
				}
			}
		}
		return null;
	}
	
	public Annotation replaceWithOverlappingAnnotations(TextDocument doc, Annotation annot) {
		Annotation newAnnot = annot;
		int newLength = 0;
		for(Class c : new Class[]{OrganizationAnnotation.class, PersonAnnotation.class, LocationAnnotation.class, CDAnnotation.class, DateAnnotation.class}) {
			List<Annotation> annots = (List<Annotation>)doc.getAnnotationList(c);
			Annotation overlappingAnnot = findOverlappingAnnot(annot, annots);
			if(overlappingAnnot != null) {
				int overlappingAnnotLength = overlappingAnnot.getEnd()-overlappingAnnot.getStart();
				if(overlappingAnnotLength > newLength) {
					newAnnot = overlappingAnnot;
					newLength = overlappingAnnotLength;
				}
			}
		}
		return newAnnot;
	}
	
}