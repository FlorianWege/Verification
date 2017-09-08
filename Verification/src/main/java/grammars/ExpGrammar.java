package grammars;

import core.Grammar;
import core.structures.NonTerminal;
import core.structures.Terminal;

public class ExpGrammar extends Grammar {
	public final Terminal TERMINAL_NUM;
	public final Terminal TERMINAL_PAREN_OPEN;
	public final Terminal TERMINAL_PAREN_CLOSE;
	public final Terminal TERMINAL_OP_PLUS;
	public final Terminal TERMINAL_OP_MINUS;
	public final Terminal TERMINAL_OP_MULT;
	public final Terminal TERMINAL_OP_DIV;
	public final Terminal TERMINAL_OP_POW;
	public final Terminal TERMINAL_OP_FACTORIAL;
	public final Terminal TERMINAL_ID;
	public final Terminal TERMINAL_PARAM_SEP;
	
	public final NonTerminal NON_TERMINAL_EXP;
	public final NonTerminal NON_TERMINAL_EXP_;
	public final NonTerminal NON_TERMINAL_FACTOR;
	public final NonTerminal NON_TERMINAL_FACTOR_;
	public final NonTerminal NON_TERMINAL_POW;
	public final NonTerminal NON_TERMINAL_POW_;
	public final NonTerminal NON_TERMINAL_FACTORIAL;
	public final NonTerminal NON_TERMINAL_FACTORIAL_;
	public final NonTerminal NON_TERMINAL_EXP_ELEMENTARY;

	public final NonTerminal NON_TERMINAL_PARAM_LIST;
	public final NonTerminal NON_TERMINAL_PARAM;
	public final NonTerminal NON_TERMINAL_PARAM_;
	
	public ExpGrammar() {
		super();
		
		//lexer rules
		TERMINAL_NUM = createTerminal("NUM");
		TERMINAL_PAREN_OPEN = createTerminal("PAREN_OPEN");
		TERMINAL_PAREN_CLOSE = createTerminal("PAREN_CLOSE");
		TERMINAL_OP_PLUS = createTerminal("OP_PLUS");
		TERMINAL_OP_MINUS = createTerminal("OP_MINUS");
		TERMINAL_OP_MULT = createTerminal("OP_MULT");
		TERMINAL_OP_DIV = createTerminal("OP_DIV");
		TERMINAL_OP_POW = createTerminal("OP_POW");
		TERMINAL_OP_FACTORIAL = createTerminal("OP_FACTORIAL");
		TERMINAL_ID = createTerminal("ID");
		TERMINAL_PARAM_SEP = createTerminal("PARAM_SEP");

		TERMINAL_NUM.addRuleRegEx("[1-9][0-9]*");
		TERMINAL_NUM.addRuleRegEx("0");
		
		TERMINAL_PAREN_OPEN.addRule("(");
		TERMINAL_PAREN_CLOSE.addRule(")");
		TERMINAL_OP_PLUS.addRule("+");
		TERMINAL_OP_MINUS.addRule("-");
		TERMINAL_OP_MULT.addRule("*");
		TERMINAL_OP_DIV.addRule("/");
		TERMINAL_OP_POW.addRule("^");
		TERMINAL_OP_FACTORIAL.addRule("!");
		
		TERMINAL_ID.addRuleRegEx("[a-zA-Z][a-zA-Z0-9]*");

		TERMINAL_PARAM_SEP.addRule(",");
		TERMINAL_PARAM_SEP.setSep();
		
		//parser rules
		NON_TERMINAL_EXP = createNonTerminal("exp");
		NON_TERMINAL_EXP_ = createNonTerminal("exp'");
		NON_TERMINAL_FACTOR = createNonTerminal("factor");
		NON_TERMINAL_FACTOR_ = createNonTerminal("factor'");
		NON_TERMINAL_POW = createNonTerminal("pow");
		NON_TERMINAL_POW_ = createNonTerminal("pow'");
		NON_TERMINAL_FACTORIAL = createNonTerminal("factorial");
		NON_TERMINAL_FACTORIAL_ = createNonTerminal("factorial'");
		NON_TERMINAL_EXP_ELEMENTARY = createNonTerminal("exp_elem");
		NON_TERMINAL_PARAM_LIST = createNonTerminal("param_list");
		NON_TERMINAL_PARAM = createNonTerminal("param");
		NON_TERMINAL_PARAM_ = createNonTerminal("param'");
		
		createRule(NON_TERMINAL_EXP, "factor exp'");
		
		createRule(NON_TERMINAL_EXP_, "OP_PLUS factor exp'");
		createRule(NON_TERMINAL_EXP_, "OP_MINUS factor exp'");
		createRule(NON_TERMINAL_EXP_, Terminal.EPSILON);
		
		createRule(NON_TERMINAL_FACTOR, "pow factor'");
		
		createRule(NON_TERMINAL_FACTOR_, "OP_MULT pow factor'");
		createRule(NON_TERMINAL_FACTOR_, "OP_DIV pow factor'");
		createRule(NON_TERMINAL_FACTOR_, Terminal.EPSILON);
		
		createRule(NON_TERMINAL_POW, "factorial pow'");
		
		createRule(NON_TERMINAL_POW_, "OP_POW pow");
		createRule(NON_TERMINAL_POW_, Terminal.EPSILON);
		
		createRule(NON_TERMINAL_FACTORIAL, "exp_elem factorial'");
		
		createRule(NON_TERMINAL_FACTORIAL_, "OP_FACTORIAL");
		createRule(NON_TERMINAL_FACTORIAL_, Terminal.EPSILON);
		
		createRule(NON_TERMINAL_EXP_ELEMENTARY, "ID param_list");
		createRule(NON_TERMINAL_EXP_ELEMENTARY, "NUM");
		createRule(NON_TERMINAL_EXP_ELEMENTARY, "PAREN_OPEN exp PAREN_CLOSE");

		createRule(NON_TERMINAL_PARAM_LIST, "PAREN_OPEN param param' PAREN_CLOSE");
		createRule(NON_TERMINAL_PARAM_LIST, Terminal.EPSILON);

		createRule(NON_TERMINAL_PARAM, "exp");
		createRule(NON_TERMINAL_PARAM_, "PARAM_SEP exp param'");
		createRule(NON_TERMINAL_PARAM_, Terminal.EPSILON);

		//finalize
		setStartSymbol(NON_TERMINAL_EXP);
		
		updateParserTable();
	}
}