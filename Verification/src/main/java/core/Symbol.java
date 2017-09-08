package core;

public abstract class Symbol {
	private SymbolKey _key;
	
	public SymbolKey getKey() {
		return _key;
	}
	
	@Override
	public int hashCode() {
		return _key.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Symbol) return _key.equals(((Symbol) other).getKey());
		
		return _key.equals(other);
	}
	
	@Override
	public String toString() {
		return _key.toString();
	}
	
	public abstract String toLatexString();
	
	protected Symbol(SymbolKey key) {
		_key = key;
	}
}