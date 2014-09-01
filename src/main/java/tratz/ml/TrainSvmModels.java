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

package tratz.ml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;

/**
 * Script for invoking the LIBLINEAR training program for multiple files at a time.
 *
 */
public class TrainSvmModels {
	
	
	public final static String OPT_INPUT = "input",
							   OPT_OUTPUT = "output",
							   OPT_C_PARAM = "svmcparam",
							   OPT_WAIT_FOR = "waitfor",
							   OPT_MAX_AT_ONE_TIME = "maxprocesses";
	
	public final static int DEFAULT_MAX_AT_ONE_TIME = 100;
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_INPUT, "file", "file/directory containing LIBLINEAR-style training information");
		cmdOpts.addOption(OPT_OUTPUT, "file", "file/directory for saving the trained model(s)");
		cmdOpts.addOption(OPT_C_PARAM, "double", "the C parameter for training the SVMs");
		cmdOpts.addOption(OPT_MAX_AT_ONE_TIME, "integer", "the maximum number of SVMs to be training at once");
		cmdOpts.addOption(OPT_WAIT_FOR, "boolean", "wait for one model to complete training before moving on");
		return cmdOpts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File input = new File(cmdLine.getStringValue(OPT_INPUT));
		File output = new File(cmdLine.getStringValue(OPT_OUTPUT));
		double cParam = cmdLine.getDoubleValue(OPT_C_PARAM);
		boolean waitFor = cmdLine.getBooleanValue(OPT_WAIT_FOR);
		int maxAtOneTime = cmdLine.getIntegerValue(OPT_MAX_AT_ONE_TIME, DEFAULT_MAX_AT_ONE_TIME);
		
		if(!output.getAbsoluteFile().exists() && input.isDirectory()) {
			System.err.print("Attempting to create non-existent output directory...");
			boolean madeDirectories = output.getAbsoluteFile().mkdirs();
			System.err.println(madeDirectories ? "success" : "failure");		
		}
		
		Runtime runtime = Runtime.getRuntime();
		
		File[] files = null;
		if(input.isDirectory()) {
			files = input.listFiles();
		}
		else {
			files = new File[]{input};
		}
		Arrays.sort(files);
		List<Process> processesToWaitOn = new ArrayList<Process>();
		//File f : files
		for(int i = 0; i < files.length; i++) {
			File f = files[i];
			String outputFilename;
			if(input.isDirectory()) {
				outputFilename = output.getAbsolutePath()+"/"+f.getName()+".model";
			}
			else {
				outputFilename = output.getAbsolutePath();
			}
			String command = "./trainSVM -q -c " + cParam + " " + f.getAbsolutePath() + " " + outputFilename;
			System.err.println("Executing: " + command);
			Process p = runtime.exec(command);
			
			// if we use the -q (quiet option), we don't have to worry about
			// buffers getting full and causing the thing to stall indefinitely
			//BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			if(waitFor) {
				int result = p.waitFor();
				System.err.println("Process return value: " + result);
			}
			else {
				System.err.println("Will proceed to start the next process immediately if fewer than " + maxAtOneTime + " have been started.");
				processesToWaitOn.add(p);
			}
			
			if(processesToWaitOn.size() >= maxAtOneTime) {
				// Better stop and wait so that we don't open too many files at once
				boolean stillMoreToWait = true;
				while(stillMoreToWait) {
					stillMoreToWait = false;
					for(Process process : processesToWaitOn) {
						try {
							process.exitValue();
						}
						catch(Exception e) {
							stillMoreToWait = true;
							// Just wait a little and poll again
							Thread.sleep(2000); 
						}
					}
				}
				for(Process pro : processesToWaitOn){
					pro.destroy();
				}
				processesToWaitOn.clear();
			}
			
		}
		
		boolean stillMoreToWait = true;
		while(stillMoreToWait) {
			stillMoreToWait = false;
			for(Process p : processesToWaitOn) {
				try {
					p.exitValue();
				}
				catch(Exception e) {
					stillMoreToWait = true;
					Thread.sleep(2000);
				}
			}
		}
		
	}
	
}