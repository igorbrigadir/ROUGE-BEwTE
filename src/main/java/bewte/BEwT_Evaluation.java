package bewte;

import tratz.runpipe.util.RunPipe;

public class BEwT_Evaluation {
		
	static final String PATH = "/home/BEwT/";

	static final String SUMMARIES_DIR = PATH+"eval";
	
	static final String MODELS_DIR = PATH+"models";
	static final String WORDNET_DIR = PATH+"data/wordnet3_0";
	static final String OPENNLP_NER_MODELS_DIR = PATH+"models/opennlp";
	
	static final String INTERMEDIATE_FILES_DIR = PATH+"eval/temp";
		
	static final String STEP1_OUTPUT_DIR = INTERMEDIATE_FILES_DIR+"/parsed";
	static final String STEP2_OUTPUT_DIR = INTERMEDIATE_FILES_DIR+"/BEs";
	static final String STEP3_OUTPUT_DIR = INTERMEDIATE_FILES_DIR+"/BEXs";
	
	static final String REFERENCE_NAME_PATTERN = "reference.*";

	
	static final String ENGLISH_RULE_LIST = PATH+"conf/rules/EN_ruleList.txt";
	static final String ENGLISH_TRANSFORM_LIST = PATH+"conf/transformations/EN_transformsList.txt";
	static final String ENGLISH_TRANSFORM_COEFFS = PATH+"conf/transformations/EN_transformCoeffs.txt";
	static final String END_ANALYSIS_CONF_FILE = PATH+"conf/endanalysis/doNothingEndAnalysisConfig.txt";
	
	static final String SYSTEM_LEVEL_OUTPUT_FILE = PATH+"systemLevelOutput.txt";
	static final String SUMMARY_LEVEL_OUTPUT_FILE = PATH+"summaryLevelOutput.txt";
	
	
	/*
	 * Perform Steps defined ant build:
	 */
	public static void main(String[] args) throws Exception {
		
		// Set Base Path:
		
		System.setProperty("bewte_path", PATH);
		
		/*
		 * Step 1. Parse input documents
		 */
		System.out.println("Step 1. Parse input documents:");
		step1();		
		System.out.println("Done.");
		/*
		 * Step 2. Extract BEs
		 */
		System.out.println("Step 2. Extract BEs");
		step2();
		System.out.println("Done.");
		/*
		 * Step 3. Perform Transformations
		 */
		System.out.println("Step 3. Perform Transformations");
		step3();
		System.out.println("Done.");
		/*
		 * Step 4. Perform Evaluation
		 */
		System.out.println("Step 4. Perform Evaluation");
		step4();
		
		System.out.println("Finished.");
		
	}
	
	public static void step1() throws Exception {
		String[] args1 = new String[] {
				RunPipe.CORPUS_READER,
				"tratz.runpipe.impl.corpusreader.DirectoryCorpusReader",
				"InputDirectories="+SUMMARIES_DIR,
				RunPipe.DOC_READER,
				"bewte.io.StandardTextDocReader",
				RunPipe.ANNOTATOR,
				"tratz.runpipe.impl.annotators.sentence.BreakIteratorSentenceAnnotator",
				"ONLY_WHEN_NECESSARY=true",
				RunPipe.ANNOTATOR,
				"bewte.annotators.RegexTokenizer",
				RunPipe.ANNOTATOR,
				"tratz.runpipe.impl.annotators.pos.TratzPosTaggerAnnotator",
				"ModelFile="+MODELS_DIR+"/posTaggingModel.gz",
				"WordNetDir="+WORDNET_DIR,
				RunPipe.ANNOTATOR,
				"tratz.runpipe.impl.annotators.parse.TratzParserAnnotator",
				"ModelFile="+MODELS_DIR+"/parseModel.gz",
				"WordNetDir="+WORDNET_DIR,
				"VchTransform=true",
				RunPipe.END_POINT,
				"tratz.runpipe.impl.endpoints.GzippedDocumentWriter",
				"OutputDir="+STEP1_OUTPUT_DIR
		};
		RunPipe.main(args1);
	}
	
	public static void step2() throws Exception {
		String[] args2 = new String[] {
				RunPipe.CORPUS_READER,
				"tratz.runpipe.impl.corpusreader.GzippedCorpusReader",
				"InputDirectories="+STEP1_OUTPUT_DIR,
				RunPipe.ANNOTATOR,
				"tratz.runpipe.impl.annotators.parse.TokenFieldUpdater",
				"WordNetDir="+WORDNET_DIR,
				RunPipe.ANNOTATOR,
				"runpipewrappers.ner.OpenNlpNerWrapper",
				"ModelPath="+OPENNLP_NER_MODELS_DIR+"/person.bin.gz",
				"AnnotationClass=tratz.runpipe.annotations.PersonAnnotation",
				RunPipe.ANNOTATOR,
				"runpipewrappers.ner.OpenNlpNerWrapper",
				"ModelPath="+OPENNLP_NER_MODELS_DIR+"/organization.bin.gz",
				"AnnotationClass=tratz.runpipe.annotations.OrganizationAnnotation",				
				RunPipe.ANNOTATOR,
				"runpipewrappers.ner.OpenNlpNerWrapper", 
				"ModelPath="+OPENNLP_NER_MODELS_DIR+"/location.bin.gz",
				"AnnotationClass=tratz.runpipe.annotations.LocationAnnotation",
				RunPipe.END_POINT,
				"bewte.beextraction.BasicElementExtractor",
				"OutputDir="+STEP2_OUTPUT_DIR
		};	
		RunPipe.main(args2);
	}

	
	public static void step3() throws Exception {
		String[] args3 = new String[] {
				STEP2_OUTPUT_DIR,
				STEP3_OUTPUT_DIR,
				WORDNET_DIR,
				"0",
				"-1",
				".*"+REFERENCE_NAME_PATTERN,
				ENGLISH_TRANSFORM_LIST,
				"bewte.names.T2VStyleNameExtractor"				
		};
		BEXpander.main(args3);	
	}
	
	public static void step4() throws Exception {
		String[] args4 = new String[] {
				STEP3_OUTPUT_DIR,
				SYSTEM_LEVEL_OUTPUT_FILE,
				SUMMARY_LEVEL_OUTPUT_FILE,
				"bewte.scoring.TallyFunction$BinaryTallyFunction",
				"false",
				ENGLISH_RULE_LIST,
				ENGLISH_TRANSFORM_LIST,
				ENGLISH_TRANSFORM_COEFFS,
				REFERENCE_NAME_PATTERN,
				END_ANALYSIS_CONF_FILE,
				"bewte.names.T2VStyleNameExtractor",
				".*"				
		};
		BEwT_E.main(args4);	
	}
	
}
