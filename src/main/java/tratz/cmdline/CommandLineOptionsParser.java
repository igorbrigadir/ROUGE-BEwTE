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

import java.util.List;

import tratz.cmdline.CommandLineOptions.Option;

public class CommandLineOptionsParser {
	
	public ParsedCommandLine parseOptions(CommandLineOptions options, String[] args) throws CommandLineOptionsParsingException {
		ParsedCommandLine commandLine = new ParsedCommandLine();
		
		List<Option> optionsList = options.getOptions();
		
		for(int i = 0; i < args.length; i+=2) {
			String argumentIdentifier = args[i];
			if(!argumentIdentifier.startsWith("-") || argumentIdentifier.length() <= 1) {
				throw new CommandLineOptionsParsingException("unexpected argument name: " + argumentIdentifier);
			}
			else {
				String argIdName = argumentIdentifier.substring(1);
				boolean matched = false;
				for(Option opt : optionsList) {
					if(opt.getName().equals(argIdName)) {
						matched = true;
						commandLine.setArgumentValue(argIdName, args[i+1]);
					}
				}
				if(!matched) {
					throw new CommandLineOptionsParsingException("unexpected argument name: " + argumentIdentifier + "\n" + options.getArgumentsDescriptionString());
				}
			}
		}
		
		return commandLine;
	}
	
}