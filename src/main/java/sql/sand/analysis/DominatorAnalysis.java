package sql.sand.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import sql.sand.abstraction.Codepoint;
import sql.sand.analysis.helper.Dominator;
import sql.sand.analysis.helper.PostDominator;
import sql.sand.function.Analyzer;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class DominatorAnalysis {
	
	private Codepoint codepoint;
	private Map<NodeInterface, Codepoint> nodeToTargetCodepoints = new HashMap<>();
	private Set<Codepoint> dominateCodepoints = new HashSet<>();
	private Map<NodeInterface, Set<NodeInterface>> postDominatorTable;
	private Map<NodeInterface, Set<NodeInterface>> dominatorTable;
	private Map<NodeInterface, Set<Codepoint>> methodToContainingCodepoints = new HashMap<>();
	
	public DominatorAnalysis(Codepoint codepoint, Set<Codepoint> targetCodepoints, Map<CFGInterface, Dominator> cachedDominator)
	{
		this.codepoint = codepoint;
		if(targetCodepoints != null)
		{
			for(Codepoint targetCodepoint : targetCodepoints)
			{
				nodeToTargetCodepoints.put(targetCodepoint.getNode(), targetCodepoint);
			}
		}
		
		for(Codepoint targetCodepoint : targetCodepoints)
		{
			for(NodeInterface callSiteNode : targetCodepoint.getCallChain().keySet())
			{
				//exclude the dummy entry node
				if(((Node<Unit>) callSiteNode).getActualNode() != null)
				{
					if(methodToContainingCodepoints.containsKey(callSiteNode))
					{
						methodToContainingCodepoints.get(callSiteNode).add(targetCodepoint);
					}
					else
					{
						Set<Codepoint> reachableCodepoint = new HashSet<>();
						reachableCodepoint.add(targetCodepoint);
						methodToContainingCodepoints.put(callSiteNode, reachableCodepoint);
					}
				}
			}
		}
		computeInterDominators(cachedDominator);

	}
	
	/**
	 * return a set of codepoints that the input codepoint dominates
	 * Note: although the input codepoint dominates itself in theory, 
	 * we do not add it to the dominator set
	 */
	public Set<Codepoint> getDominateCodepoints()
	{
		return dominateCodepoints;
	}

	public boolean isPostDominate(NodeInterface source, NodeInterface dest)
	{
		return postDominatorTable.get(source).contains(dest);
	}
	
	public boolean isDominate(NodeInterface source, NodeInterface dest)
	{
		return dominatorTable.get(source).contains(dest);
	}
	
	private void computeDominators(CFGInterface cfg) {
		
		Dominator dom = new Dominator(cfg.getAllNodes(), cfg.getAllEdges(), cfg.getEntryNode());
		
		dominatorTable = dom.getDominatorTable();
		postDominatorTable = new PostDominator(cfg.getAllNodes(), cfg.getAllEdges(), cfg.getExitNode()).getPostDominatorTable();
		
		for(NodeInterface n : cfg.getAllNodes())
		{
			if(n.equals(codepoint.getNode()))
			{
				for(NodeInterface domNode : dom.getDominatorTable().get(n))
				{
					//we do not add the node itself 
					if(nodeToTargetCodepoints.containsKey(domNode) && !domNode.equals(codepoint.getNode()))
					{
						dominateCodepoints.add(nodeToTargetCodepoints.get(domNode));
					}
				}
			}
		}
	}
	
	
	private void computeInterDominators(Map<CFGInterface, Dominator> cachedDominator)
	{
		CFGInterface last = null;
		for(CFGInterface cfg : codepoint.getCallChain().values())
		{
			Dominator dom;
			if(cachedDominator.containsKey(cfg))
			{
				dom = cachedDominator.get(cfg);
			}
			else
			{
				dom = new Dominator(cfg.getAllNodes(), cfg.getAllEdges(), cfg.getEntryNode());
				if(Analyzer.isCached)
					cachedDominator.put(cfg, dom);
			}
			last = cfg;
		}
		
		//TODO: compute the table for all the cfgs in the call chain
		dominatorTable = cachedDominator.get(last).getDominatorTable();
		postDominatorTable = new PostDominator(last.getAllNodes(), last.getAllEdges(), last.getExitNode()).getPostDominatorTable();
		
			
		for(CFGInterface cfg : codepoint.getCallChain().values())
		{
			for(NodeInterface n : cfg.getAllNodes())
			{
				//get the node that the codepoint node dominates, or its call site nodes can dominates (the call sites nodes should dominate the exit)	
				if(n.equals(codepoint.getNode()) || codepoint.getCallChain().keySet().contains(n))
				{
					if(codepoint.getCallChain().keySet().contains(n))
					{
						
						boolean allDominatesTheExit = true;
						CFGInterface childCFG = null;
						for(NodeInterface current : codepoint.getCallChain().keySet())
						{
							//System.out.println(childCFG + " " + n.getNodeContent() + " " + current.getNodeContent() + " here");
							if(childCFG != null)
							{
								boolean dominateExit = false;
								for(NodeInterface node : cachedDominator.get(childCFG).getDominatorTable().get(current))
								{
									if(node.equals(childCFG.getExitNode()))
									{
										dominateExit = true;
										break;
									}
								}
								if(!dominateExit)
									allDominatesTheExit = false;
								childCFG = codepoint.getCallChain().get(current);
							}
							if(current.equals(n))
								childCFG = codepoint.getCallChain().get(current);
						}

						boolean dominateExit = false;
						for(NodeInterface node : cachedDominator.get(childCFG).getDominatorTable().get(codepoint.getNode()))
						{
							if(node.equals(childCFG.getExitNode()))
							{
								dominateExit = true;
								break;
							}
	
						}
						if(!dominateExit)
							allDominatesTheExit = false;
						if(!allDominatesTheExit)
							continue;
					}
					
					for(NodeInterface dominateNode : cachedDominator.get(cfg).getDominatorTable().get(n))
					{
						//if dominates a target codepoint
						if(nodeToTargetCodepoints.containsKey(dominateNode) && !dominateNode.equals(codepoint.getNode()))
						{
							dominateCodepoints.add(nodeToTargetCodepoints.get(dominateNode));
						}
						//if dominates a call site of a target codepoint
						else if(methodToContainingCodepoints.containsKey(dominateNode) && !codepoint.getCallChain().containsKey(dominateNode))
						{
							dominateCodepoints.addAll(methodToContainingCodepoints.get(dominateNode));
						}
					}
				}
					
			}
		}
	}
}
