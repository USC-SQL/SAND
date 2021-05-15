package sql.sand.abstraction;

import soot.Unit;

public class Loop extends BaseAbstraction{

	private Unit loopHeader;
	
	public Loop(Location location, Unit loopHeader)
	{
		super(location);
		this.loopHeader = loopHeader;
	}


	public Unit getLoopHeader() {
		return loopHeader;
	}
	
	public String toString() {
		return "Location: " + getLocation() + "\nHeader:" + loopHeader;
	}
}
