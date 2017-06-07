package core;

/**
 * left side of a rule
 */
public class RuleKey {
	private String _name;
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RuleKey)) return false;
		
		return _name.equals(((RuleKey) other)._name);
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	public RuleKey(String name) {
		_name = name;
	}
}