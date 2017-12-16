package grammars;

import core.Grammar;
import core.structures.NonTerminal;
import core.structures.ParserRule;
import core.structures.Terminal;

public class ExpGrammar extends Grammar {
	public final Terminal TERMINAL_EXP_LIT;
	public final Terminal TERMINAL_PAREN_OPEN;
	public final Terminal TERMINAL_PAREN_CLOSE;
	public final Terminal TERMINAL_OP_PLUS;
	public final Terminal TERMINAL_OP_MINUS;
	public final Terminal TERMINAL_OP_MULT;
	public final Terminal TERMINAL_OP_DIV;
	public final Terminal TERMINAL_OP_POW;
	public final Terminal TERMINAL_OP_FACT;
	public final Terminal TERMINAL_ID;
	public final Terminal TERMINAL_PARAM_SEP;
	
	public final NonTerminal NON_TERMINAL_EXP;
	public final NonTerminal NON_TERMINAL_SUM;
	public final NonTerminal NON_TERMINAL_SUM_;
	public final NonTerminal NON_TERMINAL_PROD;
	public final NonTerminal NON_TERMINAL_PROD_;
	public final NonTerminal NON_TERMINAL_POW;
	public final NonTerminal NON_TERMINAL_POW_;
	public final NonTerminal NON_TERMINAL_FACT;
	public final NonTerminal NON_TERMINAL_FACT_;
	public final NonTerminal NON_TERMINAL_EXP_ELEM;

	public final NonTerminal NON_TERMINAL_PARAM_LIST;
	public final NonTerminal NON_TERMINAL_PARAM_LIST_;
	public final NonTerminal NON_TERMINAL_PARAM;
	
	public final ParserRule RULE_SUM;
	public final ParserRule RULE_PROD_SUM_;
	public final ParserRule RULE_OP_PLUS_PROD_SUM_;
	public final ParserRule RULE_OP_MINUS_PROD_SUM_;
	public final ParserRule RULE_POW_PROD_;
	public final ParserRule RULE_OP_MULT_POW_PROD_;
	public final ParserRule RULE_OP_DIV_POW_PROD_;
	public final ParserRule RULE_FACT_POW_;
	public final ParserRule RULE_OP_POW_POW;
	public final ParserRule RULE_EXP_ELEM_FACT_;
	public final ParserRule RULE_OP_FACT_FACT_;
	public final ParserRule RULE_ID_PARAM_LIST;
	public final ParserRule RULE_EXP_LIT;
	public final ParserRule RULE_PARENS_EXP;
	public final ParserRule RULE_PARENS_PARAM_PARAM_LIST_;
	public final ParserRule RULE_PARAM_SEP_PARAM_PARAM_LIST_;
	public final ParserRule RULE_EXP;

	public ExpGrammar() {
		super();
		
		//lexer rules
		TERMINAL_EXP_LIT = createTerminal("EXP_LIT");
		TERMINAL_PAREN_OPEN = createTerminal("PAREN_OPEN");
		TERMINAL_PAREN_CLOSE = createTerminal("PAREN_CLOSE");
		TERMINAL_OP_PLUS = createTerminal("OP_PLUS");
		TERMINAL_OP_MINUS = createTerminal("OP_MINUS");
		TERMINAL_OP_MULT = createTerminal("OP_MULT");
		TERMINAL_OP_DIV = createTerminal("OP_DIV");
		TERMINAL_OP_POW = createTerminal("OP_POW");
		TERMINAL_OP_FACT = createTerminal("OP_FACT");
		TERMINAL_ID = createTerminal("ID");
		TERMINAL_PARAM_SEP = createTerminal("PARAM_SEP").setSep();

		TERMINAL_EXP_LIT.addRuleRegEx("[1-9][0-9]*");
		TERMINAL_EXP_LIT.addRuleRegEx("0");
		
		TERMINAL_PAREN_OPEN.addRule("(");
		TERMINAL_PAREN_CLOSE.addRule(")");
		TERMINAL_OP_PLUS.addRule("+");
		TERMINAL_OP_MINUS.addRule("-");
		TERMINAL_OP_MULT.addRule("*");
		TERMINAL_OP_DIV.addRule("/");
		TERMINAL_OP_POW.addRule("^");
		TERMINAL_OP_FACT.addRule("!");
		
		TERMINAL_ID.addRuleRegEx("[a-zA-Z][a-zA-Z0-9]*");

		TERMINAL_PARAM_SEP.addRule(",");
		
		//parser rules
		NON_TERMINAL_EXP = createNonTerminal("exp");
		NON_TERMINAL_SUM = createNonTerminal("sum");
		NON_TERMINAL_SUM_ = createNonTerminal("sum'");
		NON_TERMINAL_PROD = createNonTerminal("prod");
		NON_TERMINAL_PROD_ = createNonTerminal("prod'");
		NON_TERMINAL_POW = createNonTerminal("pow");
		NON_TERMINAL_POW_ = createNonTerminal("pow'");
		NON_TERMINAL_FACT = createNonTerminal("fact");
		NON_TERMINAL_FACT_ = createNonTerminal("fact'");
		NON_TERMINAL_EXP_ELEM = createNonTerminal("exp_elem");
		NON_TERMINAL_PARAM_LIST = createNonTerminal("param_list");
		NON_TERMINAL_PARAM_LIST_ = createNonTerminal("param_list'");
		NON_TERMINAL_PARAM = createNonTerminal("param");
		
		RULE_SUM = createRule(NON_TERMINAL_EXP, "sum");

		RULE_PROD_SUM_ = createRule(NON_TERMINAL_SUM, "prod sum'");
		
		RULE_OP_PLUS_PROD_SUM_ = createRule(NON_TERMINAL_SUM_, "OP_PLUS prod sum'");
		RULE_OP_MINUS_PROD_SUM_ = createRule(NON_TERMINAL_SUM_, "OP_MINUS prod sum'");
		createRule(NON_TERMINAL_SUM_, Terminal.EPSILON);
		
		RULE_POW_PROD_ = createRule(NON_TERMINAL_PROD, "pow prod'");
		
		RULE_OP_MULT_POW_PROD_ = createRule(NON_TERMINAL_PROD_, "OP_MULT pow prod'");
		RULE_OP_DIV_POW_PROD_ = createRule(NON_TERMINAL_PROD_, "OP_DIV pow prod'");
		createRule(NON_TERMINAL_PROD_, Terminal.EPSILON);
		
		RULE_FACT_POW_ = createRule(NON_TERMINAL_POW, "fact pow'");
		
		RULE_OP_POW_POW = createRule(NON_TERMINAL_POW_, "OP_POW pow");
		createRule(NON_TERMINAL_POW_, Terminal.EPSILON);
		
		RULE_EXP_ELEM_FACT_ = createRule(NON_TERMINAL_FACT, "exp_elem fact'");
		
		RULE_OP_FACT_FACT_=  createRule(NON_TERMINAL_FACT_, "OP_FACT fact'");
		createRule(NON_TERMINAL_FACT_, Terminal.EPSILON);
		
		RULE_ID_PARAM_LIST = createRule(NON_TERMINAL_EXP_ELEM, "ID param_list");
		RULE_EXP_LIT = createRule(NON_TERMINAL_EXP_ELEM, "EXP_LIT");
		RULE_PARENS_EXP = createRule(NON_TERMINAL_EXP_ELEM, "PAREN_OPEN exp PAREN_CLOSE");

		RULE_PARENS_PARAM_PARAM_LIST_ = createRule(NON_TERMINAL_PARAM_LIST, "PAREN_OPEN param param_list' PAREN_CLOSE");
		createRule(NON_TERMINAL_PARAM_LIST, Terminal.EPSILON);

		RULE_PARAM_SEP_PARAM_PARAM_LIST_ = createRule(NON_TERMINAL_PARAM_LIST_, "PARAM_SEP param param_list'");
		createRule(NON_TERMINAL_PARAM_LIST_, Terminal.EPSILON);

		RULE_EXP = createRule(NON_TERMINAL_PARAM, "exp");

		//finalize
		setStartSymbol(NON_TERMINAL_EXP);
		
		updateParserTable();
	}

	private static ExpGrammar _instance = new ExpGrammar();

	public static ExpGrammar getInstance() {
		return _instance;
	}
}