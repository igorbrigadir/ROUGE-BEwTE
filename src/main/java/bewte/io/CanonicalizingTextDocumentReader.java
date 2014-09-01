package bewte.io;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.parse.util.NLParserUtils;
import tratz.runpipe.TextDocumentReader;

import bewte.BEConstants;


abstract public class CanonicalizingTextDocumentReader implements TextDocumentReader {

	
	private final Matcher START_QUOTE = Pattern.compile("^(''|\")").matcher("");
	private final Matcher MORE_START_QUOTES = Pattern.compile("([ \\(\\[{<])(''|\")").matcher("");
	private final Matcher DOUBLE_QUOTE = Pattern.compile("''|\"").matcher("");
	
	public void initialize(Map<String, String> params) {
		
	}
	
	public String canonicalizeString(String l) {
		l = l.trim();
		l = START_QUOTE.reset(l).replaceAll("`` ");
		l = MORE_START_QUOTES.reset(l).replaceAll("$1 `` ");
		l = DOUBLE_QUOTE.reset(l).replaceAll(" '' ");
		
		// Canonicalize quotes
		l = l.replaceAll(NLParserUtils.UNUSUAL_DOUBLE_QUOTES, "\"");
		l = l.replaceAll(NLParserUtils.UNUSUAL_SINGLE_QUOTES, "'");
		//l = l.replace("''", "\"");
		//l = l.replace("``", "\"");
		//l = l.replace("`", "'");
		
		
		// Canonicalize dashes
		l = l.replace(" _ ", " - ");
		l = l.replace(" -- ", " - ");
		l = l.replace("- -", " - ");
		// Remove excess whitespace from contractions
		l = l.replaceAll(" ' (s|re|ve)([^a-zA-Z])?", "'$1$2");
		l = l.replaceAll(" '(s|re|ve)([^a-zA-Z])?", "'$1$2");
		l = l.replaceAll("n 't([^a-zA-Z])?", "n't$1");
		l = l.replaceAll(" n't","n't");
		l = l.replace("' ll", "'ll");
		// Remove elipses
		l = l.replaceAll("\\. \\. \\.[ \\.]*", "... ");
		l = l.replace("...", ".");
		// Remove excess whitespace near punctuation
		l = l.replace("$ ", "$");
		l = l.replace("( ", "(");
		l = l.replace(" )", ")");
		// Remove instances of the BE SEPARATOR CHAR to prevent weird issues
		l = l.replace(BEConstants.BE_SEPARATOR_CHAR, ' ');
		
		// Fix obvious sentence breaks
		l = l.replaceAll("([a-z]\\.)([A-Z])", "$1 $2");
		l = l.replaceAll("([a-z]\\?)([A-Z])", "$1 $2");
		l = l.replaceAll("([a-z]\\!)([A-Z])", "$1 $2");
		return l;
	}
	
}