package sql.sand.analysis.helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.traversal.DFS;

public class Reachability {

	private Set<NodeInterface> allNode;
	private Set<EdgeInterface> allEdge;
	private List<RNode> allRNode = new ArrayList();
	//a node mapping to the nodes it can reach
	private Map<NodeInterface, Set<NodeInterface>> rTable = new HashMap<>();
	public Reachability(Set<NodeInterface> allNode, Set<EdgeInterface> allEdge, NodeInterface entry) {
		this.allNode = allNode;
		this.allEdge = allEdge;
		List<NodeInterface> dfsNodes = DFS.dfs(allNode, entry);
		if(dfsNodes.size()!=allNode.size())
			System.err.println("Bug in DFS");
		// this.allNode=allNode;
		for (NodeInterface n : dfsNodes) {
			allRNode.add(new RNode(n));
			
			Set<NodeInterface> temp = new HashSet<>();
			rTable.put(n,temp);
		}
		computeReachability();
	}

	private void initialize() {
		for (RNode rn : allRNode) {
			rn.getGenSet().add(rn.getNode());
			rn.getOutSet().add(rn.getNode());
		}
	}

	private boolean compareTwoSet(Set<NodeInterface> oldset,
			Set<NodeInterface> newset) 
	{
		if (oldset.containsAll(newset) && newset.containsAll(oldset))
			return true;
		else
			return false;
	}

	private void computeReachability() {
		initialize();
		Map<RNode, List<RNode>> preList = new HashMap<>();
		// Create predecessor list for each node
		for (RNode node : allRNode) {
			List<RNode> nodePre = new ArrayList<>();
			for (EdgeInterface e : node.getNode().getInEdges()) {
				for (RNode n : allRNode)
					if (n.getNode().equals(e.getSource()))
						nodePre.add(n);

			}

			preList.put(node, nodePre);

		}
		Map<Integer, Set<NodeInterface>> old = new HashMap<>();
		for (RNode rn : allRNode) {
			Set<NodeInterface> s = new HashSet<>(rn.getOutSet());
			old.put(rn.getNode().getOffset(), s);
			
		}
		boolean needtoloop = true;

		while (needtoloop) {
			needtoloop = false;
			for (RNode rn : allRNode) {
				// Union the out set of all the nodes in the predecessor
				// list，add to the in set of node rn
				for (RNode node : preList.get(rn)) {
					for (NodeInterface outnode : node.getOutSet())
						if (!rn.getInSet().contains(outnode))
							rn.getInSet().add(outnode);
				}
				// Union the gen set and in set to the out set of node rn
				for (NodeInterface gennode : rn.getGenSet())
					if (!rn.getOutSet().contains(gennode))
						rn.getOutSet().add(gennode);
				for (NodeInterface innode : rn.getInSet())
					if (!rn.getOutSet().contains(innode))
						rn.getOutSet().add(innode);
				if (!compareTwoSet(old.get(rn.getNode().getOffset()),
						rn.getOutSet())) {
					needtoloop = true;
					Set<NodeInterface> s = new HashSet<>(rn.getOutSet());
					old.put(rn.getNode().getOffset(), s);

				}
			}
		}

		
		for(RNode rn: allRNode)
		{
			for(NodeInterface n: rn.getInSet())
			{
				rTable.get(n).add(rn.getNode());
			}
		}
	}

	/**
	 * Return a map mapping a node to the nodes it can reach.
	 */
	public Map<NodeInterface,Set<NodeInterface>> getReachableTable()
	{
		return rTable;
	}
	
}

class RNode {
	private NodeInterface node;
	private Set<NodeInterface> inSet = new HashSet<>();
	private Set<NodeInterface> outSet = new HashSet<>();
	private Set<NodeInterface> genSet = new HashSet<>();

	public RNode(NodeInterface node) {
		this.node = node;
	}

	public NodeInterface getNode() {
		return node;
	}

	public void setNode(NodeInterface node) {
		this.node = node;
	}

	public Set<NodeInterface> getInSet() {
		return inSet;
	}

	public void setInSet(Set<NodeInterface> inSet) {
		this.inSet = inSet;
	}

	public Set<NodeInterface> getOutSet() {
		return outSet;
	}

	public void setOutSet(Set<NodeInterface> outSet) {
		this.outSet = outSet;
	}

	public Set<NodeInterface> getGenSet() {
		return genSet;
	}

	public void setGenSet(Set<NodeInterface> genSet) {
		this.genSet = genSet;
	}

}