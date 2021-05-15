package sql.sand.abstraction;

import java.util.Map;
import java.util.Map.Entry;

import soot.Unit;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class Codepoint {
	private NodeInterface node;
	private Map<NodeInterface, CFGInterface> callChain;
	private Map<NodeInterface, CFGInterface> relevantCallersAndCallees;
	private Unit statement;
	
	public Codepoint(NodeInterface node,
			Map<NodeInterface, CFGInterface> callChain)
	{
		this.node = node;
		this.callChain = callChain;
		this.statement =  ((Node<Unit>) node).getActualNode();
	}
	
	public Codepoint(NodeInterface node,
			Map<NodeInterface, CFGInterface> callChain,
			Map<NodeInterface, CFGInterface> relevantCallersAndCallees)
	{
		this.node = node;
		this.callChain = callChain;
		this.relevantCallersAndCallees = relevantCallersAndCallees;
		this.statement =  ((Node<Unit>) node).getActualNode();
	}
	
	public NodeInterface getNode() {
		return node;
	}
	
	public void setNode(NodeInterface node) {
		this.node = node;
	}
	
	public Map<NodeInterface, CFGInterface> getCallChain() {
		return callChain;
	}
	
	public void setCallChain(Map<NodeInterface, CFGInterface> callChain) {
		this.callChain = callChain;
	}
	
	public Map<NodeInterface, CFGInterface> getRelevantCallersAndCallees() {
		return relevantCallersAndCallees;
	}
	
	public void setRelevantCallersAndCallees(
			Map<NodeInterface, CFGInterface> relevantCallersAndCallees) {
		this.relevantCallersAndCallees = relevantCallersAndCallees;
	}
	
	public Unit getStatement()
	{
		return statement;
	}
	
	public int getSourceLineNumer()
	{
		return statement.getJavaSourceStartLineNumber();
	}
	
    @Override
    public boolean equals(Object obj) {
    	if(!(obj instanceof Codepoint))
    		return false;
    	else
    	{
    		return this.node.equals(((Codepoint)obj).getNode());
    	}
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }
    
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		//int length = silica.getCallChain().size();
		//int count = 0;
		for(Entry<NodeInterface, CFGInterface> callSiteNodeToCfg : callChain.entrySet())
		{
			Unit actualNode = ((Node<Unit>)callSiteNodeToCfg.getKey()).getActualNode();
			String sig = callSiteNodeToCfg.getValue().getSignature();
			if(actualNode != null)
			{
				output.append("@");
				output.append(actualNode.getJavaSourceStartLineNumber());
				output.append("->\n");
				output.append(sig);
			}
			else
			{
				output.append(sig);
			}
	
		}
		output.append("@" + getStatement().getJavaSourceStartLineNumber());
		return output.toString();
	}
}
