package sql.sand.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.traversal.DFS;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.IdentityRefBox;
import soot.jimple.internal.ImmediateBox;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.sand.abstraction.*;
import sql.sand.analysis.helper.ControlDependenceGraph;
import sql.sand.analysis.helper.Dominator;
import sql.sand.analysis.helper.LayerRegion;
import sql.sand.analysis.helper.Pair;
import sql.sand.analysis.helper.TopoSort;


public class UseAnalysis {

	private Silica silica;
	private Codepoint codepoint;
	private Map<NodeInterface, Map<NodeInterface, CFGInterface>> taintNodeToCallChain = new HashMap<>();
	private Map<NodeInterface, Set<String>> taintNodeToSelectedColumn = new HashMap<>();
	private Set<NodeInterface> taintNodeFromColumnIndex = new HashSet<>();
	private Set<String> getColumnAPIs = new HashSet<>();
	private Set<Use> useSet = new HashSet<>();
	
	private Map<Key, Pair<Boolean,Set<String>>> cachedUseFlowAnalysisResult = new HashMap<>();

	
	public UseAnalysis(Silica silica)
	{
		this.silica = silica;
		this.codepoint = silica.getCodepoint();
		
		//System.out.println("silica:" + silica.getCodepoint());
		//for(CFGInterface cfg : silica.getRelevantCallerAndCallees().values())
		//	System.out.println("relevant:"+ cfg.getSignature());
		addGetColumnAPIs();
		CFGInterface entry = silica.getCallChain().entrySet().iterator().next().getValue();
		
		Map<NodeInterface, CFGInterface> callChain = new LinkedHashMap<>();
		callChain.put(silica.getCodepoint().getCallChain().entrySet().iterator().next().getKey(),
				silica.getCodepoint().getCallChain().entrySet().iterator().next().getValue());
		
		//Set<Integer> index = new HashSet<>(); index.add(0);
		UseFlowAnalysis(entry, null, callChain);
		generateUseSet();
	}

	private void addGetColumnAPIs()
	{
		getColumnAPIs.add("<android.database.Cursor: int getInt(int)>");
		getColumnAPIs.add("<android.database.Cursor: java.lang.String getString(int)>");
		getColumnAPIs.add("<android.database.Cursor: byte[] getBlob(int)>");	
		getColumnAPIs.add("<android.database.Cursor: double getDouble(int)>");
		getColumnAPIs.add("<android.database.Cursor: int getType(int)>");
		getColumnAPIs.add("<android.database.Cursor: short getShort(int)>");
		getColumnAPIs.add("<android.database.Cursor: long getLong(int)>");
		getColumnAPIs.add("<android.database.Cursor: float getFloat(int)>");
	}
	
	private void generateUseSet()
	{
		for(NodeInterface n : taintNodeToCallChain.keySet())
		{
			
			Codepoint point = new Codepoint(n, taintNodeToCallChain.get(n));
			Use use = new Use(point);

			use.setSelectedColumn(taintNodeToSelectedColumn.get(n));
			use.setIsFromIndex(taintNodeFromColumnIndex.contains(n));
			setCursorAdapterToUseAllColumns(use);
			useSet.add(use);
		}
	}
	
	private void setCursorAdapterToUseAllColumns(Use use)
	{
		Stmt stmt = (Stmt) use.getCodepoint().getStatement();
		if(stmt instanceof InvokeStmt)
		{
			String sig = null;
			try
			{
				sig = stmt.getInvokeExpr().getMethod().getSignature();
			}
			catch(Exception ex)
			{
				
			}
			
			if(sig != null && sig.equals("<android.app.Activity: void startManagingCursor(android.database.Cursor)>"))
			{
				Set<String> allColumns = new HashSet<>();
				for(List<String> columns : silica.getQueryToSelectedColumns().values())
				{
					allColumns.addAll(columns);
				}
				//System.out.println(sig);
				//System.out.println(allColumns);
				use.setSelectedColumn(allColumns);
			}
			
		}
	}
	/*
	private void generateUseSet()
	{
		for(Entry<NodeInterface, CFGInterface> nodeToCFG : taintNodeToContainingCFG.entrySet())
		{
		
			NodeInterface node = nodeToCFG.getKey();
			CFGInterface cfg = nodeToCFG.getValue();
			
			Stmt actualNode = (Stmt) ((Node<Unit>) node).getActualNode();
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
			Location location = new Location(cfg.getSignature(), sourceLineNum, byteCodeOffset);
			
	

			Use use = new Use(location, node);
			//column annotation
			Set<String> selectedColumn = taintNodeToSelectedColumn.get(node);
			use.setSelectedColumn(selectedColumn);
			//row annotation
			ControlDependenceGraph cdg;
			if(cachedControlDependenceGraph.containsKey(cfg))
				cdg = cachedControlDependenceGraph.get(cfg);
			else
			{
				cdg = new ControlDependenceGraph(cfg);
				cachedControlDependenceGraph.put(cfg, cdg);
			}
			//StringBuilder rowAnnotation = new StringBuilder("");
			
			Set<String> rowFilterByColumn = null;
			for(NodeInterface ifNode : cdg.getControlDependents(node))
			{
				if(taintNodeToSelectedColumn.containsKey(ifNode))
				{
					
					Set<String> columns = taintNodeToSelectedColumn.get(ifNode);
					if(rowFilterByColumn == null)
						rowFilterByColumn = new HashSet<>(columns);
					else
						rowFilterByColumn.addAll(columns);
				}
			}
			use.setFilterByColumn(rowFilterByColumn);
			useSet.add(use);
		}
	}
	*/
	public Set<Use> getUseSet()
	{
		return useSet;
	}
	
	//TODO: inter-procedural column annotations
	private Pair<Boolean, Set<String>> UseFlowAnalysis(CFGInterface cfg, Map<Integer, Set<String>> taintedParameterIndex, Map<NodeInterface, CFGInterface> callChain)
	{

		Map<NodeInterface, Set<Def>> inSet = new HashMap<>();
		Map<NodeInterface, Set<Def>> outSet = new HashMap<>();
		Map<NodeInterface, Set<Def>> genSet = new HashMap<>();
		
		//initialize
		for(NodeInterface n : cfg.getAllNodes())
		{	
			inSet.put(n, new HashSet<>());
			outSet.put(n, new HashSet<>());
			genSet.put(n, new HashSet<>());
			String varName = getDefVarName(n);
			if(varName != null)
			{
				Def v = new Def(n, cfg.getSignature(), varName);
				
				if(n.equals(codepoint.getNode()))
				{
					v.setTainted(true);
				}
				
				//check if input parameters are tainted
				if(taintedParameterIndex != null && !taintedParameterIndex.isEmpty())
				{
					Unit actualNode = ((Node<Unit>)n).getActualNode();
					for(ValueBox vb : actualNode.getUseBoxes())
					{
						if(vb.getValue() instanceof ParameterRef)
						{
							int index = Integer.parseInt(
									vb.getValue().toString().split(":")[0].replace("@parameter", ""));
							if(taintedParameterIndex.containsKey(index))
							{
								v.setTainted(true);
								v.setPossibleSelectedColumn(taintedParameterIndex.get(index));
							}
	 					}
					}
				}
		
				setColumnAnnotation(((Node<Unit>)n).getActualNode(), v);
				genSet.get(n).add(v);
			}

		}
		/*
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
		*/

		boolean change = true;

		//LayerRegion lr = new LayerRegion(null);
		//List<NodeInterface> dfsNode = TopoSort.topoSort(cfg.getAllNodes(), lr.identifyBackEdges(cfg.getAllNodes(), cfg.getAllEdges(), cfg.getEntryNode()));
		List<NodeInterface> dfsNode = DFS.dfs(cfg.getAllNodes(), cfg.getEntryNode());
		
		/*
		if(cfg.getSignature().contains("<com.cyberlink.you.database.e: com.cyberlink.you.database.Group a(java.util.List)>"))
		{	
			System.out.println(cfg.toDot());
			for(NodeInterface n : dfsNode)
			{
				Stmt actualNode = ((Node<Stmt>)n).getActualNode();
				System.out.println(n.getOffset() + ":" + actualNode);
			}
		}
		*/
		while (change) {
			change = false;
			for (NodeInterface n : dfsNode) {
				// Union the out set of all the nodes in the predecessor
				// list，add to the in set of node n
				for(EdgeInterface e:n.getInEdges())
				{
					//Reaching Definition
					for (Def outnode : outSet.get(e.getSource()))
						if (!inSet.get(n).contains(outnode))
							inSet.get(n).add(outnode);
				}
				
				//check if any used variable is tainted 
				Pair<Boolean, Set<String>> useTaint = new Pair<Boolean, Set<String>>(false, null);
				//Set<String> possibleSelectColumn = null;
				Stmt actualNode = ((Node<Stmt>)n).getActualNode();
				if(actualNode != null)
				{
					//need to further analyze the callee
					if(codepoint.getRelevantCallersAndCallees().containsKey(n))
					{
						CFGInterface calleeCFG = codepoint.getRelevantCallersAndCallees().get(n);
						int paraIndex = 0;
						Map<Integer, Set<String>> taintedPara = new HashMap<>();
						for(ValueBox vb : actualNode.getUseBoxes())
						{
							if(vb instanceof ImmediateBox)
							{
								String varName = vb.getValue().toString();
								for(Def innode : inSet.get(n))
								{
									if(innode.getVarName().equals(varName))
									{ 
										if(innode.isTainted())
										{
											taintedPara.put(paraIndex, innode.getPossibleSelectedColumn());
										}
									}
								}
								paraIndex++;
							}
						}
						Key key = new Key(calleeCFG, taintedPara);
						if(cachedUseFlowAnalysisResult.containsKey(key))
						{
							useTaint = cachedUseFlowAnalysisResult.get(key);
						}
						else
						{
							
							Map<NodeInterface, CFGInterface> copyCallChain = new LinkedHashMap<>();
							for(Entry<NodeInterface, CFGInterface> entry : callChain.entrySet())
								copyCallChain.put(entry.getKey(), entry.getValue());
							copyCallChain.put(n, calleeCFG);
						
							useTaint = UseFlowAnalysis(calleeCFG, taintedPara, copyCallChain);
							cachedUseFlowAnalysisResult.put(key, useTaint);
						}
					}

					//other case
					else
					{
						//System.out.println(actualNode.getClass() + ":" + actualNode);
						//System.out.println(actualNode.getUseBoxes());
						
						//only pass the direct use along
						boolean isDirectAssign = (actualNode instanceof AssignStmt) && !actualNode.containsInvokeExpr();
						
						boolean isBasicTransformAssign = false;
						if(actualNode.containsInvokeExpr())
						{
							try
							{
								String className = actualNode.getInvokeExpr().getMethod().getDeclaringClass().getName();
								if(className.startsWith("java.lang.") || className.equals("android.database.Cursor"))
									isBasicTransformAssign = true;
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}
						}
					
						if(isDirectAssign || isBasicTransformAssign)
						{
							for(ValueBox vb : actualNode.getUseBoxes())
							{
								String varName = vb.getValue().toString();
								

								for(Def innode : inSet.get(n))
								{
									if(hasSameName(innode.getVarName(), varName))
									//if(innode.getVarName().equals(varName))
									{ 
										 if(innode.isTainted())
											 useTaint.setFirst(true);
										 if(innode.getPossibleSelectedColumn() != null)
										 {
											 if(useTaint.getSecond() == null)
												 useTaint.setSecond(new HashSet<>());
											 for(String column : innode.getPossibleSelectedColumn())
											 {
												 if(!useTaint.getSecond().contains(column))
													 useTaint.getSecond().add(column);
											 }
										 } 
									}
								}
							}
						}
					}
				}
				
				
				
				// Union the gen set with (in set - kill set) to the out set of node rn
				//Reaching Definition
				if(!genSet.get(n).isEmpty())
					for (Def gennode : genSet.get(n))
					{	
						//if a tainted object is used in a define statement, 
						//the defined variable is also tainted
						if(useTaint.getFirst())
							gennode.setTainted(true);
						if(useTaint.getSecond() != null)
						{
							gennode.setPossibleSelectedColumn(new HashSet<>(useTaint.getSecond()));
						}
						
						//Union gen set to out set
						if (!outSet.get(n).contains(gennode))
						{
							change = true;
							outSet.get(n).add(gennode);
						}
					}

				if(genSet.get(n).isEmpty())
				{	
					for(Def innode : inSet.get(n))
						if (!outSet.get(n).contains(innode))
						{
							change = true;
							outSet.get(n).add(innode);
						}
				}
				//kill
				else
				{
					
					Set<String> genVarName = new HashSet<>();
					for(Def d : genSet.get(n))
						genVarName.add(d.getVarName());
					
					for (Def innode : inSet.get(n))
						if (!outSet.get(n).contains(innode)&&!genVarName.contains(innode.getVarName()))
						{
							change = true;
							outSet.get(n).add(innode);
						}
				}

			}
		}
		
		boolean isReturnPossiblyTainted = false;
		Set<String> returnSelectedColumns = null;
		
		//Check the possible uses of the result, add the use information. 
		//For inter-procedural analysis purpose,
		//if the use is returned by a method, the method return a true value indicating
		//the return value of the method is tainted.
		for(NodeInterface n : cfg.getAllNodes())
		{
			Stmt actualNode = ((Node<Stmt>)n).getActualNode();

			if(actualNode != null)
			{
				boolean isReturnStmt = actualNode instanceof ReturnStmt;
				//check if any used variable is tainted 
				for(ValueBox vb : actualNode.getUseBoxes())
				{
					String varName = vb.getValue().toString();
					for(Def innode : inSet.get(n))
					{
						if(hasSameName(innode.getVarName(), varName))
						//if(innode.getVarName().equals(varName))
						{ 
							if(innode.isTainted())
							{
								taintNodeToCallChain.put(n, callChain);
								
								if(isReturnStmt)
								{
									isReturnPossiblyTainted = true;
								}
							}
							if(innode.getPossibleSelectedColumn() != null)
							{
								if(innode.isFromIndex())
									taintNodeFromColumnIndex.add(n);
								
								if(actualNode.containsInvokeExpr() && 
									getColumnAPIs.contains(actualNode.getInvokeExpr().getMethod().getSignature()))
								{
									//skip outputting the case: int i = cursor.getColumnIndex("user")
									//cursor.getInt(i) as use of columns, but they are tainted;
										continue;
								}
								
								if(taintNodeToSelectedColumn.containsKey(n))
									taintNodeToSelectedColumn.get(n).addAll(innode.getPossibleSelectedColumn());
								else
									taintNodeToSelectedColumn.put(n, new HashSet<>(innode.getPossibleSelectedColumn()));
								if(isReturnStmt)
								{
									//return the column annotations
									returnSelectedColumns = new HashSet<>(innode.getPossibleSelectedColumn());
								}
							}
						}
					}
				}
				
			}
		}
		return new Pair<Boolean, Set<String>>(isReturnPossiblyTainted, returnSelectedColumns);
		
	}
	
	private boolean hasSameName(String inVarName, String genVarName)
	{
		boolean isArrayElement = inVarName.contains("[") && !inVarName.contains("[]");
		if(!isArrayElement)
			return inVarName.equals(genVarName);
		else
		{
			return inVarName.substring(0, inVarName.indexOf("[")).equals(genVarName);
		}
	}
	//long value = cursor.getLong(1)
	//int index = cursor.getColumnIndex("user")
	//annotate Def value with the corresponding column 
	//TODO: alias analysis of the cursor object (can help to improve detection recall)
	private void setColumnAnnotation(Unit actualNode, Def gen)
	{
		Stmt stmt = (Stmt) actualNode;
		if(stmt.containsInvokeExpr())
		{
			String sig = stmt.getInvokeExpr().getMethod().getSignature();
			if(getColumnAPIs.contains(sig))
			{
				for(ValueBox vb : actualNode.getUseBoxes())
				{
					if(vb instanceof ImmediateBox)
					{
						//value = cursor.getInt(0)
						if(vb.getValue() instanceof Constant)
						{
							//analyze table and get the column from the index
							int index = Integer.parseInt(vb.getValue().toString());
							for(List<String> columns : silica.getQueryToSelectedColumns().values())
							{
								//TODO: better handling of mapping different queries to corresponding column uses.
								if(index < columns.size())
								{
									String column = columns.get(index);
									annoteDefWithSelectedColumns(gen, column);
									//is from index
									gen.setIsFromIndex(true);
								}
							}
							if(gen.getPossibleSelectedColumn() == null)
								annoteDefWithSelectedColumns(gen, "$$$");
						}
						else 
						{
							//TODO: Integer Analysis
							
							//We are currently not performing a precise integer analysis. 
							//In case that the integer is not a constant,
							//we safely and assume that all selected columns are used

							annoteDefWithSelectedColumns(gen, "$$$");
							/*
							for(List<String> columns : silica.getQueryToSelectedColumns().values())
							{
								//TODO: better handling of mapping different queries to corresponding column uses.
								for(String column : columns)
								{
									annoteDefWithSelectedColumns(gen, column);
								}
							}
							*/
						}
					}
				}
			}
			
			else if(sig.equals("<android.database.Cursor: int getColumnIndex(java.lang.String)>")
					|| sig.equals("<android.database.Cursor: int getColumnIndexOrThrow(java.lang.String)>"))
			{
				for(ValueBox vb : actualNode.getUseBoxes())
				{
					if(vb instanceof ImmediateBox)
					{
						if(vb.getValue() instanceof Constant)
						{

							Set<String> possibleSelectedColumn = new HashSet<>();
							possibleSelectedColumn.add(vb.getValue().toString().toLowerCase().replace("\"", ""));
							gen.setPossibleSelectedColumn(possibleSelectedColumn);
						}
						else
						{
							//TODO: String analysis
							
							//We are currently not performing a string analysis for this API. 
							//In case that the string parameter is not a constant,
							//we tend to be safe and assume that all selected columns are used

							annoteDefWithSelectedColumns(gen, "$$$");
							/*
							for(List<String> columns : silica.getQueryToSelectedColumns().values())
							{
								for(String column : columns)
								{
									annoteDefWithSelectedColumns(gen, column);
								}
							}
							*/
						}
					}
				}
			}
		}
	}

	private void annoteDefWithSelectedColumns(Def gen, String column) {
		if(gen.getPossibleSelectedColumn() == null)
		{
			Set<String> possibleSelectedColumn = new HashSet<>();
			possibleSelectedColumn.add(column);
			gen.setPossibleSelectedColumn(possibleSelectedColumn);
		}
		else
		{
			Set<String> possibleSelectedColumn = gen.getPossibleSelectedColumn();
			if(!possibleSelectedColumn.contains(column))
				gen.getPossibleSelectedColumn().add(column);
		}
	}
	

	
	protected static String getDefVarName(NodeInterface n)
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
				return temp.getDefBoxes().get(0).getValue().toString();
			}		
		}
	}

}

class Key{
	private CFGInterface cfg;
	private Map<Integer, Set<String>> taintedPara;
	public Key(CFGInterface cfg, Map<Integer, Set<String>> taintedPara)
	{
		this.cfg = cfg;
		this.taintedPara = taintedPara;
	}
	
	@Override
	public int hashCode() {
		return cfg.hashCode() * 31 + taintedPara.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		Key other = (Key) obj;
		return other.cfg.equals(this.cfg) && other.taintedPara.equals(this.taintedPara);
	} 
}


class Def{
	private String varName;
	private int position;
	private String methodName;
	private boolean isTainted = false;
	private Set<String> possibleSelectedColumn;
	private boolean isFromIndex = false;
	
	public Def(NodeInterface n,String methodName)
	{
		position = n.getOffset();
		varName = UseAnalysis.getDefVarName(n);
		this.methodName = methodName;
	}
	public Def(NodeInterface n,String methodName,String varName)
	{

		position = n.getOffset();
		this.varName = varName;
		this.methodName = methodName;
	}
	
	public boolean isTainted()
	{
		return isTainted;
	}
	
	public void setTainted(boolean isTainted)
	{
		this.isTainted = isTainted;
	}
	
	public void setIsFromIndex(boolean isFromIndex)
	{
		this.isFromIndex = isFromIndex;
	}
	
	public boolean isFromIndex()
	{
		return this.isFromIndex;
	}
	
	public Set<String> getPossibleSelectedColumn()
	{
		return this.possibleSelectedColumn;
	}
	
	public void setPossibleSelectedColumn(Set<String> possibleSelectedColumn)
	{
		this.possibleSelectedColumn = possibleSelectedColumn;
	}
	
	@Override
	public boolean equals(Object o)
	{
        if (o == this) { 
            return true; 
        } 
        if (!(o instanceof Def)) { 
            return false; 
        } 
        Def c = (Def) o; 
         
        return this.position == c.position
                && this.varName.equals(c.varName)
                && this.methodName.endsWith(c.methodName); 
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
				
				//if(!temp.getDefBoxes().get(0).getValue().getType().toString().equals(targetType)
				//		&&!temp.getDefBoxes().get(0).getValue().getType().toString().equals("android.database.sqlite.SQLiteStatement"))
				//	return null;
				
				//if(temp.getDefBoxes().get(0).getValue() instanceof FieldRef)
				//	return ((FieldRef)temp.getDefBoxes().get(0).getValue()).getField().getSignature();
				//else
					return temp.getDefBoxes().get(0).getValue().toString();
			}		
		}
	}
	
	public String getVarName() {
		return varName;
	}
	
	public int getPosition() {
		return position;
	}
	
	public String getMethodName()
	{
		return methodName;
	}
}
