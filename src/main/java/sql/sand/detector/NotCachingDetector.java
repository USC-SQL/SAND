package sql.sand.detector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Silica;
import sql.sand.analysis.QueryAnalysis;
import sql.sand.analysis.helper.Pair;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class NotCachingDetector {
	/**
	 * Check if 
	 * (1) the second query is executed after the first query is executed
	 * (2) the queries are identical
	 */ 
	
	//21 lines of code
	public static Set<Pair<Silica, Silica>> detect(boolean isMust, Set<String> classFilter)
	{
		Set<Pair<Silica, Silica>> targetSilicaPairs = new HashSet<>();
		Set<Silica> relevantSilicas = SilicaFinder.find("(^(select)(?!.*( limit 0|\\?|unknown@)).*)", classFilter);
		for(Silica silica : relevantSilicas)
		{
			Set<Codepoint> reachSet = Analyzer.getReachableSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(relevantSilicas));
			Set<Silica> comExpSecondSilicas = new HashSet<>();
			for(Silica secondReadSilica :relevantSilicas)
				if(reachSet.contains(secondReadSilica.getCodepoint()))
					for(String firstQuery : Analyzer.getQuerySet(silica))
						for(String secondQuery: Analyzer.getQuerySet(secondReadSilica))
							if(firstQuery.equals(secondQuery))
								comExpSecondSilicas.add(secondReadSilica);
		
			for(Silica secondReadSilica : comExpSecondSilicas)
			{
				targetSilicaPairs.add(new Pair<Silica, Silica>(silica, secondReadSilica));
			}
		}
		return targetSilicaPairs;
	}
}
