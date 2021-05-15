package sql.sand.abstraction;

import java.util.Map.Entry;

import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class BaseAbstraction {
	private Location location;
	private Codepoint codepoint;
	
	public BaseAbstraction(Location location)
	{
		this.location = location;
	}
	
	public BaseAbstraction(Codepoint codepoint)
	{
		this.codepoint = codepoint;
		
		Unit actualNode =  ((Node<Unit>) codepoint.getNode()).getActualNode();
		int sourceLineNum = actualNode.getJavaSourceStartLineNumber();
		int byteCodeOffset = -1;
		for(Tag t : actualNode.getTags())
		{
			if(t instanceof BytecodeOffsetTag)
			{
				byteCodeOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
			}
		}
		if(byteCodeOffset == -1)
			byteCodeOffset = actualNode.hashCode();
		CFGInterface cfg = null;
		for(CFGInterface callSite : codepoint.getCallChain().values())
		{
			cfg = callSite;
		}
		this.location = new Location(cfg.getSignature(), sourceLineNum, byteCodeOffset);
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public Codepoint getCodepoint()
	{
		return codepoint;
	}
	

}
