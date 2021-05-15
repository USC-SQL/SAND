package sql.sand.analysis.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;

public class TopoSort {
	public static List<NodeInterface> topoSort(Set<NodeInterface> allNode, List<EdgeInterface> backedge)
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
}


