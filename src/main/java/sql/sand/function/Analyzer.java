package sql.sand.function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.cfg.CFGInterface;
import sql.sand.abstraction.*;
import sql.sand.analysis.ControlDependentAnalysis;
import sql.sand.analysis.DefAnalysis;
import sql.sand.analysis.DominatorAnalysis;
import sql.sand.analysis.LoopAnalysis;
import sql.sand.analysis.QueryAnalysis;
import sql.sand.analysis.ReachabilityAnalysis;
import sql.sand.analysis.TransactionAnalysis;
import sql.sand.analysis.UseAnalysis;
import sql.sand.analysis.helper.ControlDependenceGraph;
import sql.sand.analysis.helper.Dominator;
import sql.sand.analysis.helper.LayerRegion;
import sql.sand.analysis.helper.Pair;
import sql.sand.analysis.helper.Reachability;

public class Analyzer {

	public static boolean isCached = true;
	private static Map<Codepoint, LoopAnalysis> cachedLoopAnalysis = new HashMap<>();
	private static Map<Codepoint, ControlDependentAnalysis> cachedControlDenpendentAnalysis = new HashMap<>();
	private static Map<Codepoint, TransactionAnalysis> cachedTransactionAnalysis = new HashMap<>();
	private static Map<Silica, DefAnalysis> cachedDefAnalysis = new HashMap<>();
	private static Map<Silica, DominatorAnalysis> cachedDominatorAnalysis = new HashMap<>();
	private static Map<Silica, ReachabilityAnalysis> cachedReachabilityAnalysis = new HashMap<>();
	private static Map<Silica, UseAnalysis> cachedUseAnalysis = new HashMap<>();
	private static Map<Silica, Set<String>> cachedQuerySet = new HashMap<>();
	//TODO: cache all the other analyses
	private static Map<CFGInterface, Reachability> cachedReachability = new HashMap<>();
	private static Map<CFGInterface, Dominator> cachedDominator = new HashMap<>();
	private static Map<CFGInterface, LayerRegion> cachedRegionTree = new HashMap<>();
	private static Map<CFGInterface, ControlDependenceGraph> cachedControlDependenceGraph = new HashMap<>();
	
	public static Set<String> getQuerySet(Silica silica)
	{
		return silica.getStringQueries();
		/*
		if(cachedQuerySet.containsKey(silica))
			return cachedQuerySet.get(silica);
		else
		{
			Set<String> querySet = QueryAnalysis.convertToQueryString(silica.getStringQueries());
			cachedQuerySet.put(silica, querySet);
			return querySet;
		}
		*/
	}
	public static Set<Pair<Codepoint,Integer>> getControlDependentSet(Codepoint codepoint)
	{
		ControlDependentAnalysis cd;
		if(cachedControlDenpendentAnalysis.containsKey(codepoint))
			cd = cachedControlDenpendentAnalysis.get(codepoint);
		else
		{
			cd = new ControlDependentAnalysis(codepoint, cachedControlDependenceGraph);
			if(isCached)
				cachedControlDenpendentAnalysis.put(codepoint, cd);
		}
		return cd.getControlDependentSet();
	}
	
	public static Set<Codepoint> getLoopSet(Codepoint codepoint)
	{
		LoopAnalysis la;
		if(cachedLoopAnalysis.containsKey(codepoint))
			la = cachedLoopAnalysis.get(codepoint);
		else
		{
			la = new LoopAnalysis(codepoint, cachedRegionTree);
			if(isCached)
				cachedLoopAnalysis.put(codepoint, la);
		}
		return la.getLoopSet();
	}
	
	public static Set<Codepoint> getTransactionSet(Codepoint codepoint)
	{
		TransactionAnalysis ta;
		if(cachedTransactionAnalysis.containsKey(codepoint))
			ta = cachedTransactionAnalysis.get(codepoint);
		else
		{
			ta = new TransactionAnalysis(codepoint);
			if(isCached)
				cachedTransactionAnalysis.put(codepoint, ta);
		}
		return ta.getTransactionSet();
	}
	public static Set<Codepoint> getBeginTransactionSet(Codepoint codepoint)
	{
		TransactionAnalysis ta;
		if(cachedTransactionAnalysis.containsKey(codepoint))
			ta = cachedTransactionAnalysis.get(codepoint);
		else
		{
			ta = new TransactionAnalysis(codepoint);
			cachedTransactionAnalysis.put(codepoint, ta);
		}
		return ta.getBeginTransactionSet();
	}
	
	
	public static Set<Def> getDefSet(Silica silica)
	{
		DefAnalysis da;
		if(cachedDefAnalysis.containsKey(silica))
			da = cachedDefAnalysis.get(silica);
		else 
			da = new DefAnalysis(silica);
		
		return da.getDefSet();
	}
	
	public static Set<Use> getUseSet(Silica silica)
	{
		UseAnalysis ua;
		if(cachedUseAnalysis.containsKey(silica))
			ua = cachedUseAnalysis.get(silica);
		else
			ua = new UseAnalysis(silica);
		return ua.getUseSet();
	}
	
	public static Set<Codepoint> getDominatorSet(Codepoint codepoint, Set<Codepoint> relevantCodepoints)
	{
		DominatorAnalysis da = new DominatorAnalysis(codepoint, relevantCodepoints, cachedDominator);
		return da.getDominateCodepoints();
	}
	
	public static Set<Codepoint> getReachableSet(Codepoint codepoint, Set<Codepoint> relevantCodepoints) {
		ReachabilityAnalysis ra = new ReachabilityAnalysis(codepoint, relevantCodepoints, cachedReachability);
		return ra.getReachableCodepoints();
	}
	
	public static Set<Codepoint> getCodepointsFromSilicas(Set<Silica> silicas)
	{
		Set<Codepoint> codepoints = new HashSet<>();
		for(Silica silica : silicas)
			codepoints.add(silica.getCodepoint());
		return codepoints;
	}
}
