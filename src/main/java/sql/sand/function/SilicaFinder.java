package sql.sand.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import org.omg.CORBA.INITIALIZE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;
import CallGraph.NewNode;
import CallGraph.StringCallGraph;
import soot.ResolutionFailedException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.dava.internal.AST.ASTTryNode.container;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import soot.util.Chain;
import sql.sand.abstraction.Location;
import sql.sand.abstraction.Silica;
import sql.sand.analysis.QueryAnalysis;
import sql.sand.analysis.helper.StringResult;

public class SilicaFinder {

	private static StringCallGraph callgraph;
	private static Map<NodeInterface, CFGInterface> preStoredCFGs;
	private static Map<String, StringResult> targetStatements;  //mapping the statement id to the possible queries issued
	private static QueryAnalysis queryAnalysis;

	private static Set<String> relevantMethods;
	private static Map<String, List<String>> tableNameToColumns = new HashMap<>();
	private static Map<String, String> tableNameToForms = new HashMap<>();
	private static Map<String, Set<Silica>> cachedSilicas = new HashMap<>(); //mapping the regex + class_filter to the corresponding silicas (along different call chains)
	private static Set<String> transactionAPIs = new HashSet<>();
	private static long totalLinesOfCode;
	private static long	relevantLinesOfCode;
	private static long silicaRelevantLinesOfCode;
	private static Map<String, Long> sigToLinesOfCode;
	static {
		long t1 = System.currentTimeMillis();
		Chain<SootClass> classes =  Scene.v().getApplicationClasses();
		List<SootMethod> entryPoints = new ArrayList<>();
		Set<SootMethod> allMethods = new HashSet<>();
		for(Iterator<SootClass> iter = classes.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			if(!sc.getName().startsWith("android.support.v"))
            {
                sc.setApplicationClass();
                allMethods.addAll(sc.getMethods());
            }
			for(SootMethod sm: sc.getMethods()){
				if(sm.isConcrete())
					entryPoints.add(sm);
			}
		}
		Scene.v().loadNecessaryClasses();
		Scene.v().setEntryPoints(entryPoints);
		CHATransformer.v().transform();
		CallGraph cg = Scene.v().getCallGraph();
		long t2 = System.currentTimeMillis();
		System.out.println("Running soot takes " + (t2 - t1) + "ms");
		sigToLinesOfCode = new HashMap<>();

		preStoredCFGs = new HashMap<>();

		targetStatements = new HashMap<>();
		long t3 = System.currentTimeMillis();
		callgraph = new StringCallGraph(cg, allMethods);
		queryAnalysis = new QueryAnalysis(callgraph);
		long t4 = System.currentTimeMillis();
		System.out.println("Running string analysis takes " + (t4 - t3) + "ms");
		extractTableFormsAndColumns();
		
		Set<String> relevantAPIs = new HashSet<>();
		
		transactionAPIs.add("<android.database.sqlite.SQLiteDatabase: void endTransaction()>");
		transactionAPIs.add("<android.database.sqlite.SQLiteDatabase: void beginTransaction()>");
		transactionAPIs.add("<android.database.sqlite.SQLiteDatabase: void beginTransactionNonExclusive()>");
		transactionAPIs.add("<android.database.sqlite.SQLiteDatabase: void beginTransactionWithListener(android.database.sqlite.SQLiteTransactionListener)>");
		transactionAPIs.add("<android.database.sqlite.SQLiteDatabase: void beginTransactionWithListenerNonExclusive(android.database.sqlite.SQLiteTransactionListener)>");
		relevantAPIs.addAll(transactionAPIs);
		
		relevantAPIs.add("<android.database.Cursor: int getInt(int)>");
		relevantAPIs.add("<android.database.Cursor: java.lang.String getString(int)>");
		relevantAPIs.add("<android.database.Cursor: byte[] getBlob(int)>");	
		relevantAPIs.add("<android.database.Cursor: double getDouble(int)>");
		relevantAPIs.add("<android.database.Cursor: int getType(int)>");
		relevantAPIs.add("<android.database.Cursor: short getShort(int)>");
		relevantAPIs.add("<android.database.Cursor: long getLong(int)>");
		relevantAPIs.add("<android.database.Cursor: float getFloat(int)>");
		relevantAPIs.add("<android.database.Cursor: int getColumnIndex(java.lang.String)>");
		relevantAPIs.add("<android.database.Cursor: int getColumnIndexOrThrow(java.lang.String)>");
		
		relevantAPIs.add("<android.app.Activity: void startManagingCursor(android.database.Cursor)>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String,java.lang.Object[])>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[])>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[],android.os.CancellationSignal)>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQueryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,java.lang.String,java.lang.String[],java.lang.String)>");
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQueryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,java.lang.String,java.lang.String[],java.lang.String,android.os.CancellationSignal)>");
		
		relevantMethods = identifyRelevantMethods(relevantAPIs);
		
		computeDBrelevantLinesOfCode(relevantAPIs);

		long t5 = System.currentTimeMillis();
		System.out.println("Total set up time " + (t5 - t1) + "ms");
	}
	
	private static void computeDBrelevantLinesOfCode(Set<String> rAPIs)
	{
		Set<String> relevantMethods = new HashSet<>();
		Set<String> relevantAPIs = new HashSet<>(rAPIs);
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: long insert(java.lang.String,java.lang.String,android.content.ContentValues)>");

		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: int update(java.lang.String,android.content.ContentValues,java.lang.String,java.lang.String[])>");
		//target.put("<android.database.sqlite.SQLiteDatabase: int updateWithOnConflict(java.lang.String,android.content.ContentValues,java.lang.String,java.lang.String[],int)>", paraSet3);

		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: int delete(java.lang.String,java.lang.String,java.lang.String[])>");
		
		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String)>");

		relevantAPIs.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)>");
		for(NewNode n: callgraph.getRTOdering())
		{
			if(n.getMethod().isConcrete()
					&& !n.getMethod().getDeclaringClass().isAbstract() && !containLib(n.getMethod().getSignature()))
			{
				boolean isRelevant = false;
				for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
				{
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						SootMethod sm = null;
						try 
						{
							sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
						}
						catch(Exception e)
						{
							continue;
						}
						String sig = sm.getSignature();
						if(relevantAPIs.contains(sig) || relevantMethods.contains(sig))
						{
							isRelevant = true;
							break;
						}
					}
				}
				if(isRelevant)
				{
					relevantLinesOfCode += n.getMethod().retrieveActiveBody().getUnits().size();
					relevantMethods.add(n.getMethod().getSignature());  
				}
			}
		}
	}
	private static void clear()
	{
		//preStoredCFGs.clear();
		targetStatements.clear();
	}
	

	private static void extractTableFormsAndColumns()
	{
		for(StringResult sr : queryAnalysis.getStringAnalysisResult().values())
		{
			Set<String> queries = sr.possibleValues;
			for(String query : queries.stream()
		            .map(String::toLowerCase)
		            .collect(Collectors.toSet()))
			{
				String tableName = QueryAnalysis.parseTableName(query);
				List<String> tableColumnNames = QueryAnalysis.parseTableColumns(query);
				if(tableName != null && tableColumnNames != null)
				{
					
					if(tableNameToColumns.containsKey(tableName))
					{
						List<String> existingTableColumnNames = tableNameToColumns.get(tableName);
						
						if(!(tableColumnNames.containsAll(existingTableColumnNames) &&
							existingTableColumnNames.containsAll(tableColumnNames)))
						{
							System.err.println(tableName);
							System.err.println(existingTableColumnNames + "\n" + tableColumnNames);
							System.err.println("Found inconsistent table forms");
							
							//Use a arbitrary one
							tableNameToColumns.put(tableName, tableColumnNames);
						}
					}
					else
					{
						tableNameToColumns.put(tableName, tableColumnNames);
					}
				}
				
				String tableForm = QueryAnalysis.parseTableForm(query);
				if(tableName != null && tableForm != null)
					tableNameToForms.put(tableName, tableForm);
			}
		}
	}
	
	public static String getInputId(String input, Set<String> filter)
	{
		return input + filter;
	}
	
	//Find the statements that meet the filtering criteria
	public static Set<Silica> find(String regex, Set<String> targetClasses)
	{
		String inputId = getInputId(regex, targetClasses);
		if(cachedSilicas.get(inputId) != null)
		{
			return cachedSilicas.get(inputId);
		}
		else
		{
			//make it case insensitive
			regex = "(?i)" + regex;
			clear();
			Map<String, StringResult> stringAnalysisResult = queryAnalysis.getStringAnalysisResult();
			Set<String> callChainMethods = new HashSet<>();

			for(String callPathId : stringAnalysisResult.keySet())
			{					
				if(targetClasses != null)
				{
					String className = callPathId.split(":")[0].substring(1);
					if(!targetClasses.contains(className))
					{
						continue;
					}
				}
				
				if(containLib(callPathId))
					continue;
				
				StringResult sr = stringAnalysisResult.get(callPathId);
				Set<String> queries = sr.possibleValues;
				
				for(String query : queries)
				{
					if(query.matches(regex))
					{
						targetStatements.put(callPathId, sr);
						
						String[] methods = callPathId.split("->\n");
						for(String method : methods)
						{
							callChainMethods.add(method.split("@")[0]);
						}
						
						break;
					}
				}
			}
		
			//callChainMethods.addAll(addTransactionAPIs(regex, targetClasses));
			Set<Silica> targetSilicas = findTargetSilicas(callChainMethods);
			
			cachedSilicas.put(inputId, targetSilicas);
			
			Set<String> silicaMethods = new HashSet<>();
			for(Silica s : targetSilicas)
			{
				for(CFGInterface cfg :s.getRelevantCallerAndCallees().values())
				{
					if(!containLib(cfg.getSignature()))
						silicaMethods.add(cfg.getSignature());
				}
			}
			for(String sig : silicaMethods)
			{
				if(sigToLinesOfCode.get(sig) == null)
					System.err.println("Bug in finding lines of code for method:" + sig);
				else
					silicaRelevantLinesOfCode += sigToLinesOfCode.get(sig);
			
			}
			System.out.println("Total_lines:" + totalLinesOfCode + " Relevant_lines:" + relevantLinesOfCode + " Silica_lines:"+ silicaRelevantLinesOfCode);
			return targetSilicas;
		}
	}

	/*
	public static Set<Silica> findByAPI(Set<String> targetAPI, Set<String> targetClasses)
	{
		String inputId = getInputId(targetAPI.toString(), targetClasses);
		if(cachedSilicas.containsKey(inputId))
			return cachedSilicas.get(inputId);
		else
		{
			clear();
			Set<String> callChainMethods = new HashSet<>();
			for(NewNode n: callgraph.getRTOdering())
			{
				if(targetClasses != null)
				{
					if(!targetClasses.contains(n.getMethod().getDeclaringClass().getName()))
					{
						continue;
					}
				}
				if(n.getMethod().isConcrete() && !containLib(n.getMethod().getSignature())
						&& !n.getMethod().getDeclaringClass().isAbstract())
				{
					boolean isOnCallChain = false;
					for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
					{
						if(((Stmt)actualNode).containsInvokeExpr())
						{
							SootMethod sm = null;
							try 
							{
								sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
							}
							catch(Exception e)
							{
								continue;
							}
							String sig = sm.getSignature();
							if(targetAPI.contains(sig))
							{

								targetStatements.put(getAPIIdentifierFromUnit(actualNode, n.getMethod().getSignature()), mapAPItoStringCommand(sig));
							}
							//is on the call chain to the target statements
							if(targetAPI.contains(sig)||callChainMethods.contains(sig))
							{
								isOnCallChain = true;
							}
						}
					}
					if(isOnCallChain)
					{
						callChainMethods.add(n.getMethod().getSignature());
					}

				}
			}
	
			Set<Silica> targetSilicas = findTargetSilicas(callChainMethods);
			cachedSilicas.put(inputId, targetSilicas);
			return targetSilicas;
		}
	}
	*/

	/**
	 * call chain methods is a subset of relevant methods
	 */
	private static Set<Silica> findTargetSilicas(Set<String> callChainMethods) {
		Set<Silica> targetSilicas = new HashSet<>();
		//find the target silicas and compute the call chains
		for(NewNode n: callgraph.getHeads())
		{
			if(!callChainMethods.contains(n.getMethod().getSignature()))
				continue;
			
			CFGInterface cfg = new SootCFG(n.getMethod().getSignature(),n.getMethod());
			removeExceptionBlock(cfg);
			//create a fake node standing for the call graph entry
			Node<Unit> entry = new Node<Unit>();
			entry.setName("entry");
			entry.setNodeContent("entry");
			entry.setActualNode(null);
			entry.setOffset(-100);
			Map<NodeInterface, CFGInterface> callChain = new LinkedHashMap<>();
			callChain.put(entry, cfg);
			addToTargetSilicas(cfg, callChainMethods, callChain, targetSilicas);
		}
		
		//TODO: relevantCallees are removed for now for performance concern
		/*
		for(Silica silica : targetSilicas)
		{
			silica.setRelevantCallersAndCallees(silica.getCallChain());
		}
		*/
		
		
		Map<NodeInterface, Silica> nodeToSilica = new HashMap<>();
		for(Silica silica : targetSilicas)
			nodeToSilica.put(silica.getNode(), silica);
		for(Silica silica : targetSilicas)
		{
			//call graph entry that can reach the silica
			Entry<NodeInterface, CFGInterface> entryToCFG = silica.getCallChain().entrySet().iterator().next();
			CFGInterface entry = entryToCFG.getValue();
			Map<NodeInterface, CFGInterface> relevantCFGs = new HashMap<>();
			relevantCFGs.put(entryToCFG.getKey(), entry);
			
			Set<String> callChainForEachSilica = new HashSet<>();
			for(CFGInterface c : silica.getCallChain().values())
				callChainForEachSilica.add(c.getSignature());
			
			//compute relevant methods for each silica
			computeRelevantCFGsForSilica(entry, relevantMethods, relevantCFGs);
			silica.setRelevantCallersAndCallees(relevantCFGs);		
		}
		
		
		return targetSilicas;
	}
	
	private static void computeRelevantCFGsForSilica(CFGInterface cfg,
			Set<String> relevantCallersAndCalleesForEachSilica,
			Map<NodeInterface, CFGInterface> relevantCFGs) {
		for(NodeInterface n: cfg.getAllNodes())
		{
			Unit actualNode = (Unit) ((Node)n).getActualNode();
			if(actualNode!=null)
			{
				if(((Stmt)actualNode).containsInvokeExpr())
				{
					SootMethod sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
					String sig= sm.getSignature();
					if(preStoredCFGs.containsKey(n))
					{
						relevantCFGs.put(n, preStoredCFGs.get(n));
						computeRelevantCFGsForSilica(preStoredCFGs.get(n), relevantCallersAndCalleesForEachSilica, relevantCFGs);
					}
					else if(relevantCallersAndCalleesForEachSilica.contains(sig))
					{
						if(sm.isConcrete())
						{
							CFGInterface newCfg = new SootCFG(sig, sm);
							removeExceptionBlock(newCfg);
							preStoredCFGs.put(n, newCfg);
							relevantCFGs.put(n, newCfg);
							computeRelevantCFGsForSilica(newCfg, relevantCallersAndCalleesForEachSilica, relevantCFGs);
						}
					}
				}
			}
		}

		
	}

	private static Set<String> identifyRelevantMethods(Set<String> relevantAPIs)
	{
		Set<String> relevantMethods = new HashSet<>();

		
		for(NewNode n: callgraph.getRTOdering())
		{
			if(n.getMethod().isConcrete() && !containLib(n.getMethod().getSignature())
					&& !n.getMethod().getDeclaringClass().isAbstract())
			{
				totalLinesOfCode += n.getMethod().retrieveActiveBody().getUnits().size();
				sigToLinesOfCode.put(n.getMethod().getSignature(), (long) n.getMethod().retrieveActiveBody().getUnits().size());
				boolean isRelevant = false;
				for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
				{
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						SootMethod sm = null;
						try 
						{
							sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
						}
						catch(Exception e)
						{
							continue;
						}
						String sig = sm.getSignature();
						//if(sig.contains("a(android.database.Cursor)>"))
							//System.out.println(sig);
							
						if(relevantAPIs.contains(sig) || relevantMethods.contains(sig))
						{
							isRelevant = true;
							break;
						}
					}
				}
				if(isRelevant)
				{
					relevantMethods.add(n.getMethod().getSignature());  
				}
			}
		}
		Set<String> scc = new HashSet<>(Arrays.asList(
				"<com.avos.avoscloud.im.v2.AVIMMessageStorage: com.avos.avoscloud.im.v2.AVIMMessage createMessageFromCursor(android.database.Cursor)>",
				"<com.exacttarget.etpushsdk.a.f: com.exacttarget.etpushsdk.data.Region a(android.database.Cursor)>",
				"<com.cyberlink.you.database.f: android.util.Pair a(android.database.Cursor)>",
				"<com.cyberlink.you.database.e: com.cyberlink.you.database.Group a(android.database.Cursor)>"));

		relevantMethods.addAll(scc);
		return relevantMethods;
	}
	//recursively visit the methods that can reach the target silicas from the call graph entries
	private static void addToTargetSilicas(CFGInterface cfg, Set<String> callChainMethods, 
			Map<NodeInterface, CFGInterface> callChain, 
			Set<Silica> targetSilicas)
	{
		for(NodeInterface n: cfg.getAllNodes())
		{
			Unit actualNode = (Unit) ((Node)n).getActualNode();
			if(actualNode!=null)
			{
				if(((Stmt)actualNode).containsInvokeExpr())
				{
					SootMethod sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
					String sig = sm.getSignature();
					if(callChainMethods.contains(sig))
					{
						CFGInterface newCfg = new SootCFG(sig, sm);
						removeExceptionBlock(newCfg);
						preStoredCFGs.put(n, newCfg);
						
						Map<NodeInterface, CFGInterface> copyCallChain = new LinkedHashMap<>();
						for(Entry<NodeInterface, CFGInterface> entry : callChain.entrySet())
							copyCallChain.put(entry.getKey(), entry.getValue());
						copyCallChain.put(n, newCfg);
						addToTargetSilicas(newCfg, callChainMethods, copyCallChain, targetSilicas);
					}
					
					String findByAPIId = getAPIIdentifierFromUnit(actualNode, cfg.getSignature());
					String findByStringId = getStringIdentifierFromCallChain(actualNode, callChain);
					
					if(targetStatements.containsKey(findByAPIId) || targetStatements.containsKey(findByStringId))
					{
						Set<String> stringAnalysisResult = new HashSet<>();
						boolean isAPI = false;
						Map<String, Integer> irOp = null;
						if(targetStatements.containsKey(findByStringId))
						{
							//stringAnalysisResult = targetStatements.get(findByStringId).stream()
	                        //     .map(String::toLowerCase)
	                        //     .collect(Collectors.toSet());
							for(String result : targetStatements.get(findByStringId).possibleValues)
							{
								stringAnalysisResult.add(result.toLowerCase().replace("select*", "select *"));
							}
							isAPI = targetStatements.get(findByStringId).isAPI;
							irOp = targetStatements.get(findByStringId).irOp;
							
						}
						else if(targetStatements.containsKey(findByAPIId))
						{
							stringAnalysisResult.addAll(targetStatements.get(findByAPIId).possibleValues);
							isAPI = targetStatements.get(findByAPIId).isAPI;
							irOp = targetStatements.get(findByStringId).irOp;
						}
						/*
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
						Location location = new Location(cfg.getSignature(), actualNode.getJavaSourceStartLineNumber(), byteCodeOffset);
						*/
						Silica silica = new Silica(n, callChain, stringAnalysisResult);
						silica.setTableToColumns(tableNameToColumns);
						silica.setTableToForm(tableNameToForms);
						silica.setIROp(irOp);
						silica.setIsAPI(isAPI);
						targetSilicas.add(silica);
						
						/*
						if(cachedSilicas.containsKey(id))
							cachedSilicas.get(id).add(silica);
						else
						{
							Set<Silica> temp = new HashSet<>();
							temp.add(silica);
							cachedSilicas.put(id, temp);
						}
						*/
					}
				}
			}
		}
	}
	
	private static String getStringIdentifierFromCallChain(Unit unit, Map<NodeInterface, CFGInterface> callChain)
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
				output.append("@");
				output.append(getBytecodeOffset(actualNode));
				output.append("->\n");
				output.append(sig);
			}
			else
			{
				output.append(sig);
			}
	
		}
		output.append("@" + unit.getJavaSourceStartLineNumber());		
		output.append("@" + getBytecodeOffset(unit));
		return output.toString();
	}

	private static int getBytecodeOffset(Unit unit)
	{
		int bytecodeOffset = -1;
		for (Tag t : unit.getTags()) {
			if (t instanceof BytecodeOffsetTag)
				bytecodeOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
		}
		if(bytecodeOffset == -1)
			bytecodeOffset = unit.hashCode();
		return bytecodeOffset;
	}

	private static String getAPIIdentifierFromUnit(Unit unit, String parentSig)
	{
		int offset = -1;
		for(Tag t : unit.getTags())
		{
			if(t instanceof BytecodeOffsetTag)
			{
				offset = ((BytecodeOffsetTag) t).getBytecodeOffset();
			}
		}
		if(offset == -1)
			offset = unit.hashCode();
		StringBuilder sb = new StringBuilder();
		sb.append(parentSig);
		sb.append("@");
		sb.append(unit.getJavaSourceStartLineNumber());
		sb.append("@");
		sb.append(offset);
		return sb.toString();
	}
	
	private static Set<String> mapAPItoStringCommand(String signature)
	{
		Set<String> result = new HashSet<>();
		if(signature.startsWith("<android.database.sqlite.SQLiteDatabase: void beginTransaction"))
		{
			result.add("begin transaction");
		}
		else if(signature.equals("<android.database.sqlite.SQLiteDatabase: void endTransaction()>"))
		{
			result.add("end transaction");
		}
		return result;
	}
	
	private static void removeExceptionBlock(CFGInterface cfg)
	{
		//remove redundant catch and finally block
		Map<NodeInterface, Boolean> marked = new HashMap<>();
		NodeInterface entry = cfg.getEntryNode();
		Set<EdgeInterface> realEdge = new HashSet<>();
		for(EdgeInterface e: cfg.getEntryNode().getOutEdges())
		{
			if(e.getLabel().equals("real"))
				realEdge.add(e);
		}
		entry.getOutEdges().retainAll(realEdge);
		for(NodeInterface n:cfg.getAllNodes())
		{
			marked.put(n, false);
		}
		Set<NodeInterface> dfsNode = new HashSet<>();
		dfs(entry,marked,dfsNode);
		cfg.getAllNodes().retainAll(dfsNode);
		
		for(NodeInterface n: dfsNode)
		{
			Set<EdgeInterface> remain = new HashSet<EdgeInterface>();
			for(EdgeInterface e:n.getInEdges())
			{
				if(dfsNode.contains(e.getSource()))
					remain.add(e);
			}
			
			n.getInEdges().retainAll(remain);
		}
		
	}

	
	
	private static void dfs(NodeInterface node,Map<NodeInterface, Boolean> marked, Set<NodeInterface> dfsNode) {
		marked.put(node, true);
		dfsNode.add(node);
		for(EdgeInterface e:node.getOutEdges())
		{
			if(!marked.get(e.getDestination()))
				dfs(e.getDestination(),marked,dfsNode);
		}
	}
	
	
	private static Set<String> identifyRelevantCalleesFromCallChain(Set<String> callChain)
	{
		Set<String> targetMethods = new HashSet<>();
		List<NewNode> rto = callgraph.getRTOdering();
		Map<String,NewNode> rtoMap = callgraph.getRTOMap();

		Stack<String> processMethod = new Stack<>();
		processMethod.addAll(callChain);
	
		//identify all the relevant callees
		while(!processMethod.isEmpty())
		{
			String currentMethod = processMethod.pop();

			NewNode n = rtoMap.get(currentMethod);


			if(n != null)
			{
				if(!targetMethods.contains(currentMethod))
					targetMethods.add(currentMethod);
				SootMethod sm = n.getMethod();
				if(sm.isConcrete()&&!sm.getDeclaringClass().isAbstract()&&!containLib(sm.getSignature()))
				{
					
					for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
					{
						
						if(((Stmt)actualNode).containsInvokeExpr())
						{
							
							String sig = ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
	
							if(!targetMethods.contains(sig) && rtoMap.get(sig) != null)
							{
								processMethod.push(sig);
								targetMethods.add(sig);
							}
						}
					}
				}
			}
		}
		
		//Add the constructors 
		Set<String> targetClass = new HashSet<>();
		for(String sig: targetMethods)
		{
			targetClass.add(sig.substring(0,sig.indexOf(":")+1));
		}
		for(NewNode n: rto)
		{
			String sig = n.getMethod().getSignature();
			if(!containLib(sig))
			{
				String className = sig.substring(0,sig.indexOf(":")+1);
				if(sig.contains("<init>")||sig.contains("<clinit>"))
				{
					if(targetClass.contains(className))
					{
						targetMethods.add(sig);
					}
					
				}
			}
		}
		return targetMethods;
	}
	
	private static boolean containLib(String s)
	{
		if(s.startsWith("<com.google")||
				s.startsWith("<com.facebook")||
				s.startsWith("<com.amazon")||
				s.startsWith("<com.urbanairship")||
				s.startsWith("<net.robotmedia")||
				s.startsWith("<com.localytics.android")||
				s.startsWith("<com.millennialmedia.android")||
				s.startsWith("<com.alipay"))
			return true;
		else
			return false;
	}
}
