package sql.sand.abstraction;

import java.util.Map;

import soot.Unit;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class Location {

	private int bytecodeId;
	private int sourceLineNumber;
	private String methodSignature;
	
	public Location(String methodSignature, int sourceLineNumber, int bytecodeId)
	{
		this.setMethodSignature(methodSignature);
		this.setSourceLineNumber(sourceLineNumber);
		this.setBytecodeId(bytecodeId);
	}
	
	public int getBytecodeId() {
		return bytecodeId;
	}

	public void setBytecodeId(int bytecodeId) {
		this.bytecodeId = bytecodeId;
	}

	public int getSourceLineNumber() {
		return sourceLineNumber;
	}

	public void setSourceLineNumber(int sourceLineNumber) {
		this.sourceLineNumber = sourceLineNumber;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public String toString() {
		return methodSignature + "@" + sourceLineNumber;
	}
	
    @Override
    public boolean equals(Object obj) {
    	if(!(obj instanceof Location))
    		return false;
    	
    	Location targetLocation = (Location) obj;
        return targetLocation.methodSignature.equals(methodSignature)
        		&& targetLocation.bytecodeId == bytecodeId
        		&& targetLocation.sourceLineNumber == sourceLineNumber;
    }
}
