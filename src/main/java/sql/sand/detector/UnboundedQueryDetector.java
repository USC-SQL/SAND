package sql.sand.detector;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Silica;
import sql.sand.abstraction.Use;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class UnboundedQueryDetector {
	/**
	 * Check if 
	 * (1) a select query can possibly retrieve an unbounded number of records
	 * (2) the retrieved results is iterated one by one 
	 */

	/*
	 * A query is bounded if
	 	(1) the query always returns a single value (e.g., a COUNT query); 
	 	(2) the query always returns a single record (e.g., retrieving using a unique identifier);
	 	(3) the query uses a LIMIT keyword bounding the number of returned records.
	 */
	
	//79 lines of code
	public static Set<Silica> detect(Set<String> classFilter)
	{	
		Set<Silica> targetSilicas = new HashSet<>();
		Set<Silica> relevantSilicas = SilicaFinder.find("^select.*", classFilter);
		for(Silica silica : relevantSilicas)
		{
			boolean isBounded = false;
			//(1) returns a single value
			for(Entry<String, List<String>> queryToSelectedColumns : silica.getQueryToSelectedColumns().entrySet())
			{
				if(queryToSelectedColumns.getValue().size() == 1)
				{
					String selectedColumn = queryToSelectedColumns.getValue().get(0);
					String [] singleValueFuncs = {"count", "avg", "sum", "min", "max"};
					for(String func : singleValueFuncs)
					{
						if(selectedColumn.startsWith(func + "(") 
								|| queryToSelectedColumns.getKey().startsWith("select " + func + "("))
							isBounded = true;
					}
				}
			}
			for(String query : Analyzer.getQuerySet(silica))
			{
				StringBuilder qb = new StringBuilder(query);
				while(qb.indexOf("'") != -1)
				{
					int a1 = qb.indexOf("'");
					int a2 = qb.indexOf("'", a1+1);
					if(a2 == -1)
						qb.replace(a1, a1+1, "");
					else
						qb.replace(a1, a2+1, "$$$");
				}
				query = qb.toString();
				//(2) returns a single record (currently judging by if there exists any equality comparison)
				if(query.contains("="))
				{
					isBounded = true;
				}
				//(3) uses a LIMIT keyword
				if(query.contains("limit"))
				{
					isBounded = true;
				}
			}
			//the retrieved results are iterated one by one
			if(!isBounded)
			{
				Set<Use> useSet = Analyzer.getUseSet(silica);
				Set<Codepoint> useCodepoints = new HashSet<>();
				for(Use use : useSet)
				{
					useCodepoints.add(use.getCodepoint());
				}
				boolean isIterate = false;
				for(Use use : useSet)
				{

					if(use.getSelectedColumn() != null)
					{
						for(Codepoint loopHeader: Analyzer.getLoopSet(use.getCodepoint()))
						{
							//System.out.println("Header:" + loopHeader.getStatement());
							if(useCodepoints.contains(loopHeader))
							{
								silica.setMetaInfo("Loop at:\n" + loopHeader);
								isIterate = true;
								break;
							}
						}
					}
					if(isIterate)
						break;
				}
				silica.setIterate(isIterate);
				targetSilicas.add(silica);
				//if(isIterate)
				//	targetSilicas.add(silica);
			}
		}
		return targetSilicas;
	}
}
