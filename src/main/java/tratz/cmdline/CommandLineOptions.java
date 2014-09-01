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

import java.util.ArrayList;
import java.util.List;

public class CommandLineOptions {
	
	public static class Option {
		private String mName;
		private String mType;
		private String mDescription;
		
		public Option(String name, String type, String description) {
			mName = name;
			mType = type;
			mDescription = description;
		}
		
		public String getName() {
			return mName;
		}
		
		public String getType() {
			return mType;
		}
		
		public String getDescription() {
			return mDescription;
		}
	}
	
	private List<Option> mOptions;
	
	public CommandLineOptions() {
		mOptions = new ArrayList<Option>();
	}
	
	public void addOption(String name, String type, String description) {
		mOptions.add(new Option(name, type, description));
	}
	
	public List<Option> getOptions() {
		return new ArrayList<Option>(mOptions);
	}
	
	public String getArgumentsDescriptionString() {
		StringBuilder buf = new StringBuilder();
		for(Option opt : mOptions) {
			buf.append(opt.getName() + "\t<" + opt.getType() + ">\t" + opt.getDescription()).append("\n");
		}
		return buf.toString();
	}
	
}