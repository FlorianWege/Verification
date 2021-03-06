package core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public abstract class Symbol implements Serializable {
	private final SymbolKey _key;
	
	public SymbolKey getKey() {
		return _key;
	}
	
	@Override
	public int hashCode() {
		return _key.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object other) {
		if (other instanceof Symbol) return _key.equals(((Symbol) other).getKey());
		
		return _key.equals(other);
	}
	
	@Override
	public String toString() {
		return _key.toString();
	}
	
	public abstract String toLatexString();
	
	protected Symbol(@Nonnull SymbolKey key) {
		_key = key;
	}
}