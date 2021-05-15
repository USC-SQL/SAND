package sql.sand.abstraction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.sand.analysis.QueryAnalysis;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class Silica extends BaseAbstraction{

	private NodeInterface node;
	private Map<NodeInterface, CFGInterface> callChain;
	private Map<NodeInterface, CFGInterface> relevantCallersAndCallees;
	private Set<String> queries;
	private Map<String, List<String>> tableToColumns;
	private Map<String, String> tableToForm;
	private Location location;
	private Map<String, List<String>> cachedQueryToSelectedColumns = null;
	private Map<String, List<String>> cachedQueryToDataValues = null;
	private String meta_info;
	private boolean isAPI;
	private Map<String, Integer> irOp;
	private boolean isIterate = false;
	private boolean isFromIndex = false;
	private boolean isPlus = false;
	
	public Silica(NodeInterface node, Map<NodeInterface, CFGInterface> callChain, Set<String> stringQueries)
	{
		super(new Codepoint(node, callChain));
		//super(location);
		this.node = node;
		this.callChain = callChain;
		this.queries = stringQueries;
		
	}

	public void setIsAPI(boolean isAPI)
	{
		this.isAPI = isAPI;
	}
	
	public boolean isAPI()
	{
		return isAPI;
	}
	
	public void setIROp(Map<String, Integer> irOp)
	{
		this.irOp = irOp;
	}
	
	public Map<String, Integer> getIROp()
	{
		return this.irOp;
	}
	
	public boolean isHardCoded()
	{
		if(irOp == null)
			return false;
		else
		{
			if(irOp.isEmpty())
				return true;
			else
				return false;
		}
	}
	
	public boolean isIterate()
	{
		return this.isIterate;
	}
	
	public void setIterate(boolean isIterate)
	{
		this.isIterate = isIterate;
	}
	
	public void setMetaInfo(String meta_info)
	{
		this.meta_info = meta_info;
	}
	
	public String getMetaInfo() 
	{
		return meta_info;
	}
	
	public NodeInterface getNode() {
		return node;
	}
	
	public Unit getUnit() {
		return (Unit) ((Node)node).getActualNode();
	}

	/**
	 * get the call chain from the call graph entry to the method that contains the Silica
	 * the call chain is represented in a ordered map mapping the method signature to the CFG
	 */
	public Map<NodeInterface, CFGInterface> getCallChain() {
		return callChain;
	}
	
	public void setRelevantCallersAndCallees(Map<NodeInterface, CFGInterface> relevantCFGs) {
		relevantCallersAndCallees = relevantCFGs;
		getCodepoint().setRelevantCallersAndCallees(relevantCFGs);
	}
	
	/**
	 * get the relevant methods from the call graph entry to the method that contains the Silica
	 * also the methods from the call graph entry that can reach the Silica to the methods that are relevant
	 * (the call chain is essentially the relevant callers, which is a subset of the return object)
	 */
	public Map<NodeInterface, CFGInterface> getRelevantCallerAndCallees()
	{
		return relevantCallersAndCallees;
	}
	
	public void setStringQueries(Set<String> queries)
	{
		this.queries = queries;
	}
	
	public Set<String> getStringQueries()
	{
		return queries;
	}
	
	//mapping a query to a list of columns that the target table contains
	public Map<String, List<String>> getQueryToSelectedColumns()
	{
		if(cachedQueryToSelectedColumns == null)
		{
			cachedQueryToSelectedColumns = new HashMap<>();
			for(String query : queries)
			{
				List<String> columns = QueryAnalysis.getSelectedColumns(query, tableToColumns);
				if(columns != null)
					cachedQueryToSelectedColumns.put(query, columns);
			}

		}
		return cachedQueryToSelectedColumns;
	}
	
	public Map<String, List<String>> getQueryToDataValues()
	{
		if(cachedQueryToDataValues == null)
		{
			cachedQueryToDataValues = new HashMap<>();
			for(String query : queries)
			{
				List<String> dataValues = QueryAnalysis.getDataValues(query);
				if(dataValues != null)
					cachedQueryToDataValues.put(query, dataValues);
			}
		}
		return cachedQueryToDataValues;
	}
	
    @Override
    public boolean equals(Object obj) {
        return this.node.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }
    
    public String toString() {
    	return getUnit().toString();
    }
    
    //mapping a table to a list of columns it contains
	public Map<String, List<String>> getTableToColumns() {
		return tableToColumns;
	}

	public void setTableToColumns(Map<String, List<String>> tableToColumns) {
		this.tableToColumns = tableToColumns;
	}

	public Map<String, String> getTableToForm() {
		return tableToForm;
	}
	public void setTableToForm(Map<String, String> tableNameToForm) {
		// TODO Auto-generated method stub
		this.tableToForm = tableNameToForm;
		
	}

	public boolean isFromIndex() {
		return isFromIndex;
	}

	public void setFromIndex(boolean isFromIndex) {
		this.isFromIndex = isFromIndex;
	}

	public void setIsPlus(boolean plus) {
		this.isPlus = plus;
	}
	
	public boolean isPlus()
	{
		return this.isPlus;
	}

	
}
