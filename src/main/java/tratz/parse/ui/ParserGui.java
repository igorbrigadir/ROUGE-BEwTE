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

package tratz.parse.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import tratz.featgen.MultiStepFeatureGenerator;
import tratz.jwni.WordNet;
import tratz.ml.ClassScoreTuple;
import tratz.ml.LinearClassificationModel;
import tratz.parse.NLParser;
import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.ml.ParseModel;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;
import tratz.pos.featgen.PosFeatureGenerator;

public class ParserGui extends JFrame {
	
	private final static Dimension DEFAULT_SIZE = new Dimension(500,500);
	
	private JButton mParseButton = new JButton("Parse");
	private JButton mParseAndDisplay = new JButton("Display");
	private JButton mClear = new JButton("Clear");
	private JPanel mRightPanel = new JPanel(new GridBagLayout());
	private JPanel mLeftPanel = new JPanel(new BorderLayout());
	private JTextField mInputField = new JTextField();
	private JTextArea mOutputArea = new JTextArea();
	private JScrollPane mScrollPane = new JScrollPane(mOutputArea);
	
	private List<String> mHistory = new ArrayList<String>();
	private int mHistoryIndex = 0;
	
	private PosFeatureGenerator mFeatGen;
	private LinearClassificationModel mDecMaker;
	private NLParser mParser;
	private MultiStepFeatureGenerator mPsdFeatGen;
	private MultiStepFeatureGenerator mNNFeatGen;
	private Map<String, LinearClassificationModel> mPrepModels;
	
	public ParserGui(NLParser parser, 
					 PosFeatureGenerator featGen, 
					 LinearClassificationModel decMaker, 
					 MultiStepFeatureGenerator psdFeatGenerator, 
					 MultiStepFeatureGenerator nnFeatGenerator,
					 Map<String, LinearClassificationModel> prepModels) {
		mParser = parser;
		mFeatGen = featGen;
		mDecMaker = decMaker;
		mPsdFeatGen = psdFeatGenerator;
		mNNFeatGen = nnFeatGenerator;
		mPrepModels = prepModels;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(DEFAULT_SIZE);
		setTitle("Parser GUI");
		createLayout();
		createListeners();
		mInputField.setFont(new Font(mInputField.getFont().getName(), 0, 16));
		mOutputArea.setFont(new Font(mOutputArea.getFont().getName(), 0, 16));
	}
	
	private void createLayout() {
		Container contentPane = getContentPane();
		contentPane.add(mLeftPanel, BorderLayout.CENTER);
		contentPane.add(mRightPanel, BorderLayout.EAST);
		mLeftPanel.add(mInputField, BorderLayout.NORTH);
		mLeftPanel.add(mScrollPane, BorderLayout.CENTER);
		mRightPanel.add(mParseButton, new GridBagConstraints(0, 0, 1, 1, 100.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(30, 5, 5, 5), 0, 0));
		mRightPanel.add(mParseAndDisplay, new GridBagConstraints(0, 1, 1, 1, 100.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		mRightPanel.add(mClear, new GridBagConstraints(0, 2, 1, 1, 100.0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		mRightPanel.add(new JPanel(), new GridBagConstraints(0, 3, 1, 1, 100.0, 100.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}
	
	private void createListeners() {
		mInputField.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyPressed(KeyEvent e) {
				//System.err.println("Got one: " + e.getKeyCode());
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					mHistoryIndex++;
					if(mHistoryIndex < mHistory.size()) {
						mInputField.setText(mHistory.get(mHistoryIndex));
					}
					else {
						mHistoryIndex = mHistory.size()-2;
						//mInputField.setText("");
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					mHistoryIndex--;
					if(mHistoryIndex > 0 && mHistoryIndex < mHistory.size()) {
						mInputField.setText(mHistory.get(mHistoryIndex));
					}
					else {
						mHistoryIndex = 0;
						mInputField.setText("");
					}
				}
			}
		});
		mInputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parseAction(e);
			}
		});
		mParseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parseAction(e);
			}
		});
		mParseAndDisplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mParseAndDisplay_actionPerformed(e);
			}
		});
		mClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mClear_actionPerformed(e);
			}
		});
	}
	
	
	
	private void parseAction(ActionEvent e) {
		try {
			String text = mInputField.getText();
			if(!text.trim().equals("")) {
				String[] tokenized = text.split("\\s+");
				List<Token> tokens = new ArrayList<Token>();
				int index = 1;
				for(String s : tokenized) {
					tokens.add(new Token(s, index++));
				}
			
				posTag(tokens);
			
				Parse parse = mParser.parseSentence(new Sentence(tokens));
				List[] tokenToArcs = parse.getDependentArcLists();
				Arc[] tokenToHead = parse.getHeadArcs();
				
				performPrepositionDisambiguation(tokens, parse);
				
				String outputString = createOutputString(tokens, parse);
				mOutputArea.setText(mOutputArea.getText()+outputString);
				mHistory.add(0, text);
				mHistoryIndex = -1;
			}
			mInputField.setText("");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void mParseAndDisplay_actionPerformed(ActionEvent e) {
		try {
			String outfilename = "monkey.png";
			String text = mInputField.getText();
			if(!text.trim().equals("")) {
				List<Token> tokens = createTokens(text);
				posTag(tokens);
				Parse parse = mParser.parseSentence(new Sentence(tokens));
				String outputString = createOutputString(tokens, parse);
				
				// Update the display ares
				mOutputArea.setText(mOutputArea.getText()+outputString);
				mHistory.add(0, text);
				mHistoryIndex = -1;
				
				// Create an image
				createImage(parse, outfilename, true);
				
				// Run a program to display that image
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(new String[]{"eog", outfilename});
				
			}
			mInputField.setText("");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	private List<Token> createTokens(String text) {
		String[] tokenized = text.split("\\s+");
		List<Token> tokens = new ArrayList<Token>(tokenized.length);
		int index = 1;
		for(String s : tokenized) {
			tokens.add(new Token(s, index++));
		}
		return tokens;
	}
	
	private void posTag(List<Token> tokens) {
		final int numTokens = tokens.size();
		for(int j = 0; j < numTokens; j++) {
			Set<String> feats = mFeatGen.getFeats(tokens, j);
			ClassScoreTuple[] classRankings = mDecMaker.getDecision(feats);
			Token t = tokens.get(j);
			t.setPos(classRankings[0].clazz);
		}
	}
	
	private void performPrepositionDisambiguation(List<Token> tokens, Parse parse) {
		for(Arc arc : parse.getHeadArcs()) {
			if(arc != null && ParseConstants.PREP_MOD_DEP.equals(arc.getDependency())) {
				LinearClassificationModel prepModel = mPrepModels.get(arc.getChild().getText().toLowerCase());
				if(prepModel != null) {
					Set<String> feats = mPsdFeatGen.generateFeatures(tokens, parse, arc.getChild().getIndex()-1);
					ClassScoreTuple[] rankings = prepModel.getDecision(feats);
					arc.getChild().setLexSense(rankings[0].clazz);
				}
			}
		}
	}
	
	private String createOutputString(List<Token> tokens, Parse parse) {
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for(Token t : tokens) {
			Arc arc = parse.getHeadArcs()[i+1];
			buf.append((i+1)).append('\t').append(t.getText());
			buf.append('\t');
			buf.append(t.getPos());
			buf.append('\t');
			buf.append((arc == null || arc.getHead().getPos()==null) ? 0 : arc.getHead().getIndex());
			buf.append('\t');
			buf.append((arc == null || arc.getHead().getPos()==null) ? "ROOT" : arc.getDependency());
			buf.append("\t" + t.getLexSense());
			buf.append("\n");
			i++;
			
			
		}
		buf.append("\n");
		return buf.toString();
	}
	
	private void mClear_actionPerformed(ActionEvent e) {
		mOutputArea.setText("");
		mHistoryIndex = 0;
	}
	
	public static void createImage(Parse parse, String outfilename, boolean includeRoot) throws IOException, InterruptedException {
		File tmp = File.createTempFile("pref", "suff");
		PrintWriter writer = new PrintWriter(new FileWriter(tmp));
		writer.println("digraph G {");
		if(includeRoot) writer.println("node_0 [label=\"[ROOT]\",style=rounded,shape=box];");
		for(Token t : parse.getSentence().getTokens()) {
			writer.println("node_"+t.getIndex()+" [label=\""+t.getText().replace("\"", "\\\"")+"\",style=rounded,shape=box,margin=\"0.055,0.027\",width=\"0.01\",height=\"0.01\",color=\""+(t.getPos().startsWith("VB")?"red":"darkturquoise")+"\"];");
		}
		for(Token t : parse.getSentence().getTokens()) {					
			Arc a = parse.getHeadArcs()[t.getIndex()];
			Token parent = parse.getHeadArcs()[a.getChild().getIndex()].getHead();
			if(parent != null) {
				// "\""+a.getHead().getText()/*+" ("+a.getHead().getIndex()+")"*/ + "\"
				if(includeRoot || parent.getIndex() > 0) writer.println("node_"+parent.getIndex()+" -> node_" + a.getChild().getIndex()/*+" ("+a.getChild().getIndex()+")"*/ + " [label=\"" + a.getDependency() + "\"];");
			}
		}
		writer.println("}");
		writer.close();
		System.err.println("Wrote to: " + tmp.getAbsolutePath());
		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec(new String[]{"dot","-Tpng","-o"+outfilename,tmp.getAbsolutePath()});
		p.waitFor();
		tmp.delete();
	}
	
	
	public static void main(String[] args) throws Exception {
		String wnDir = args[0];
		String posModel = args[1];
		String modelFile = args[2];
		
		
		
		final MultiStepFeatureGenerator nnFeatureExtractor = null;
		final MultiStepFeatureGenerator psdFeatGen = null;
		
		final Map<String, LinearClassificationModel> prepModels = new HashMap<String, LinearClassificationModel>();
		
		
		System.err.print("Loading pos tagging model...");
		ObjectInputStream ois1 = new ObjectInputStream(new GZIPInputStream(new FileInputStream(posModel)));
		final LinearClassificationModel posTaggingDecisionModule = (LinearClassificationModel)ois1.readObject();
		final PosFeatureGenerator posTaggingFeatureGenerator = (PosFeatureGenerator)ois1.readObject();
		ois1.close();
		System.err.println("loaded");
		
		System.err.print("Loading parsing model...");
		long startTime = System.currentTimeMillis();
		InputStream is = new BufferedInputStream(new FileInputStream(modelFile));
		if(modelFile.endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		ObjectInputStream ois = new ObjectInputStream(is);
		ParseModel model = (ParseModel)ois.readObject();
		ParseFeatureGenerator featGen = (ParseFeatureGenerator)ois.readObject();
		ois.close();

		System.gc();
		System.err.println("loaded");
		System.err.println("Parse model loading took: " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds.");
		
		new WordNet(new File(wnDir));
		
		final NLParser parser = new NLParser(model, featGen);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ParserGui gui = new ParserGui(parser, 
											  posTaggingFeatureGenerator, 
											  posTaggingDecisionModule,
											  psdFeatGen,
											  nnFeatureExtractor,
											  prepModels);
				gui.setVisible(true);
			}
		});
	}
	
}