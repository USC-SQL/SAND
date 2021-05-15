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

public class NotCachingDetectorAugmented {
	/**
	 * Check if 
	 * (1) the second query is executed after the first query is executed
	 * (2) the queries are identical or one contains another
	 * 
	 * or 
	 * 
	 * (1) the query is executed inside a loop
	 */ 
	
	//47 lines of code
	public static Set<Pair<Silica, Silica>> detect(boolean isMust, Set<String> classFilter)
	{
		Set<Pair<Silica, Silica>> targetSilicaPairs = new HashSet<>();
		Set<Silica> relevantSilicas = SilicaFinder.find("(^(select)(?!.*( limit 0|\\?|unknown@)).*)", classFilter);
		Set<Silica> readSilicas = new HashSet<>();
		Set<Silica> writeSilicas = new HashSet<>();
		for(Silica silica : relevantSilicas)
		{
			boolean isRead = false;
			for(String sql : Analyzer.getQuerySet(silica))
			{
				if(sql.startsWith("select"))
				{
					readSilicas.add(silica);
					isRead = true;
					break;
				}
			}
			if(!isRead)
				writeSilicas.add(silica);
		}
		for(Silica silica : readSilicas)
		{
			Set<Codepoint> reachSet = Analyzer.getReachableSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(relevantSilicas));
			Set<Silica> comExpSecondSilicas = new HashSet<>();
			for(Silica secondReadSilica : readSilicas)
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
		for(Silica silica : readSilicas)
		{
			Set<Codepoint> readLoops = Analyzer.getLoopSet(silica.getCodepoint());
			if(!readLoops.isEmpty())
			{
				silica.setMetaInfo("Loop at:\n" + readLoops.iterator().next());
				targetSilicaPairs.add(new Pair<Silica, Silica>(silica, silica));
			}	
		}
		return targetSilicaPairs;
	}
}
