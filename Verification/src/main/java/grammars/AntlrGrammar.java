package grammars;

import core.Grammar;

public class AntlrGrammar extends Grammar {
	public AntlrGrammar() {
		/*Grammar antlrGrammar = new Grammar();
		
		LexerRule grammarToken = antlrGrammar.createTokenInfo("grammar");
		
		grammarToken.addRule("grammar");
		
		LexerRule cToken = antlrGrammar.createTokenInfo("ctrl");
		
		cToken.addRule("\\p{Cntrl}+");
		
		LexerRule wsToken = antlrGrammar.createTokenInfo("ws");
		
		wsToken.addRule("\\s+");
		
		LexerRule upperIdToken = antlrGrammar.createTokenInfo("upperId");
		
		upperIdToken.addRule("[A-Z][A-Za-z0-9]*");
		
		LexerRule lowerIdToken = antlrGrammar.createTokenInfo("lowerId");
		
		lowerIdToken.addRule("[a-z][A-Za-z0-9]*");
		
		LexerRule sepToken = antlrGrammar.createTokenInfo("sep");
		
		sepToken.addRule(";");
		
		LexerRule assignToken = antlrGrammar.createTokenInfo("assign");
		
		assignToken.addRule(Pattern.quote(":"));
		
		LexerRule litToken = antlrGrammar.createTokenInfo("literal");
		
		litToken.addRule("\'[^\']*\'");
		
		LexerRule setLitToken = antlrGrammar.createTokenInfo("setLiteral");
		
		setLitToken.addRule(String.format("%s[^%s]*%s[%s]?", Pattern.quote("["), Pattern.quote("[]"), Pattern.quote("]"), Pattern.quote("+*")));
		
		LexerRule skipArrowToken = antlrGrammar.createTokenInfo("skipArrow");
		
		skipArrowToken.addRule(String.format("%s", Pattern.quote("->")));
		
		LexerRule skipToken = antlrGrammar.createTokenInfo("skip");
		
		skipToken.addRule("skip");
		
		LexerRule orToken = antlrGrammar.createTokenInfo("or");
		
		orToken.addRule(Pattern.quote("|"));
		
		File gFile = new File(workDir, "Grammar\\Hello.g4");
		
		//FileInputStream gf = new FileInputStream(new FileReader(gFile, StandardCharsets.UTF_8));
		
		byte[] gbs = Files.readAllBytes(gFile.toPath());
		
		String gs = new String(gbs, StandardCharsets.UTF_8);
		
		new Lexer(antlrGrammar).tokenize(gs).print();*/
		
		//gf.close();
	}
}