package core;

public class HoareConditionNeg extends HoareCondition {
	private HoareCondition _base = null;
	
	@Override
	public String toString() {
		return "not " + _base.toString();
	}
	
	public HoareConditionNeg(HoareCondition base) {
		super();		
	}
}