package sql.sand.analysis.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import soot.Unit;
import soot.ValueBox;
import soot.coffi.CFG;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocalBox;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class ReachingDefinition {

	private CFGInterface cfg;
	private Set<NodeInterface> allNode;
	private List<NodeInterface> topoNode;
	private Map<String, List<String>> sigToSum;
	private Map<String, List<String>> sigToFieldSum;
	private String targetType;
//	private Set<ReachingDefSet> allRNode = new HashSet<ReachingDefSet>();
	private Map<NodeInterface,ReachingDefSet> reachingDefMap = new HashMap<>();
	private Map<String,String> allDef = new HashMap<>();
	public ReachingDefinition(CFGInterface cfg,String targetType,Map<String, List<String>> sigToSum,Map<String, List<String>> sigToFieldSum, List<EdgeInterface> backedge) {
		this.cfg = cfg;
		this.allNode = cfg.getAllNodes();
		this.targetType = targetType;
		this.sigToSum = sigToSum;
		this.sigToFieldSum = sigToFieldSum;
		// this.allNode=allNode;
		for (NodeInterface n : allNode) {
			ReachingDefSet rds = new ReachingDefSet();
			reachingDefMap.put(n, rds);
		//	allRNode.add(new ReachingDefSet(n));
		}
		topoNode = topoSort(allNode,backedge);
		computeReachingDefinition();
	}

	private void initialize() {
		for (NodeInterface n : allNode)
		{
			Def v = new Def(n,cfg.getSignature(),targetType);
			if(v.getVarName()!=null)
			{
				reachingDefMap.get(n).getGenSet().add(v);
				allDef.put(n.getOffset().toString(), v.getVarName());
			}
			
			//field definition
			Unit actualNode = (Unit) ((Node)n).getActualNode();
			if(actualNode!=null && ((Stmt)actualNode).containsInvokeExpr())
			{
				String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();	
				if(sigToFieldSum.get(sig)!=null)
				{
					for(String fieldName: sigToFieldSum.get(sig))
					{
						reachingDefMap.get(n).getGenSet().add(new Def(n, cfg.getSignature(), fieldName, targetType));
					}					
					//	allDef.put(n.getOffset().toString(), v.getVarName());
				}
			}
		}
	}

	
	public List<NodeInterface> topoSort(Set<NodeInterface> allNode,List<EdgeInterface> backedge)
	{
		NodeInterface entry = null;
		Map<NodeInterface,List<EdgeInterface>> inEdgeMap = new HashMap<>();
		for(NodeInterface n:allNode)
		{
			if(n.getInEdges().isEmpty())
				entry = n;
			else
			{
				List<EdgeInterface> inEdge = new ArrayList<>();
				for(EdgeInterface e:n.getInEdges())
				{
					if(!backedge.contains(e))
					inEdge.add(e);
				}
				inEdgeMap.put(n, inEdge);
			}
		}
		//L ← Empty list that will contain the sorted elements
		List<NodeInterface> L = new ArrayList<>();
		//S ← Set of all nodes with no incoming edges
		Queue<NodeInterface> S = new LinkedList<>();
		
		S.add(entry);
		while(!S.isEmpty())
		{
			NodeInterface n = S.poll();
			L.add(n);
			for(EdgeInterface e:n.getOutEdges())
			{
				if(!backedge.contains(e))
				{
					inEdgeMap.get(e.getDestination()).remove(e);
				
				if(inEdgeMap.get(e.getDestination()).isEmpty())
					S.add(e.getDestination());
				}
			}
		}
		return L;
	}

	public void computeReachingDefinition() {
		
		initialize();

		int count = 0;
		
		boolean change = true;

		while (change) {
			change = false;
			count++;
			for (NodeInterface n : topoNode) {
				// Union the out set of all the nodes in the predecessor
				// list，add to the in set of node n
				for(EdgeInterface e:n.getInEdges())
				{
					//Reaching Definition
					for (Def outnode : reachingDefMap.get(e.getSource()).getOutSet())
						if (!reachingDefMap.get(n).getInSet().contains(outnode))
							reachingDefMap.get(n).getInSet().add(outnode);
				}
				// Union the gen set with (in set - kill set) to the out set of node rn
				//Reaching Definition
				if(!reachingDefMap.get(n).getGenSet().isEmpty())
					for (Def gennode : reachingDefMap.get(n).getGenSet())
						if (!reachingDefMap.get(n).getOutSet().contains(gennode))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(gennode);
						}

				
				if(reachingDefMap.get(n).getGenSet().isEmpty())
				{	for(Def innode : reachingDefMap.get(n).getInSet())
						if (!reachingDefMap.get(n).getOutSet().contains(innode))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(innode);
						}
				}
				else
				{
			
					Set<String> genVarName = new HashSet<>();
					for(Def d :reachingDefMap.get(n).getGenSet())
						genVarName.add(d.getVarName());
					
					for (Def innode : reachingDefMap.get(n).getInSet())
						if (!reachingDefMap.get(n).getOutSet().contains(innode)&&!genVarName.contains(innode.getVarName()))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(innode);
						}
					
				}

			}
		}
		
		List<String> summary = new ArrayList<>();
		for(EdgeInterface e: cfg.getExitNode().getInEdges())
		{
			NodeInterface n = e.getSource();
			Stmt u= (Stmt) ((Node)n).getActualNode();
			if(u instanceof ReturnStmt)
			{
				if(!u.getUseBoxes().isEmpty())
				{	
				List<Pair<NodeInterface,String>> temp = new ArrayList<>();
				temp.add(new Pair<NodeInterface,String> (n,u.getUseBoxes().get(0).getValue().toString()));
				
				for(String line:getDefineLineNum(temp))
					if(!line.contains("-"))
						summary.add(line);
				}
			}
				
		}
		
		List<String> fieldSummary = new ArrayList<>();
		
		
		for(Def def: reachingDefMap.get(cfg.getExitNode()).getOutSet())
		{
			//Field defs that are not inherited from child methods
			if(def.getVarName().contains(".<"))
			{
				//System.out.println("heher"+def.getVarName());
				List<Pair<NodeInterface,String>> fieldDef = new ArrayList<>();
				fieldDef.add(new Pair<NodeInterface,String>(cfg.getExitNode(),def.getVarName()));
				if(!getDefineLineNum(fieldDef).isEmpty())
				{
					int i1 = def.getVarName().indexOf("<");
					int i2 = def.getVarName().length();
					fieldSummary.add(def.getVarName().substring(i1,i2));
				}

			}
		}
		if(!fieldSummary.isEmpty())
		{
			sigToFieldSum.put(cfg.getSignature(), fieldSummary);
		}
		
		
		
		if(!summary.isEmpty())
			sigToSum.put(cfg.getSignature(), summary);
		if(!fieldSummary.isEmpty())
		{
			//System.out.println(sigToFieldSum);
			sigToFieldSum.put(cfg.getSignature(), fieldSummary);
		}

	}

	
	public void outputToConsole()
	{
		for(NodeInterface n:allNode)
		{
			System.out.println(n.getOffset()+" "+((Node)n).getActualNode());
			System.out.print("In Set:");
			for(Def v:reachingDefMap.get(n).getInSet())
				System.out.print(v.getPosition()+ " " +v.getVarName()+"   ");
			System.out.println("");
			System.out.print("Out Set: ");
			for(Def v:reachingDefMap.get(n).getOutSet())
				System.out.print(v.getPosition()+ " " +v.getVarName()+"   ");
			System.out.println("");
			
			
		}
	}
	
	public Map<String,String> getAllDef()
	{
		return allDef;
	}
	
	public Map<String,String> getInSet(NodeInterface n)
	{
		Map<String,String> lineVarName = new HashMap<>();
		for(Def v:reachingDefMap.get(n).getInSet()) 
		{
			lineVarName.put(v.getPosition(), v.getVarName());			
		}
		return lineVarName;
	}
	//ultimate 
	public List<String> getUltimateLineNumForUse(List<Pair<NodeInterface,String>> NodeToVarName)
	{
		
		List<String> line = new ArrayList<>();
		List<Pair<NodeInterface,String>> varUse = new ArrayList<>();
		for(Pair<NodeInterface,String> p:NodeToVarName)
		{
			NodeInterface n = p.getFirst();
			String varName = p.getSecond();
		
		    if(reachingDefMap.get(n)==null)
		    {
		    	System.err.println("Node is not found.");
		    	
		    }
		  
	
		    for(Def v:reachingDefMap.get(n).getInSet())
		    {
		    	if(v.getVarName().equals(varName))
		    	{
		
		    		NodeInterface newNode = cfg.getNodeFromOffset(Integer.parseInt(v.getPosition()));
		    		Stmt u = (Stmt) ((Node)newNode).getActualNode();
		    	
		    		
		    		if(u instanceof AssignStmt)
		    		{
		    			if(u.toString().matches("(.r|r)([0-9]*)(.*) = (.r|r)([0-9]*)(.*)"))
		    			{		
		    				varUse.add(new Pair<NodeInterface,String>(newNode,((AssignStmt)u).getRightOp().toString()));
		    			}
		    			else
		    			{
		    				line.add(v.getPosition());
		    			}
		    			
		    			if(u.containsInvokeExpr())
		    			{
		    				//trace the SQLiteDatabase variable
		    				if(u.getInvokeExpr().getMethod().getSignature().equals("<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteStatement compileStatement(java.lang.String)>"))
		    				{
		    					String modVar = null;
		    					for(ValueBox vb:u.getUseBoxes())
		    					{
		    						if(vb instanceof JimpleLocalBox)
		    							modVar = vb.getValue().toString();
		    					}
		    					//System.out.println(u);
		    					//System.out.println(reachingDefMap.get(newNode).getInSet());
		    				
		    					varUse.add(new Pair<NodeInterface,String>(newNode,modVar));			    			
		    				}
		    			}
		    		}
		    		else if(u instanceof IdentityStmt)
		    		{
		    			
		    			if(((IdentityStmt)u).getRightOp().toString().contains("@parameter"))
		    				line.add("-"+((IdentityStmt)u).getRightOp().toString().split(":")[0].replaceAll("@parameter", "")+":"+cfg.getSignature());
		    		}
		      			
		    	}
		    }

		   
		}
		if(!varUse.isEmpty())
		    line.addAll(getUltimateLineNumForUse(varUse));
		return line;
	}

	//field = para0 case is ignored;
	public List<String> getDefineLineNum(List<Pair<NodeInterface,String>> NodeToVarName)
	{
		List<String> result = new ArrayList<>();
		for(String line: getUltimateLineNumForUse(NodeToVarName))
		{
			if(line.contains("-"))
				result.add(line);
			else
			{
				int offset = Integer.parseInt(line);
				Stmt u = (Stmt) ((Node)cfg.getNodeFromOffset(offset)).getActualNode();
				if(u.containsInvokeExpr())
				{
					String sig= u.getInvokeExpr().getMethod().getSignature();
					if(sigToSum.get(sig)!=null)
					{
						result.add(line);
					}
	
					/*
					else if(GlobalCallGraph.defineSig.contains(sig))
					{
						result.add(line);
					}
					*/
				}
			}
		}
		
		return result;
		
	}
	//No array
	//return the line number where the variable is defined
	
	
	public List<String> getLineNumForUse(NodeInterface n,String varName)
	{
		List<String> line = new ArrayList<>();
	    if(reachingDefMap.get(n)==null)
	    {
	    	return line;
	    }
	    
		for(Def v:reachingDefMap.get(n).getInSet())
		{
			if(v.getVarName().equals(varName))
				line.add(v.getPosition());
		}
		return line;
				
	}
	
	
	
	//With array
	/*
	public List<String> getLineNumForUse(NodeInterface n,String varName)
	{
		if(varName.contains("["))
			varName = varName.substring(0,varName.indexOf("["));
		List<String> line = new ArrayList<>();
		
		if(reachingDefMap.get(n)==null)
			return line;
		for(Def v:reachingDefMap.get(n).getInSet())
		{
			String defVarName;
			if(v.getVarName().contains("["))
				defVarName = v.getVarName().substring(0,v.getVarName().indexOf("["));
			else
				defVarName = v.getVarName();
				
			if(defVarName.equals(varName))
				line.add(v.getPosition());
		}
				
		return line;
			
	}
	*/
	public Map<String,String> getOutSet(NodeInterface n)
	{
		Map<String,String> lineVarName = new HashMap<>();
		for(Def v:reachingDefMap.get(n).getOutSet()) 
		{
			lineVarName.put(v.getPosition(), v.getVarName());			
		}
		return lineVarName;
	}
	
	
	public String toDot() {
		StringBuilder dotGraph = new StringBuilder();
		dotGraph.append("digraph directed_graph {\n\tlabel=\"" + "DU Chain" + "\";\n");
		dotGraph.append("\tlabelloc=t;\n");
		
		for(NodeInterface n:allNode)
			dotGraph.append("\t"+n.getName()+" [];\n");
		for(NodeInterface n:allNode)
		{
			Unit jimple = ((Node<Unit>)n).getActualNode();
			if(jimple!=null&&jimple.getUseBoxes()!=null)
			{
				//System.out.println(jimple);
				//boolean isReported = false;
				for(ValueBox vb:jimple.getUseBoxes())
				{
						String use = vb.getValue().toString();
						System.out.println(n.getName()+" "+use+getLineNumForUse(n,use));
					for(String lineDef:getLineNumForUse(n,use))
					{
						dotGraph.append("\t"+lineDef+" -> "+n.getName()+"[label=\""+use+"\"];\n");
					}
				
				}
			}
		}
		dotGraph.append("}\n");
		return dotGraph.toString();
	}
}
class Def{
	public String getVarName() {
		return varName;
	}
	public String getPosition() {
		return position;
	}
	public String getMethodName()
	{
		return methodName;
	}
	private String varName;
	private String position;
	private String methodName;
	private String targetType;
	public Def(NodeInterface n,String methodName, String targetType)
	{
		//System.out.println(n.toString());
		this.targetType = targetType;
		position = n.getOffset().toString();
		varName = interpret(n);
		this.methodName = methodName;
	}
	public Def(NodeInterface n,String methodName,String varName, String targetType)
	{
		//System.out.println(n.toString());
		this.targetType = targetType;
		position = n.getOffset().toString();
		this.varName = varName;
		this.methodName = methodName;
	}
	public String toString()
	{
		return methodName+"@"+position+"@"+varName;
	}
	private String interpret(NodeInterface n)
	{
		Unit temp = ((Node<Unit>)n).getActualNode();
		if(temp==null)
			return null;
		else
		{			
			if(temp.getDefBoxes().isEmpty())
				return null;
			else
			{
				
				if(!temp.getDefBoxes().get(0).getValue().getType().toString().equals(targetType)
						&&!temp.getDefBoxes().get(0).getValue().getType().toString().equals("android.database.sqlite.SQLiteStatement"))
					return null;
				
				//if(temp.getDefBoxes().get(0).getValue() instanceof FieldRef)
				//	return ((FieldRef)temp.getDefBoxes().get(0).getValue()).getField().getSignature();
				//else
					return temp.getDefBoxes().get(0).getValue().toString();
			}
		
			
				
		}
	}
}

 class ReachingDefSet {
	private Set<Def> inSet = new HashSet<>();
	private Set<Def> outSet = new HashSet<>();
	private Set<Def> genSet = new HashSet<>();

	public Set<Def> getInSet() {
		return inSet;
	}

	public void setInSet(Set<Def> inSet) {
		this.inSet = inSet;
	}

	public Set<Def> getOutSet() {
		return outSet;
	}

	public void setOutSet(Set<Def> outSet) {
		this.outSet = outSet;
	}

	public Set<Def> getGenSet() {
		return genSet;
	}

	public void setGenSet(Set<Def> genSet) {
		this.genSet = genSet;
	}

}
 class ReachingDefStack {
	private Stack<Def> inSet = new Stack<>();
	private Stack<Def> outSet = new Stack<>();
	private Stack<Def> genSet = new Stack<>();

	public Stack<Def> getInSet() {
		return inSet;
	}

	public void setInSet(Stack<Def> inSet) {
		this.inSet = inSet;
	}

	public Stack<Def> getOutSet() {
		return outSet;
	}

	public void setOutSet(Stack<Def> outSet) {
		this.outSet = outSet;
	}

	public Stack<Def> getGenSet() {
		return genSet;
	}

	public void setGenSet(Stack<Def> genSet) {
		this.genSet = genSet;
	}

}