package core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * left side of a rule
 */
public class SymbolKey implements Serializable {
	private final String _name;
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object other) {
		if (other instanceof String) other = new SymbolKey((String) other);
		
		if (!(other instanceof SymbolKey)) return false;
		
		return _name.equals(((SymbolKey) other)._name);
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	public SymbolKey(@Nonnull String name) {
		_name = name;
	}
}