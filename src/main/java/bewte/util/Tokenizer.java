package bewte.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	
	//private final Matcher START_QUOTE = Pattern.compile("^(''|\")").matcher("");
	//private final Matcher MORE_START_QUOTES = Pattern.compile("([ \\(\\[{<])(''|\")").matcher("");
	private final Matcher PUNCTUATION = Pattern.compile("([;@#%&])").matcher("");
	private final Matcher COLON_PUNCTUATION1 = Pattern.compile("([^0-9])(:)").matcher("");
	private final Matcher COLON_PUNCTUATION2 = Pattern.compile("(:)([^0-9])").matcher("");
	private final Matcher CURRENCY_SYMBOLS = Pattern.compile("(A\\$|H\\$|C\\$|\\$|¢|£|#|¤|¥|₠|₡|₢|₣|₤|₦|₧|₨|₩|₪|₫|€|₭|₮|₯|₰|₱|₲|₳|₴|₵)").matcher("");
	private final Matcher ELLIPSE = Pattern.compile("\\.\\.\\.").matcher("");
	//private final Matcher COMMA = Pattern.compile("([^0-9]),([^0-9])").matcher("");
	private final Matcher COMMA = Pattern.compile("([^0-9]),(.)").matcher("");
	private final Matcher COMMA2 = Pattern.compile("([0-9]), ").matcher("");
	private final Matcher COMMA3 = Pattern.compile("([0-9]),(``|'|''|\") ").matcher("");
	private final Matcher FINAL_PERIOD_OR_COMMA = Pattern.compile("([^\\.])([\\.,])([\\]\\)\\}>\"']*)[ \t]*$").matcher("");
	
	private final Matcher PERIOD_QUOTE = Pattern.compile("([\\s]+)\\.(\"|''|')").matcher("");
	private final Matcher QUESTION_OR_EXCLAIM = Pattern.compile("[!?]").matcher("");
	private final Matcher BRACKETS = Pattern.compile("[\\]\\[(){}<>]").matcher("");
	private final Matcher LRB = Pattern.compile("\\(").matcher("");
	private final Matcher RRB = Pattern.compile("\\)").matcher("");
	private final Matcher LSB = Pattern.compile("\\[").matcher("");
	private final Matcher RSB = Pattern.compile("\\]").matcher("");
	private final Matcher LCB = Pattern.compile("\\{").matcher("");
	private final Matcher RCB = Pattern.compile("\\}").matcher("");
	private final Matcher DOUBLE_DASH = Pattern.compile("--").matcher("");
	private final Matcher SPACE_DASH = Pattern.compile(" -([^\\s\\-])").matcher("");
	private final Matcher DOUBLE_QUOTE = Pattern.compile("''|\"").matcher("");
	private final Matcher POSSESSIVE_OR_SINGLE_QUOTE = Pattern.compile("([^'])' ").matcher("");
	private final Matcher PRECEDING_SINGLE_QUOTE = Pattern.compile(" '([^'])").matcher("");
	private final Matcher PRECEDING_STARS = Pattern.compile("(\\*+)([^\\* ]+)").matcher("");
	private final Matcher FOLLOWING_STARS = Pattern.compile("([^\\* ]+)(\\*+)").matcher("");
	private final Matcher CONTRACTION0 = Pattern.compile("'([sSmMdD]) ").matcher("");
	private final Matcher CONTRACTION1 = Pattern.compile("'(ll|LL|re|RE|ve|VE)").matcher("");
	private final Matcher CONTRACTION2 = Pattern.compile("(n|N)'(t|T) ").matcher("");
	
	private final Matcher SLANG0 = Pattern.compile(" ([Cc])annot ").matcher("");
	private final Matcher SLANG1 = Pattern.compile(" ([Dd])'ye ").matcher("");
	private final Matcher SLANG2 = Pattern.compile(" ([Gg])imme ").matcher("");
	private final Matcher SLANG3 = Pattern.compile(" ([Gg])onna ").matcher("");
	private final Matcher SLANG4 = Pattern.compile(" ([Gg])otta ").matcher("");
	private final Matcher SLANG5 = Pattern.compile(" ([Ll])emme ").matcher("");
	private final Matcher SLANG7 = Pattern.compile(" '([Tt])is ").matcher("");
	private final Matcher SLANG8 = Pattern.compile(" '([Tt])was ").matcher("");
	private final Matcher SLANG9 = Pattern.compile(" ([Ww])anna ").matcher("");
	
	private final Matcher MULTISPACE = Pattern.compile("\\s+").matcher("");
	
	public String tokenize(String s) {
		// Let's first reset all quotes to '"'
		s = s.replace("``", "\"");
		s = s.replace("''", "\"");
		
		s = s.replaceAll("(\")([^\"]*)(\")", "`` $2 ''");
		
		s = s.replaceAll("``", " `` ");
		s = s.replaceAll("''", " '' ");
		s = s.replaceAll("\"", " \" ");
		s = s.replaceAll("\\.\\s*''", " \\. ''");
		
		s = ELLIPSE.reset(s).replaceAll(" ... ");
		
		s = PUNCTUATION.reset(s).replaceAll(" $1 ");
		s = COLON_PUNCTUATION1.reset(s).replaceAll("$1 $2 ");
		s = COLON_PUNCTUATION2.reset(s).replaceAll(" $1 $2");
		
		s = CURRENCY_SYMBOLS.reset(s).replaceAll(" $1 ");
		s = COMMA.reset(s).replaceAll("$1 , $2");
		s = COMMA2.reset(s).replaceAll("$1 , ");
		s = COMMA3.reset(s).replaceAll("$1 , $2");

		s = FINAL_PERIOD_OR_COMMA.reset(s).replaceAll("$1 $2$3");
		s = QUESTION_OR_EXCLAIM.reset(s).replaceAll(" $0 ");
		s = BRACKETS.reset(s).replaceAll(" $0 ");
		
		s = LRB.reset(s).replaceAll(" ( ");
		s = RRB.reset(s).replaceAll(" ) ");
		s = LSB.reset(s).replaceAll(" [ ");
		s = RSB.reset(s).replaceAll(" ] ");
		s = LCB.reset(s).replaceAll(" { ");
		s = RCB.reset(s).replaceAll(" } ");
		
		s = DOUBLE_DASH.reset(s).replaceAll(" -- ");
		s = SPACE_DASH.reset(s).replaceAll(" - $1");
		s = " " + s + " ";

		s = POSSESSIVE_OR_SINGLE_QUOTE.reset(s).replaceAll("$1 ' ");
		s = PRECEDING_SINGLE_QUOTE.reset(s).replaceAll(" ' $1");
		s = PERIOD_QUOTE.reset(s).replaceAll("$1 . $2");
		
		s = PRECEDING_STARS.reset(s).replaceAll("$1 $2");
		s = FOLLOWING_STARS.reset(s).replaceAll("$1 $2");
		
		s = CONTRACTION0.reset(s).replaceAll(" '$1 ");
		s = CONTRACTION1.reset(s).replaceAll(" '$1 ");
		s = CONTRACTION2.reset(s).replaceAll(" $1'$2 ");
		
		
		s = SLANG0.reset(s).replaceAll(" $1an not ");
		s = SLANG1.reset(s).replaceAll(" $1' ye ");
		s = SLANG2.reset(s).replaceAll(" $1im me ");
		s = SLANG3.reset(s).replaceAll(" $1on na ");
		s = SLANG4.reset(s).replaceAll(" $1ot ta ");
		s = SLANG5.reset(s).replaceAll(" $1em me ");
		s = SLANG7.reset(s).replaceAll(" '$1 is ");
		s = SLANG8.reset(s).replaceAll(" '$1 was ");
		s = SLANG9.reset(s).replaceAll(" $1an na ");

		s = MULTISPACE.reset(s).replaceAll(" ");
		s = s.trim();
	//	s = FINAL_PERIOD.reset(s).replaceAll("$1 $2$3");
	//	s = SENTENCE_FINAL_LOWERCASE_WORD.reset(s).replaceAll("$1 . $2");
		//s = ABBREV_MATCHER.reset(s).replaceAll("$1. ");
		return s;
	}
	
}