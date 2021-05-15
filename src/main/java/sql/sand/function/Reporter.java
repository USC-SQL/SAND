package sql.sand.function;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import edu.usc.sql.graphs.Edge;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import fj.data.Either.LeftProjection;
import soot.Unit;
import sql.sand.abstraction.Silica;
import sql.sand.analysis.QueryAnalysis;
import sql.sand.analysis.helper.Keyword;
import sql.sand.analysis.helper.Pair;
import sql.sand.detector.TaintedQueryDetector;

public class Reporter {

	
	private PrintWriter pw;
	private String outputPath;
	
	private Set<String> keywords = new HashSet<>();
	private Pattern p = Pattern.compile("(?:^|\\s)'([^']*?)'(?:$|\\s)", Pattern.MULTILINE);
	
	private Map<Silica, Set<String>> cachedQueryForm = new HashMap<>();
	private Map<Silica, Set<String>> cachedTableForm = new HashMap<>();
	
	private int index = 0;
	public static boolean isCached = true;
	
	public Reporter(String outputPath)
	{
		
		this.outputPath = outputPath;
		for(String keyword : Keyword.KEYWORDS){
			keywords.add(keyword);
		}
		try {
			pw = new PrintWriter(new FileWriter(outputPath,true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String printCallChain(Silica silica)
	{
		StringBuilder output = new StringBuilder();
		//int length = silica.getCallChain().size();
		//int count = 0;
		for(Entry<NodeInterface, CFGInterface> callSiteNodeToCfg : silica.getCallChain().entrySet())
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
		return output.toString();
	}
	
	
	private Set<String> getQueryForms(Silica silica)
	{
		if(cachedQueryForm.containsKey(silica))
			return cachedQueryForm.get(silica);
		else
		{
			Set<String> queryForms = new HashSet<>();
			for(String query : silica.getStringQueries())
			{
				query = p.matcher(query).replaceAll(" ");
				
				String[] entries = query.split(" ");
				StringBuilder cm = new StringBuilder();
				for(String item : entries)
				{
					if(keywords.contains(item))
						cm.append(item).append(" ");
				}
				String form = cm.toString().trim();
				queryForms.add(form);
			}
			cachedQueryForm.put(silica, queryForms);
			return queryForms;
		}
	}
	
	private Set<String> getTableForms(Silica silica)
	{
		if(cachedTableForm.containsKey(silica))
			return cachedTableForm.get(silica);
		else
		{
			Set<String> tableForms = new HashSet<>();
			for(String query : silica.getStringQueries())
			{
				for(String tableName : QueryAnalysis.getRelatedTableNames(query))
				{
					if(silica.getTableToForm().containsKey(tableName))
						tableForms.add(silica.getTableToForm().get(tableName));
				}
			}
			cachedTableForm.put(silica, tableForms);
			return tableForms;
		}
	}

	
	public void reportSingle(Set<Silica> silicas, String appPath, String antipattern)
	{
		Set<String> queryForms = new HashSet<>();
		Set<String> tableForms = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		sb.append(appPath + " contains " + antipattern + " num:" + silicas.size() + "\n");
		for(Silica silica : silicas)
		{
			//queryForms.addAll(getQueryForms(silica));
			//tableForms.addAll(getTableForms(silica));
			sb.append("Antipattern:" + antipattern + "\n");
			sb.append("Index:" + antipattern + "-"+appPath + "-" + index +"\n");
			sb.append("CallChain:" + printCallChain(silica) + "\n");
			sb.append("CallChain Length:" + silica.getCallChain().size() + "\n");
			sb.append("Location:" + silica.getLocation() + "\n");
			sb.append("Code point index:" + silica.getCodepoint().getNode().getName() + "\n");
			sb.append("Query:" + silica.getStringQueries() + "\n");
			sb.append("Query possible size:" + silica.getStringQueries().size() + "\n");
			//sb.append("Query Form:" + getQueryForms(silica) + "\n");
			sb.append("Table Form:" + getTableForms(silica) + "\n");
			sb.append("Is API:" + silica.isAPI() + "\n");
			sb.append("Is Hardcoded:" + silica.isHardCoded() + "\n");
			if(antipattern.equals("UnboundedQuery"))
				sb.append("Is Iterate:" + silica.isIterate() + "\n");
			if(antipattern.equals("UnnecessaryColumnRetrieval"))
				sb.append("Is From Index:" + silica.isFromIndex() + "\n");
			if(antipattern.equals("UnbatchedWrites") || antipattern.equals("NotUsingParameterizedQueries"))
				sb.append("Is Plus:" + silica.isPlus() + "\n");
			if(silica.getMetaInfo() != null)
				sb.append("Meta Info:" + silica.getMetaInfo() + "\n");
			sb.append("\n");
			
			index++;
		}
		//sb.append("Query Forms:" + queryForms + "\n");
		//sb.append("Table Forms:" + tableForms + "\n");
		pw.println(sb);
		pw.flush();
	}
	
	public void reportPair(Set<Pair<Silica, Silica>> silicaPairs, String appPath, String antipattern)
	{
		Set<String> queryForms = new HashSet<>();
		Set<String> tableForms = new HashSet<>();
		StringBuilder sb = new StringBuilder();
		sb.append(appPath + " contains " + antipattern + " num:" + silicaPairs.size() + "\n");
		for(Pair<Silica, Silica> silicaPair : silicaPairs)
		{
			//queryForms.addAll(getQueryForms(silicaPair.getFirst()));
			//queryForms.addAll(getQueryForms(silicaPair.getSecond()));
			//tableForms.addAll(getTableForms(silicaPair.getFirst()));
			//tableForms.addAll(getTableForms(silicaPair.getSecond()));
			sb.append("Antipattern:" + antipattern + "\n");
			sb.append("Index:" + antipattern + "-"+appPath + "-" + index +"\n");
			sb.append("CallChain1:" + printCallChain(silicaPair.getFirst()) + "\n" );
			sb.append("CallChain1 Length:" + silicaPair.getFirst().getCallChain().size() + "\n");
			sb.append("Location1:" + silicaPair.getFirst().getLocation()+"\n");
			sb.append("Code point index1:" + silicaPair.getFirst().getCodepoint().getNode().getName() + "\n");
			sb.append("Query1:" + silicaPair.getFirst().getStringQueries() + "\n");
			sb.append("Query1 possible size:" + silicaPair.getFirst().getStringQueries().size() + "\n");
			//sb.append("Query Form1:" + getQueryForms(silicaPair.getFirst()) + "\n");
			sb.append("Table Form1:" + getTableForms(silicaPair.getFirst()) + "\n");
			sb.append("Is API:" + silicaPair.getFirst().isAPI() + "\n");
			sb.append("Is Hardcoded:" + silicaPair.getFirst().isHardCoded() + "\n");
			sb.append("CallChain2:" + printCallChain(silicaPair.getSecond()) + "\n" );
			sb.append("CallChain2 Length:" + silicaPair.getSecond().getCallChain().size() + "\n");
			sb.append("Location2:" + silicaPair.getSecond().getLocation()+"\n");
			sb.append("Code point index2:" + silicaPair.getSecond().getCodepoint().getNode().getName() + "\n");
			sb.append("Query2:" + silicaPair.getSecond().getStringQueries() + "\n");
			sb.append("Query2 possible size:" + silicaPair.getSecond().getStringQueries().size() + "\n");
			//sb.append("Query Form2:" + getQueryForms(silicaPair.getSecond()) + "\n");
			sb.append("Table Form2:" + getTableForms(silicaPair.getSecond()) + "\n");
			sb.append("Is API:" + silicaPair.getSecond().isAPI() + "\n");
			sb.append("Is Hardcoded:" + silicaPair.getSecond().isHardCoded() + "\n");
			if(silicaPair.getFirst().getMetaInfo() != null)
				sb.append("Meta Info:" + silicaPair.getFirst().getMetaInfo() + "\n");
			sb.append("\n");
			index++;
		}
		//sb.append("Query Forms:" + queryForms + "\n");
		//sb.append("Table Forms:" + tableForms + "\n");
		pw.println(sb);
		pw.flush();
	}
	
	private void removeCycles(Silica root, Set<Silica> allNodes, Map<Silica, Set<Silica>> silicaToChildren, Map<Silica, Set<Silica>> silicaToRemovedChildren)  
    { 
          
        // Mark all the vertices as not visited and 
        // not part of recursion stack 
		Map<Silica, Boolean> visited = new HashMap<>(); 
		Map<Silica, Boolean> recStack = new HashMap<>(); 
        for(Silica s : allNodes)
        {
        	visited.put(s, false);
        	recStack.put(s,  false);
        }
        
        
        // Call the recursive helper function to 
        // detect cycle in different DFS trees 
        removeCyclicUtil(root, null, silicaToChildren, silicaToRemovedChildren, visited, recStack);
    } 
	
    private void removeCyclicUtil(Silica silica, Silica parent,
    		Map<Silica, Set<Silica>> silicaToChildren,
    		Map<Silica, Set<Silica>> silicaToRemovedChildren,
    		Map<Silica, Boolean> visited, 
    		Map<Silica, Boolean> recStack)  
	{ 
	
		// Mark the current node as visited and 
		// part of recursion stack 
		if (recStack.get(silica)) 
		{
			silicaToRemovedChildren.get(parent).remove(silica);
			return; 
		}
		
		if (visited.get(silica)) 
			return; 
		
		visited.put(silica, true); 
		
		recStack.put(silica, true);
		
		Set<Silica> children = silicaToChildren.get(silica); 
		if(children != null)
		{
			for (Silica c: children)
			{
				removeCyclicUtil(c, silica, silicaToChildren, silicaToRemovedChildren, visited, recStack); 
					
			}
		}
		recStack.put(silica, false); 
		return; 
	} 
    
	public int reportPairInSequence(Set<Pair<Silica, Silica>> silicaPairs, String appPath, String antipattern)
	{

		int sequenceNum = 0;
		
		Map<Silica, Set<Silica>> silicaToChildren = new HashMap<>();
		Set<Silica> allNodes = new HashSet<>();
		Set<Silica> rootNodes = new HashSet<>();
		Set<Silica> childNodes = new HashSet<>();

		for(Pair<Silica, Silica> silicaPair : silicaPairs)
		{
			Silica start = silicaPair.getFirst();
			Silica end = silicaPair.getSecond();
			allNodes.add(start);
			allNodes.add(end);
			rootNodes.remove(end);
			childNodes.add(end);
			if(silicaToChildren.containsKey(start))
				silicaToChildren.get(start).add(end);
			else
			{
				Set<Silica> children = new HashSet<>();
				children.add(end);
				silicaToChildren.put(start, children);
			}
			
			if(!childNodes.contains(start))
				rootNodes.add(start);
		}

		
		if(rootNodes.isEmpty())
		{
			reportPair(silicaPairs, appPath, antipattern);
		}
		else
		{
			Map<Silica, Set<Silica>> silicaToCycleRemovedChildren = new HashMap<>();
			for(Silica s : silicaToChildren.keySet())
			{
				Set<Silica> copy = new HashSet<>();
				copy.addAll(silicaToChildren.get(s));
				silicaToCycleRemovedChildren.put(s, copy);
			}
			
			for(Silica root : rootNodes)
			{
				//remove backedges
				removeCycles(root, allNodes, silicaToChildren, silicaToCycleRemovedChildren);
			}
			
			//topo sort
			
			
			// root node has null parent
			Map<Silica, Set<Silica>> silicaToParents = new HashMap<>();
			for(Silica parent : silicaToCycleRemovedChildren.keySet())
			{
				for(Silica child : silicaToCycleRemovedChildren.get(parent))
				{
					if(silicaToParents.containsKey(child))
						silicaToParents.get(child).add(parent);
					else
					{
						Set<Silica> parents = new HashSet<>();
						parents.add(parent);
						silicaToParents.put(child, parents);
					}
				}
			}
			
			
			Map<Silica, Set<Silica>> silicaToParentsCopy = new HashMap<>();
			for(Silica silica : silicaToParents.keySet())
			{
				Set<Silica> copyParents = new HashSet<>(silicaToParents.get(silica));
				silicaToParentsCopy.put(silica, copyParents);
			}
			
			//L ← Empty list that will contain the sorted elements
			List<Silica> topo = new ArrayList<>();
			//S ← Set of all nodes with no incoming edges
			Queue<Silica> S = new LinkedList<>();
			
			for(Silica root : rootNodes)
				S.add(root);
			while(!S.isEmpty())
			{
				Silica n = S.poll();
				topo.add(n);
				if(silicaToCycleRemovedChildren.containsKey(n))
				{
					for(Silica child : silicaToCycleRemovedChildren.get(n))
					{
						silicaToParentsCopy.get(child).remove(n);
						if(silicaToParentsCopy.get(child).isEmpty())
							S.add(child);
					}
				}
			}
			
			
			
			Map<Silica, Integer> nodeToLongestLength = new HashMap<>();

			//TODO: repeat for each root node
			for(Silica node : topo)
			{
				if(!silicaToParents.containsKey(node))
				{
					nodeToLongestLength.put(node, 0);
				}
				else
				{
					int max = -1;
					for(Silica parent : silicaToParents.get(node))
					{
						if(nodeToLongestLength.get(parent) > max)
							max = nodeToLongestLength.get(parent);
					}
					if(max == -1)
						System.err.println("uncomputed parent");
					
					nodeToLongestLength.put(node, max + 1);
				}
			}
			
			Map<Silica, List<List<Silica>>> leavesToAllPossilbeLongestPaths = new HashMap<>();
			
			for(Silica root : rootNodes)
			{
				Set<Silica> reachableNodesFromRoot = new HashSet<>();
				Queue<Silica> dfsQueue = new LinkedList<>();
				dfsQueue.add(root);
				
				Set<Silica> leafNodes = new HashSet<>();
				while(!dfsQueue.isEmpty())
				{
					Silica node = dfsQueue.poll();
					reachableNodesFromRoot.add(node);
					if(silicaToCycleRemovedChildren.get(node) == null || silicaToCycleRemovedChildren.get(node).isEmpty())
						leafNodes.add(node);
					else
					{
						for(Silica child : silicaToCycleRemovedChildren.get(node))
						{
							if(!reachableNodesFromRoot.contains(child))
								dfsQueue.add(child);
						}
					}
				}

				Map<Silica, List<List<Silica>>> nodeToLongestPaths = new HashMap<>();
				
				for(Silica node : topo)
				{
					if(reachableNodesFromRoot.contains(node))
					{
						
						if(!silicaToParents.containsKey(node))
						{
						
							List<Silica> path = new ArrayList<>();
							path.add(node);
							List<List<Silica>> paths = new ArrayList<>();
							paths.add(path);
							nodeToLongestPaths.put(node, paths);
						}
						else
						{
							int max = -1;
							
							for(Silica parent : silicaToParents.get(node))
							{
								if(reachableNodesFromRoot.contains(parent))
								{
									if(nodeToLongestLength.get(parent) > max)
										max = nodeToLongestLength.get(parent);
								}
							}
							
							if(max == -1)
								System.err.println("uncomputed parent");
							
							List<List<Silica>> longestPaths = new ArrayList<>();

							for(Silica parent : silicaToParents.get(node))
							{
								if(reachableNodesFromRoot.contains(parent))
								{
									if(nodeToLongestLength.get(parent) == max)
									{	
										List<List<Silica>> longestParentPaths = nodeToLongestPaths.get(parent);
										
										for(List<Silica> longestParentPath : longestParentPaths)
										{
											List<Silica> longestParentPathCopy = new ArrayList<>(longestParentPath);
											longestParentPathCopy.add(node);
											longestPaths.add(longestParentPathCopy);
										}
									}
								}
								
							}
							
							nodeToLongestPaths.put(node, longestPaths);
						}
					}
				}
				
				for(Silica leaf : leafNodes)
				{
					if(leavesToAllPossilbeLongestPaths.containsKey(leaf))
						leavesToAllPossilbeLongestPaths.get(leaf).addAll(nodeToLongestPaths.get(leaf));
					else
					{
						List<List<Silica>> paths = new ArrayList<>();
						paths.addAll(nodeToLongestPaths.get(leaf));
						leavesToAllPossilbeLongestPaths.put(leaf, paths);
					}
				}
			}
			for(List<List<Silica>> paths : leavesToAllPossilbeLongestPaths.values())
			{
				
				sequenceNum+= paths.size();
				
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(appPath + " contains " + antipattern + " pair num:" + silicaPairs.size() + " sequence num:" + sequenceNum + "\n");
			for(List<List<Silica>> paths : leavesToAllPossilbeLongestPaths.values())
			{
				for(List<Silica> path : paths)
				{
					sb.append("Antipattern:" + antipattern + "\n");
					sb.append("App:" + appPath + "\n");
					for(int i = 1; i <= path.size(); i++)
					{
						Silica silica = path.get(i-1);
						sb.append("CallChain" + i + ":" + printCallChain(silica) + "\n");
						sb.append("Location" + i + ":" + silica.getLocation() + "\n");
						sb.append("Query" + i + ":" + silica.getStringQueries() + "\n");
					}
					sb.append("\n");
				}
			}
			
		
			pw.println(sb);
			pw.flush();

		}
		return sequenceNum;
	}

	
	private void printSilicaToChildren(Map<Silica, Set<Silica>> silicaToChildren)
	{
		for(Silica s : silicaToChildren.keySet())
		{
			System.out.println(s.getStringQueries());
			System.out.println("children:");
			for(Silica c : silicaToChildren.get(s))
			{
				System.out.println(c.getStringQueries());
			}
			System.out.println();
		}
	}
	
	public void clear()
	{
		try {
			pw = new PrintWriter(outputPath);
	        pw.print("");
	        pw.close();
			pw = new PrintWriter(new FileWriter(outputPath,true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void close()
	{
		pw.close();
	}
	
	private Map<Silica, List<List<Silica>>> getLongestPathFromNodeToLeaves(Silica node,
			Map<Silica, Set<Silica>> nodeToChildren, Map<Silica, Map<Silica, List<List<Silica>>>> rootToLeafToPath)
	{
		if(rootToLeafToPath.containsKey(node))
		{
			return rootToLeafToPath.get(node);
		}
		else
		{
			Map<Silica, List<List<Silica>>> leafToPath = new HashMap<>();
			if(nodeToChildren.get(node) == null || nodeToChildren.get(node).isEmpty())
			{
				List<Silica> path = new ArrayList<>();
				path.add(node);
				List<List<Silica>> longestPaths = new ArrayList<>();
				longestPaths.add(path);
				leafToPath.put(node, longestPaths);
			}
			else
			{
				for(Silica child : nodeToChildren.get(node))
				{
					
					Map<Silica, List<List<Silica>>> childLeafToPath = 
							getLongestPathFromNodeToLeaves(child, nodeToChildren, rootToLeafToPath);
					//find the longest one
					for(Silica leaf : childLeafToPath.keySet())
					{
						List<List<Silica>> childPaths = childLeafToPath.get(leaf);
						//copy the child paths
						List<List<Silica>> paths = new ArrayList<>();
						for(List<Silica> childPath : childPaths)
						{
							List<Silica> path = new ArrayList<>(childPath);
							paths.add(path);
						}
						
						int length = paths.iterator().next().size();
						if(!leafToPath.containsKey(leaf))
						{
							leafToPath.put(leaf, paths);
						}
						else
						{
							List<List<Silica>> existingLongestPaths = leafToPath.get(leaf);
									
							int existingLength = existingLongestPaths.iterator().next().size();
							
							if(existingLength < length)
							{
								leafToPath.put(leaf, paths);
							}
							else if(existingLength == length)
							{
								leafToPath.get(leaf).addAll(paths);
							}
						}				
					}
	
	
				}
	
				for(Silica leaf : leafToPath.keySet())
				{
					//add the current node to the path
					for(List<Silica> path : leafToPath.get(leaf))
					{
						path.add(0, node);
					}
				}
			}
			/*
			for(Silica s : leafToPath.keySet())
			{
				System.out.println(s.getStringQueries());
				for(List<Silica> l : leafToPath.get(s))
				{
					System.out.println(l);
				}
				System.out.println();
			}
			*/
			rootToLeafToPath.put(node, leafToPath);
			return leafToPath;
		}
	}
}
