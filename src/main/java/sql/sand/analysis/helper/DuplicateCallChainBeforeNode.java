package sql.sand.analysis.helper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class DuplicateCallChainBeforeNode {

	public static Map<NodeInterface, CFGInterface> duplicate(Map<NodeInterface, CFGInterface> originCallChain, NodeInterface node)
	{
		Map<NodeInterface, CFGInterface> callChain = new LinkedHashMap<>();
		for(NodeInterface callSiteNode : originCallChain.keySet())
		{
			if(callSiteNode.equals(node))
				break;
			else
			{
				callChain.put(callSiteNode, originCallChain.get(callSiteNode));
			}
		}
		return callChain;
	}
}
