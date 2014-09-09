BEwT-E: Basic Elements with Transformations for Evaluation
Creators(s): Stephen Tratz, Eduard Hovy
Contact Point: tratz _at_ usc.edu
Version: 0.5

1. What is BEwT-E?
===========================================================
	BEwT-E, Basic Elements with Transformations for Evaluation, is an evaluation package intended for the evaluation of text summarization systems.


2. What are the requirements for running BEwT-E?
===========================================================
	1) Java 1.6 or higher
	2) Apache Ant
	3) (optional) Named Entity Recognition (NER) models
		BEwT-E has built in support for LingPipe and OpenNLP named entity recognition (NER) systems.
		LingPipe
			NER model file for LingPipe is available from http://alias-i.com/lingpipe/web/models.html
			The LINGPIPE_NER_MODEL variable in the BEwT-E_INSTALLATION_DIRECTORY/build/build.xml file must point to your local copy of this model file
			Uncomment the LingPipe-related lines under the 'Extract_BEs' target in the BEwT-E_INSTALLATION_DIRECTORY/build/build.xml file to enable LingPipe NER
		OpenNLP
			NER model files for OpenNLP are available from http://opennlp.sourceforge.net/models.html
			The OPENNLP_NER_MODEL variable in the BEwT-E_INSTALLATION_DIRECTORY/build/build.xml file must point to the local directory containing these models
			Uncomment the OpenNLP-related lines under the 'Extract_BEs' target in the BEwT-E_INSTALLATION_DIRECTORY/build/build.xml file to enable OpenNLP NER
			

3. How do I use BEwT-E?
===========================================================
	Step 1: See point 2. (requirements) and ensure that you have NER capabilities installed, if desired.
	Step 2: Set I/O variables in the BEwT-E_INSTALLATION_DIRECTORY/build/build.xml file
				Set the SUMMARIES_DIR variable to point to the directory with the text summaries in plain text format.
					NOTE: Summary filenames are expected to be similar to DUC/TAC names (e.g. TOPIC_NAME.(stuff_in_between).SYSTEM_ID where automatic systems are numbers and reference/human systems are uppercase letters)
				Set the INTERMEDIATE_FILES_DIR variable to point to the directory to use for intermediate files.
				Set the SYSTEM_LEVEL_OUTPUT_FILE variable to point to the file that will contain BEwT-E scores for each system.
				Set the SUMMARY_LEVEL_OUTPUT_FILE variable to point to the file that will contain BEwT-E scores for each summary.
				Set the FILES_TO_INCLUDE variable to a regular expression to limit the evaluation to only those files whose filenames match the expression.
	Step 3: Parse the summaries by executing "ant Step1" from the BEwT-E_INSTALLATION_DIRECTORY/build directory
	Step 4: Extract BEs from the parsed summaries by executing "ant Step2" from the BEwT-E_INSTALLATION_DIRECTORY/build directory
	Step 5: Transform the extracted BEs by executing "ant Step3" from the BEwT-E_INSTALLATION_DIRECTORY/build directory
	Step 6: Perform the evaluation by executing "ant Step4" from the BEwT-E_INSTALLATION_DIRECTORY/build directory		


4. If I want to modify/adapt BEwT-E, how do I go about doing this?
===========================================================
	See the doc/ADAPTING_BEWTE.txt file.


5. Where can I find the License Agreements for BEwT-E and the resources it relies upon?
===========================================================
	See the files under doc/licenseinfo