package sql.sand.analysis.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.sand.analysis.helper.LayerRegion;
import sql.sand.analysis.helper.RegionNode;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class RegionAnalysis {
	
	private Map<RegionNode, Boolean> markedforRTree = new HashMap<>();

	private Map<EdgeInterface,RegionNode> backedges = new HashMap<>();
	private CFGInterface cfg;
	private Set<NodeInterface> targetNodes;
	private Set<String> rSum;
	private String par;
	private RegionNode parNode;
	private Map<NodeInterface,String> modToRegion;
	private Map<NodeInterface, RegionNode> modToRegionNode;
	private Map<String,SootMethod> targetMethod;
	private Map<NodeInterface,CFGInterface> preStoreCFG;
	private Map<NodeInterface,String> modToSig;
	
	public RegionAnalysis(CFGInterface cfg,Set<NodeInterface> targetNodes,Set<String> rSum,String par,RegionNode parNode,Map<NodeInterface,String> modToRegion,Map<NodeInterface, RegionNode> modToRegionNode, Map<String,SootMethod> targetMethod,Map<NodeInterface,CFGInterface> preStoreCFG,Map<NodeInterface,String> modToSig)
	{
		this.cfg = cfg;
		this.targetNodes = targetNodes;
		this.rSum = rSum;
		this.par = par;
		this.parNode = parNode;
		this.modToRegion = modToRegion;
		this.modToRegionNode = modToRegionNode;
		this.targetMethod =targetMethod;
		this.preStoreCFG = preStoreCFG;
		this.modToSig = modToSig;

		
		LayerRegion lr = new LayerRegion(cfg);
		
		RegionNode root = lr.getRoot();

		//may move this before LayerRegion lr = new LayerRegion(cfg);
		removeExceptionBlock(cfg);
		//System.out.println(cfg.toDot());
		
		for (RegionNode rn : lr.getAllRegionNode()) {		
			markedforRTree.put(rn, false);
			backedges.put(rn.getBackEdge(),rn);
		}
		if(rSum.contains(cfg.getSignature()))
		{
			dfsRegionTree(root);
			reconstructCFG(cfg);
		}
		else
		{
			reconstructCFG(cfg);
		}
		
	}
	
	private void dfsRegionTree(RegionNode root)
	{
		markedforRTree.put(root, true);
		for (RegionNode child : root.getChildren()) {
			if (!markedforRTree.get(child))
			{				
				dfsRegionTree(child);
			}					
	
		}	
		markTargetRegionNode(root);
	}
	
	
	private void markTargetRegionNode(RegionNode rn)
	{
		Set<NodeInterface> remainNodes = new HashSet<>();
		remainNodes.addAll(rn.getNodeList());
		for(RegionNode chil:rn.getChildren())
		{
			remainNodes.removeAll(chil.getNodeList());
		}
		
		if(rn.getBackEdge()!=null)
		{
			Unit loopEntry = (Unit) ((Node)rn.getBackEdge().getDestination()).getActualNode();
			int loopEntryOffset = -1;
			for(Tag t : loopEntry.getTags())
			{
				if(t instanceof BytecodeOffsetTag)
				{
					loopEntryOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
				}
			}
			for(NodeInterface n:remainNodes)
			{
				Unit actualNode = (Unit) ((Node)n).getActualNode();
				if(actualNode!=null)
				{
					
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
						
						
						modToRegionNode.put(n, rn);
						if(targetNodes.contains(actualNode))
						{
														
							if(modToRegion.get(n)!=null/*||modToSig.get(n)!=null*/)
								System.err.println("Region Analysis: Node Duplication Appeared");
							
							
							//modToRegion.put(n, cfg.getSignature()+"RegionNum:"+rn.getRegionNumber()+",RegionEntry:"+rn.getBackEdge().getDestination().getOffset());
							modToRegion.put(n,cfg.getSignature()+loopEntry.getJavaSourceStartLineNumber()+","+loopEntryOffset);
							
							
							//modToSig.put(n, cfg.getSignature()+actualNode.getJavaSourceStartLineNumber());
						}
						else if(targetMethod.keySet().contains(sig))
						{
							
							CFGInterface newcfg= preStoreCFG.get(n);
							//new RegionAnalysis(newcfg, targetAPI, rSum, cfg.getSignature()+"RegionNum:"+rn.getRegionNumber()+",RegionEntry:"+rn.getBackEdge().getDestination().getOffset(), modToRegion, targetMethod, preStoreCFG,modToSig);
							new RegionAnalysis(newcfg, targetNodes, rSum, cfg.getSignature()+loopEntry.getJavaSourceStartLineNumber()+","+loopEntryOffset, rn, modToRegion, modToRegionNode, targetMethod, preStoreCFG,modToSig);
						}
	
					}
				}
			}
		}
		else
		{
			for(NodeInterface n:remainNodes)
			{
				Unit actualNode = (Unit) ((Node)n).getActualNode();
				if(actualNode!=null)
				{
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						modToRegionNode.put(n, parNode);
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();

						if(targetNodes.contains(actualNode))
						{						
							if(par != null)
							{
								modToRegion.put(n, par);
								//modToSig.put(n, cfg.getSignature()+actualNode.getJavaSourceStartLineNumber());
							}
						}
						else if(targetMethod.keySet().contains(sig))
						{
							CFGInterface newcfg= preStoreCFG.get(n);
							new RegionAnalysis(newcfg, targetNodes, rSum, par, parNode, modToRegion, modToRegionNode, targetMethod, preStoreCFG,modToSig);

						}
					}
				}
			}

		}
	}
	private void removeExceptionBlock(CFGInterface cfg)
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
	private void reconstructCFG(CFGInterface cfg)
	{
		//redirect the back edges 
		for(Entry<EdgeInterface,RegionNode> en: backedges.entrySet())
		{
			NodeInterface loopEntry = en.getKey().getDestination();
			Set<EdgeInterface> edgeInsideLoop = new HashSet<>();
			Set<EdgeInterface> edgeOutsideLoop = new HashSet<>();
			for(EdgeInterface e: loopEntry.getOutEdges())
			{	
				if(en.getValue().getNodeList().contains(e.getDestination()))
					edgeInsideLoop.add(e);
				else
					edgeOutsideLoop.add(e);
			}
			loopEntry.getOutEdges().retainAll(edgeInsideLoop);
			loopEntry.getInEdges().remove(en.getKey());
			
			en.getKey().getSource().getOutEdges().remove(en.getKey());
			en.getKey().getSource().getOutEdges().addAll(edgeOutsideLoop);
			for(EdgeInterface e:edgeOutsideLoop)
			{
				e.setSource(en.getKey().getSource());
			}				
		}
		
	}
	
	
	private void dfs(NodeInterface node,Map<NodeInterface, Boolean> marked, Set<NodeInterface> dfsNode) {
		marked.put(node, true);
		dfsNode.add(node);

		for(EdgeInterface e:node.getOutEdges())
		{
				if(!marked.get(e.getDestination()))
					dfs(e.getDestination(),marked,dfsNode);
		}
	}
	
	
}

