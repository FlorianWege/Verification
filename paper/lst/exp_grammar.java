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
	
	//parser rules
	NON_TERMINAL_EXP = createNonTerminal("exp");
	NON_TERMINAL_EXP_ = createNonTerminal("exp'");
	NON_TERMINAL_FACTOR = createNonTerminal("factor");
	NON_TERMINAL_FACTOR_ = createNonTerminal("factor'");
	NON_TERMINAL_POW = createNonTerminal("pow");
	NON_TERMINAL_POW_ = createNonTerminal("pow'");
	NON_TERMINAL_FACTORIAL = createNonTerminal("factorial");
	NON_TERMINAL_FACTORIAL_ = createNonTerminal("factorial'");
	NON_TERMINAL_EXP_ELEMENTARY = createNonTerminal("exp_elementary");
	
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
	
	createRule(NON_TERMINAL_FACTORIAL, "exp_elementary factorial'");
	
	createRule(NON_TERMINAL_FACTORIAL_, "OP_FACTORIAL");
	createRule(NON_TERMINAL_FACTORIAL_, Terminal.EPSILON);
	
	createRule(NON_TERMINAL_EXP_ELEMENTARY, "ID");
	createRule(NON_TERMINAL_EXP_ELEMENTARY, "NUM");
	createRule(NON_TERMINAL_EXP_ELEMENTARY, "PAREN_OPEN exp PAREN_CLOSE");
	
	//finalize
	setStartParserRule(NON_TERMINAL_EXP);
	
	updatePredictiveParserTable();
}