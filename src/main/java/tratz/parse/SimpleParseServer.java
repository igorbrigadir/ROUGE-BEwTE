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

package tratz.parse;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.parse.FullSystemWrapper.FullSystemResult;
import tratz.parse.transform.BasicStanfordTransformer;
import tratz.parse.types.Sentence;

/**
 * 
 *
 */
public class SimpleParseServer {
	
	public final static String OPT_POS_MODEL = "posmodel",
	   OPT_PARSE_MODEL = "parsemodel",
		
		OPT_POSSESSIVES_MODEL = "possmodel",
		OPT_NOUN_COMPOUND_MODEL = "nnmodel",
		OPT_PREPOSITIONS_MODEL = "psdmodel",
		OPT_SRL_ARGS_MODEL = "srlargsmodel",
		OPT_SRL_PREDICATES_MODEL = "srlpredmodel",
		
		OPT_WORDNET_DIR = "wndir",
		OPT_PORT_NUMBER = "port";

	public final static String DEFAULT_SENTENCE_READER_CLASS = tratz.parse.io.ConllxSentenceReader.class.getName();
	
	public static class ParseRequest implements Serializable {
		private static final long serialVersionUID = 1L;
		Sentence sentence;
		boolean doPos, doParse, doPreps, doNNs, doPoss, doSRL, doStanfordBasicTransform;
		public ParseRequest(Sentence sentence,
				boolean posTag,
				boolean parse,
				boolean prepDisambiguation,
				boolean nnDisambiguation,
				boolean possInterpretation,
				boolean srl,
				boolean stanfordBasicTransform) {
			this.sentence = sentence;
			doPos = posTag;
			doParse = parse;
			doPreps = prepDisambiguation;
			doNNs = nnDisambiguation;
			doPoss = possInterpretation;
			doSRL = srl;
			doStanfordBasicTransform = stanfordBasicTransform;
		}
		
		public ParseRequest(Sentence sentence) {
			this(sentence, true, true, true, true, true, true, false);
		}
		
		public Sentence getSentence() {
			return sentence;
		}
	}
	
	public static class ParseResult implements Serializable {

		private static final long serialVersionUID = 1L;

		private Exception mException;
		private FullSystemResult mFullResult;
		
		public ParseResult(Exception e) {
			mException = e;
		}
		
		public ParseResult(FullSystemResult parse) {
			mFullResult = parse;
		}
		
		public Exception getException() {
			return mException;
		}
		
		public FullSystemResult getResult() {
			return mFullResult;
		}
	}

	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_PORT_NUMBER, "integer", "port number to run server on");
		cmdOpts.addOption(OPT_POS_MODEL, "file", "part-of-speech tagging model file");
		cmdOpts.addOption(OPT_PARSE_MODEL, "file", "parser model file");

		cmdOpts.addOption(OPT_NOUN_COMPOUND_MODEL, "file", "noun compound interpretation model file");
		cmdOpts.addOption(OPT_PREPOSITIONS_MODEL, "file", "preposition disambiguation models file");
		cmdOpts.addOption(OPT_POSSESSIVES_MODEL, "file", "possessives interpretation model file");
		cmdOpts.addOption(OPT_SRL_PREDICATES_MODEL, "file", "semantic role labeling model file");
		cmdOpts.addOption(OPT_SRL_ARGS_MODEL, "file", "semantic role labeling model file");

		cmdOpts.addOption(OPT_WORDNET_DIR, "file", "WordNet dictionary (dict) directory");

		return cmdOpts;
	}
	
	private int mPortNumber;
	private ServerSocket mServerSocket;
	private volatile boolean mIsRunning;
	
	private FullSystemWrapper mFullSystem;
	
	public SimpleParseServer(int portNumber, FullSystemWrapper fullSystem) {
		mPortNumber = portNumber;
		mFullSystem = fullSystem;
	}
	
	public void launch() throws IOException {
		mServerSocket = new ServerSocket(mPortNumber);
		System.err.println("Server started, waiting to accept connections...");
		mIsRunning = true;
		while(mIsRunning) {
			Socket newConnection = mServerSocket.accept();
			System.err.println("Connection accepted");
			ClientConnectionHandlerRunnable runnable = null;
			try {
				runnable = new ClientConnectionHandlerRunnable(newConnection);
			}
			catch(IOException ioe) {
				System.err.println("Exception occurred while attempting to handle new connection");
				ioe.printStackTrace();
			}
			if(runnable != null) {
				new Thread(runnable).start();
			}
		}
	}
	
	public class ClientConnectionHandlerRunnable implements Runnable {
		
		private Socket mConnection;
		private ObjectInputStream mInputStream;
		private ObjectOutputStream mOutputStream;
		private boolean mIsRunning;
		
		public ClientConnectionHandlerRunnable(Socket connection) throws IOException {
			mConnection = connection;
			mOutputStream = new ObjectOutputStream(connection.getOutputStream());
			mInputStream = new ObjectInputStream(connection.getInputStream());
		}
		
		@Override
		public void run() {
			mIsRunning = true;
			BasicStanfordTransformer basicStanTransform = new BasicStanfordTransformer();
			while(mIsRunning) {
				ParseRequest parseRequest = null;
				try {
					parseRequest = (ParseRequest)mInputStream.readObject();
				}
				catch(EOFException eof) {
					// connection must be closed, exit quietly
					mIsRunning = false;
					break;
				}
				catch(Exception e) {
					System.err.println("Exception occurred while receiving data from client");
					e.printStackTrace();
					mIsRunning = false;
					break;
				}
				
				Exception encounteredException = null;
				FullSystemResult fullResult = null;
				try {
					fullResult = mFullSystem.process(parseRequest.sentence,
											     parseRequest.doPos,
											     parseRequest.doParse,
											     parseRequest.doPreps,
											     parseRequest.doNNs,
											     parseRequest.doPoss,
											     parseRequest.doSRL);
					if(parseRequest.doStanfordBasicTransform) {
						basicStanTransform.performTransformation(fullResult.getParse());
					}
				}
				catch(Exception e) {
					encounteredException = e;
				}
				
				try {
					ParseResult result = fullResult == null 
										    ? new ParseResult(encounteredException)
											: new ParseResult(fullResult);
					mOutputStream.writeObject(result);
					mOutputStream.flush();
					// Call reset() to avoid memory leak
					mOutputStream.reset();
				}
				catch(Exception e) {
					System.err.println("Exception occurred while sending data to client");
					e.printStackTrace();
					mIsRunning = false;
				}
			}
			
			try {
				mConnection.close();
			}
			catch(IOException e) {
				System.err.println("Exception while closing connection");
				e.printStackTrace();
			}
			System.err.println("Client handler thread exiting.");
		}
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		// POS-tagging model file
		String posModelFile = cmdLine.getStringValue(OPT_POS_MODEL);
		// 'dict' directory of WordNet
		String wnDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		// parsing model file
		String parseModelFile = cmdLine.getStringValue(OPT_PARSE_MODEL);
		// possessives model file 
		String possessivesModelFile = cmdLine.getStringValue(OPT_POSSESSIVES_MODEL);
		// noun compound model file
		String nounCompoundModelFile = cmdLine.getStringValue(OPT_NOUN_COMPOUND_MODEL);
		// preposition model file
		String prepositionModelFile = cmdLine.getStringValue(OPT_PREPOSITIONS_MODEL);
		// srl model file
		String srlArgsModelFile = cmdLine.getStringValue(OPT_SRL_ARGS_MODEL);
		String srlPredicatesModelFile = cmdLine.getStringValue(OPT_SRL_PREDICATES_MODEL);
		
		int portNumber = cmdLine.getIntegerValue(OPT_PORT_NUMBER);
		System.err.println("Port number: " + portNumber);
		FullSystemWrapper fullSysWrapper = new FullSystemWrapper(prepositionModelFile, nounCompoundModelFile, possessivesModelFile, srlArgsModelFile, srlPredicatesModelFile, posModelFile, parseModelFile, wnDir);
		
		SimpleParseServer server = new SimpleParseServer(portNumber, fullSysWrapper);
		server.launch();
	}
	
}