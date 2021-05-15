package sql.sand.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.traversal.DFS;
import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Location;
import sql.sand.abstraction.Silica;
import sql.sand.abstraction.Transaction;
import sql.sand.analysis.helper.Pair;
import sql.sand.function.Analyzer;

public class TransactionAnalysis {

	private Codepoint codepoint;
	private Set<Codepoint> transactionSet = new HashSet<>();
	private Set<Codepoint> beginTransactionSet = new HashSet<>();
	private Set<String> relevantMethods = new HashSet<>();
	//private Map<NodeInterface, String> transactionNodes = new HashMap<>();
	private Map<Key, Set<Pair<NodeInterface, Integer>>> cachedTransactionAnalysisResult = new HashMap<>();
	
	public TransactionAnalysis(Codepoint codepoint) {
		this.codepoint = codepoint;
		CFGInterface callGraphEntry = codepoint.getCallChain().entrySet().iterator().next().getValue();
		
		if(codepoint.getRelevantCallersAndCallees() == null)
		{
			for(Entry<NodeInterface, CFGInterface> nodeToCFG : codepoint.getCallChain().entrySet())
			{
				relevantMethods.add(nodeToCFG.getValue().getSignature());
			}
		}
		else
		{
			for(Entry<NodeInterface, CFGInterface> nodeToCFG : codepoint.getRelevantCallersAndCallees().entrySet())
			{
				relevantMethods.add(nodeToCFG.getValue().getSignature());
			}
		}
		
		
		Map<NodeInterface, CFGInterface> callChain = new LinkedHashMap<>();
		callChain.put(codepoint.getCallChain().entrySet().iterator().next().getKey(),
				codepoint.getCallChain().entrySet().iterator().next().getValue());
		
		GlobalFlowAnalysis(callGraphEntry, null, callChain);
	}
	
	public Set<Codepoint> getTransactionSet()
	{
		return transactionSet;
	}
	
	public Set<Codepoint> getBeginTransactionSet()
	{
		return beginTransactionSet;
	}
	private Set<Pair<NodeInterface, Integer>> GlobalFlowAnalysis(CFGInterface cfg, Set<Pair<NodeInterface,Integer>>input, Map<NodeInterface, CFGInterface> callChain)
	{
	
		Map<NodeInterface,Set<Pair<NodeInterface,Integer>>> dSet = new HashMap<>();
		for(NodeInterface n: cfg.getAllNodes())
		{
			if(n.equals(codepoint.getNode()))
			{
				Pair<NodeInterface,Integer> init = new Pair<>(n,0);
				HashSet<Pair<NodeInterface,Integer>> h = new HashSet<>();
				h.add(init);
				dSet.put(n, h);
			}
			else if(n.equals(cfg.getExitNode()))
			{
				if(input!=null)
					dSet.put(n,input);
				else
					dSet.put(n, new HashSet<Pair<NodeInterface,Integer>>());
			}
			else
			{
				dSet.put(n, new HashSet<Pair<NodeInterface,Integer>>());
			}
		}
		
		
		List<NodeInterface> reverseDFS = DFS.reversedfs(cfg.getAllNodes(),cfg.getExitNode());
		boolean change = true;

		while (change) {
			change = false;
			for(NodeInterface n : reverseDFS)
			{
				Unit actualNode = (Unit) ((Node)n).getActualNode();
				
				Set<Pair<NodeInterface,Integer>> out = null;
				
				
				//Information may be flow out from callee
				if(dSet.get(n).isEmpty())
				{
					if(actualNode!=null&&((Stmt)actualNode).containsInvokeExpr())
					{
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
						
						if(relevantMethods.contains(sig))
						{
							if(codepoint.getRelevantCallersAndCallees().get(n)!=null)
							{
								Key key = new Key(codepoint.getRelevantCallersAndCallees().get(n), dSet.get(n));
								if(cachedTransactionAnalysisResult.containsKey(key))
								{
									out = cachedTransactionAnalysisResult.get(key);
								}
								else
								{
								
									Map<NodeInterface, CFGInterface> copyCallChain = new LinkedHashMap<>();
									for(Entry<NodeInterface, CFGInterface> entry : callChain.entrySet())
										copyCallChain.put(entry.getKey(), entry.getValue());
									copyCallChain.put(n, codepoint.getRelevantCallersAndCallees().get(n));
								
									out = GlobalFlowAnalysis(codepoint.getRelevantCallersAndCallees().get(n), dSet.get(n), copyCallChain);
									cachedTransactionAnalysisResult.put(key, out);
								}
								
								for(Pair<NodeInterface,Integer> outNode: out)
								{
									if(input!=null&&isInInput(outNode.getFirst(), input))
										continue;
									if(dSet.get(n).contains(outNode))
										continue;
								}
							
							}
					
						}
					}
				}
				else
				{
					//exit node
					if(actualNode==null)
						out = dSet.get(n);
					else if(((Stmt)actualNode).containsInvokeExpr())
					{
						String sig= ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
						if(sig.contains("<android.database.sqlite.SQLiteDatabase: void beginTransaction"))
						{
							beginTransactionSet.add(new Codepoint(n, callChain));
							//store the target node that is under the effect of a beginTransaction
							for(Pair<NodeInterface,Integer> p: dSet.get(n))
							{
								if(p.getSecond()==0)
								{
									//transactionNodes.put(n, cfg.getSignature());
									transactionSet.add(new Codepoint(n, callChain));
								}
							}
							out = operationOnD(dSet.get(n),-1);
						}
						else if(sig.contains("<android.database.sqlite.SQLiteDatabase: void endTransaction()>"))
							out =  operationOnD(dSet.get(n),1);
						else if(relevantMethods.contains(sig))
						{
							if(codepoint.getRelevantCallersAndCallees().get(n)!=null)
							{
								Key key = new Key(codepoint.getRelevantCallersAndCallees().get(n), dSet.get(n));
								if(cachedTransactionAnalysisResult.containsKey(key))
								{
									out = cachedTransactionAnalysisResult.get(key);
								}
								else
								{
									
									Map<NodeInterface, CFGInterface> copyCallChain = new LinkedHashMap<>();
									for(Entry<NodeInterface, CFGInterface> entry : callChain.entrySet())
										copyCallChain.put(entry.getKey(), entry.getValue());
									copyCallChain.put(n, codepoint.getRelevantCallersAndCallees().get(n));
									
									out = GlobalFlowAnalysis(codepoint.getRelevantCallersAndCallees().get(n), dSet.get(n), copyCallChain);
									cachedTransactionAnalysisResult.put(key, out);
								}
								
								for(Pair<NodeInterface,Integer> outNode: out)
								{
									if(input!=null&&isInInput(outNode.getFirst(), input))
										continue;
									if(dSet.get(n).contains(outNode))
										continue;
								}
							}
							//strongly connected component
							else
							{
								//1. do nothing: Generate false negative
								
								//2. keep propagating dSet.get(n)  : Generate false positive or false negative if there exist begin or end transaction in the by-pass method
								out = dSet.get(n);
							}
						}
						else
							out = dSet.get(n);
					}
					else
						out = dSet.get(n);
					
				}

				if(out!=null&&!out.isEmpty())
				{
					for(EdgeInterface e: n.getInEdges())
					{
						NodeInterface prep = e.getSource();
						
						//Union
						if(!dSet.get(prep).containsAll(out))
						{
							change = true;
							dSet.get(prep).addAll(out);
						}
						
					}
				}
				

			}
			
			
		}
		return dSet.get(cfg.getEntryNode());
	}
	/**
	 * operationOnD(d,i): for all (s,j) in the (Statement,Int) set d,
	 * if j+i >= 0, add (s,j+i) to the return set.
	 * 
	 * A extended step to count nested transaction, if j == 0 and i == -1, add (s, j) to the return set
	 * 
	 */
	// 
	// 
	private static final int UPPER_BOUND = 10;
	private Set<Pair<NodeInterface,Integer>> operationOnD(Set<Pair<NodeInterface,Integer>> input,int i)
	{
		Set<Pair<NodeInterface,Integer>> output = new HashSet<>();
		
		for(Pair<NodeInterface,Integer> p: input)
		{
			if(p.getSecond()+i >= 0 && p.getSecond()+i <= UPPER_BOUND)
			{
				output.add(new Pair<NodeInterface,Integer>(p.getFirst(),p.getSecond()+i));
			}
			
			//a tuple reaches a beginTransaction with counter value 0, add it to the out set
			//This step will make some tuples that are not RATs being able reach the entry;
			//It is designed on purpose to keep track of the transactions.
			if(p.getSecond() == 0 && i == -1)
			{
				output.add(p);
			}
		}
		return output;
	}
	
	private boolean isInInput(NodeInterface n, Set<Pair<NodeInterface,Integer>>input)
	{
		for(Pair<NodeInterface,Integer> in : input)
		{
			if(n.getName().equals(in.getFirst().getName()))
				return true;
		}
		return false;
	}


	
	class Key{
		private CFGInterface cfg;
		private Set<Pair<NodeInterface,Integer>> input;
		public Key(CFGInterface cfg, Set<Pair<NodeInterface,Integer>> input)
		{
			this.cfg = cfg;
			this.input = input;
		}
		
		@Override
		public int hashCode() {
			return cfg.hashCode() * 31 + input.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			Key other = (Key) obj;
			return other.cfg.equals(this.cfg) && other.input.equals(this.input);
		} 
	}
}

