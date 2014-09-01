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

package tratz.featgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tratz.featgen.fer.FeatureExtractionRule;
import tratz.featgen.wfr.WordFindingRule;


public class FeatureGenerationConfigurationReader extends DefaultHandler {
	
	
	public final static String WFR_TAG = "wfr",
							   FER_TAG = "fer",
							   COMBO_TAG = "combo",
							   PARAMS_TAG = "params",
							   PARAM_TAG = "param",
							   FER_LIST_TAG = "ferlist";
	
	public final static String CLASSNAME_ATT = "classname",
							   PREFIX_ATT = "prefix",
							   NAME_ATT = "name",
							   COMBO_TYPE_ATT = "comboType",
							   COMBO_WFR_PREFIX1 = "wfrPrefix1",
							   COMBO_WFR_PREFIX2 = "wfrPrefix2",
							   COMBO_FER_PREFIX1 = "ferPrefix1",
							   COMBO_FER_PREFIX2 = "ferPrefix2",
							   KEY_ATT = "key",
							   VALUE_ATT = "value";
	
	public static void read(File configurationFile) throws IOException{
		SAXParserFactory mParserFactory = SAXParserFactory.newInstance();
				
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configurationFile));
			
			InputSource inputSource = new InputSource(reader);
			FeatureGenerationConfigurationReader handler = new FeatureGenerationConfigurationReader();
			SAXParser parser = mParserFactory.newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(inputSource);
			reader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new IOException();
		}
	}
	
	@Override
	public void startDocument() {
	    mWfrNames = new HashSet<String>();
	    mWfrEntries = new ArrayList<WfrEntry>();
	    mFerMap = new HashMap<String, FeatureExtractionRule>();
	    resetState();
	}    
	
	@Override
	public void endDocument()  {

	}
	
	private Set<String> mWfrNames;
	private Map<String, FeatureExtractionRule> mFerMap;
	private List<WfrEntry> mWfrEntries;
	
	private String mWfrClass, mWfrName, mWfrPrefix;
	private String mFerClass, mFerName, mFerPrefix;
	private String mComboWfrPrefix1, mComboWfrPrefix2, mComboFerPrefix1, mComboFerPrefix2;
	private Map<String, String> mParams;
	private List<FeatureExtractionRule> mFers;
	
	private void resetState() {
		mWfrClass = null; mWfrName = null; mWfrPrefix = null;
		mFerClass = null; mFerName = null; mFerPrefix = null;
		mComboWfrPrefix1 = null; mComboWfrPrefix2 = null; mComboFerPrefix1 = null; mComboFerPrefix2 = null;
		mParams = null;
		mFers = null;
	}
	  
	@Override
	public void startElement(String uri, String localName, String qname, Attributes attr) throws SAXParseException {
	    System.out.println("Start element: local name: " + localName + " qname: " 
	                                                        + qname + " uri: "+uri);
	    
	    if(localName.equals(WFR_TAG)) {
	    	resetState();
	    	mWfrClass = attr.getValue(CLASSNAME_ATT);
	    	mWfrName = attr.getValue(NAME_ATT);
	    	mWfrPrefix = attr.getValue(PREFIX_ATT);
	    	if(mWfrNames.contains(mWfrName)) {
	    		throw new SAXParseException("Error. Duplicate WFR name: " + mFerName, null);
	    	}
	    }
	    else if(localName.equals(FER_TAG)) {
	    	resetState();
	    	mFerClass = attr.getValue(CLASSNAME_ATT);
	    	mFerName = attr.getValue(NAME_ATT);
	    	mFerPrefix = attr.getValue(PREFIX_ATT);
	    	if(mFerMap.containsKey(mFerName)) {
	    		throw new SAXParseException("Error. Duplicate FER name: " + mFerName, null);
	    	}
	    }
	    else if(localName.equals(COMBO_TAG)) {
	    	resetState();
	    }
	    else if(localName.equals(FER_LIST_TAG)) {
	    	mFers = new ArrayList<FeatureExtractionRule>();
	    }
	    else if(localName.equals(PARAMS_TAG)) {
	    	mParams = new HashMap<String, String>();
	    }
	    else if(localName.equals(PARAM_TAG)) {
	    	String key = attr.getValue(KEY_ATT);
	    	String value = attr.getValue(VALUE_ATT);
	    	mParams.put(key, value);
	    }
	    
	  }
	
	@Override
	public void endElement(String uri, String localName, String qname) throws SAXParseException {
		try {
			if(localName.equals(WFR_TAG)) {
				WordFindingRule wfr = (WordFindingRule)Class.forName(mWfrClass).newInstance();
				wfr.init(mParams);
				mWfrNames.add(mWfrName);
				mWfrEntries.add(new WfrEntry(mWfrName, mWfrPrefix, mFers, wfr));
			}
			else if(localName.equals(FER_TAG)) {
				FeatureExtractionRule fer = (FeatureExtractionRule)Class.forName(mFerClass).newInstance();
				fer.init(mParams);
				mFerMap.put(mFerName, fer);
			}
		}
		catch(ClassNotFoundException cnfe) {
			throw new SAXParseException(cnfe.getMessage(), null);
		}
		catch(IllegalAccessException iae) {
			throw new SAXParseException(iae.getMessage(), null);
		}
		catch(InstantiationException ie) {
			throw new SAXParseException(ie.getMessage(), null);
		}
		catch(InitException ie) {
			throw new SAXParseException(ie.getMessage(), null);
		}
	}
	  
	@Override
	  public void characters(char[] ch, int start, int length) {
	    
	  }
	
	  @Override
	  public void ignorableWhitespace(char[] ch, int start, int length) {
	    
	  }

	  @Override
	  public void warning(SAXParseException spe) {
	    System.out.println("SAX parse exception for line " + spe.getLineNumber());
	  }

	  @Override
	  public void fatalError(SAXParseException spe) throws SAXException {
	    System.out.println("SAX parse fatal error for line " + spe.getLineNumber());
	    throw spe;
	  }
	
}
