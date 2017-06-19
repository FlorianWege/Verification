package core;

public abstract class Rule {
	protected RuleKey _key;
	
	public RuleKey getKey() {
		return _key;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Rule)) return false;
		
		if (!_key.equals(((Rule) other).getKey())) return false;
		
		return true;
	}
	
	protected Rule(RuleKey key) {
		_key = key;
	}
}