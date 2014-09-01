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

package tratz.cmdline;

import java.util.HashMap;
import java.util.Map;

public class ParsedCommandLine {
	
	private Map<String, String> mArgToValue = new HashMap<String, String>();
	
	public ParsedCommandLine() {
		
	}
	
	public void setArgumentValue(String argument, String value) {
		mArgToValue.put(argument, value);
	}
	
	public String getStringValue(String argument, String defaultValue) {
		String value = mArgToValue.get(argument);
		if(value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public String getStringValue(String argument) {
		return getStringValue(argument, null);
	}
	
	public double getDoubleValue(String argument, double defaultValue) {
		String value = mArgToValue.get(argument);
		double dval = -1;
		if(value == null) {
			dval = defaultValue;
		}
		else {
			dval = Double.parseDouble(value);
		}
		return dval;
	}
	
	public double getDoubleValue(String argument) {
		return getDoubleValue(argument, 0);
	}
	
	public Integer getIntegerValue(String argument, Integer defaultValue) {
		String value = mArgToValue.get(argument);
		int dval = -1;
		if(value == null) {
			dval = defaultValue;
		}
		else {
			dval = Integer.parseInt(value);
		}
		return dval;
	}
	
	public Integer getIntegerValue(String argument) {
		return getIntegerValue(argument, null);
	}
	
	public Boolean getBooleanValue(String argument) {
		return getBooleanValue(argument, null);
	}
	
	public Boolean getBooleanValue(String argument, Boolean defaultValue) {
		String value = mArgToValue.get(argument);
		Boolean dval = null;
		if(value == null) {
			if(defaultValue != null) {
				dval = defaultValue;	
			}
		}
		else {
			dval = Boolean.parseBoolean(value);
		}
		return dval;
	}
	
}