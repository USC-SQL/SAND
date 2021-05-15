package sql.sand.analysis.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import edu.usc.sql.graphs.Edge;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Graph;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

//This class makes use of functions from PostDominator.java and builds the control dependancy table in a Map. The algorithm is built
//As per the procedure given in "Program Dependence Graph and its Use in Optimization"
public class ControlDependenceGraph {
	
	//The Map component which has a record of the node and its CDG
	//private Graph CDGraph = new Graph();
	//private Set<EdgeInterface> SPairs = new HashSet<EdgeInterface>();
	private Map<NodeInterface, Set<EdgeInterface>> nodeToControlDependents = new HashMap<>();
	private Map<NodeInterface, List<EdgeInterface>> nodeToNestedControlDependents = new HashMap<>();
	
	public ControlDependenceGraph(CFGInterface cfg)
	{
		//Creating the Post Dominator for the nodes and edges
		PostDominator PD = new PostDominator(cfg.getAllNodes(), cfg.getAllEdges(), cfg.getExitNode());
		
		Set<EdgeInterface> SPairs = new HashSet<EdgeInterface>();
		//Traversing through the edges of the CFG to figure out S
		//S= all node pairs which are not Post Dominant on each other
		 //TODO: Change Map to Set - SPairs

		for (EdgeInterface edgeInterface : cfg.getAllEdges()) {
			if(!PD.isPostDominate(edgeInterface.getDestination(),edgeInterface.getSource()))
					{
						//If there is no Post Dominance i.e. B does not post Dominate A. Store it as SPair(Algo)
						
						//System.out.println("Added:"+edgeInterface);
				
						SPairs.add(edgeInterface);

					}
		}
		
		
		//Now that we have SPairs, let us calculate the LCM. The LCM is provided in the PostDominator class
		//getLeastCommonPostDominator()
		
		//Calculating the control Dependant of A out of the SPairs as per the Algo.
		for (EdgeInterface SEdgeItr : SPairs)
		{
		   // System.out.println("\n S Pair:="+ SEdgeItr.getSource().toString() + " " + SEdgeItr.getSource().getNodeContent() + " -> " +  SEdgeItr.getDestination().toString() + " " + SEdgeItr.getDestination().getNodeContent());

		    //Retrieving the LCM
		    NodeInterface LCM = PD.getLeastCommonPostDominator(SEdgeItr.getSource(), SEdgeItr.getDestination());
		    
		    //System.out.println("LCM:"+LCM.getNodeContent());
		    
		    //Path from one node to the other node is obtained by the Post Dominance function: getPathBetweenTwoNodes()
		 
		    //Check if LCM = A or LCM is parent of A in the third parameter
		    Set<NodeInterface> PathBet=PD.getPathBetweenTwoNodes(SEdgeItr.getDestination(), LCM, LCM.equals(SEdgeItr.getSource()));

		    //System.out.println("Path between:" + PathBet);
		    
		    NodeInterface A = SEdgeItr.getSource();
		    //Traversing through the paths from LCM to B and adding all nodes to Control Dependant on A
		    for(NodeInterface NodeItr: PathBet)
	    	{
		    	if(A.equals(NodeItr))
		    	{
		    		continue;
		    	}
		    	//System.out.println("adding " + A.toString());
	    		if(nodeToControlDependents.containsKey(NodeItr))
	    		{
	    			//System.out.println("!!!!!!!!");
	    			nodeToControlDependents.get(NodeItr).add(SEdgeItr);
	    		}
	    		else
	    		{
	    			Set<EdgeInterface> controlDependents = new HashSet<>();
	    			controlDependents.add(SEdgeItr);
	    			nodeToControlDependents.put(NodeItr, controlDependents);
	    		}
		    	
	    	}

		}
		

		
		for(NodeInterface root : nodeToControlDependents.keySet())
		{
			Queue<NodeInterface> queue = new LinkedList<>();
			queue.add(root);
			List<EdgeInterface> controlDependents = new ArrayList<>();
			while(!queue.isEmpty())
			{
	
				NodeInterface current = queue.poll();
				//System.out.println("current:" + current.hashCode() + " " + current.getNodeContent());
				if(nodeToControlDependents.get(current) != null)
				{
					for(EdgeInterface cd : nodeToControlDependents.get(current))
					{
						//System.out.println("control" + cd.hashCode() + " "+ cd.getNodeContent());
						if(!controlDependents.contains(cd))
						{
							controlDependents.add(cd);
							queue.add(cd.getSource());
						}
					}
				}
			}
			nodeToNestedControlDependents.put(root, controlDependents);
		}

	}
		
	
	//get the list of nodes that the input node depends on
	public List<EdgeInterface> getControlDependents(NodeInterface root)
	{
		return nodeToNestedControlDependents.get(root);
	}
}
