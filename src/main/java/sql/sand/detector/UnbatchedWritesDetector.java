package sql.sand.detector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class UnbatchedWritesDetector {

	/**
	 * Check if 
	 * (1) a query is the write type
	 * (2) it is executed inside a loop
	 * (3) it is not executed within a transaction
	 */
	//12 lines of code
	public static Set<Silica> detect(Set<String> classFilter)
	{
		Set<Silica> writeSilicas = SilicaFinder.find("^(INSERT|UPDATE|DELETE).*", classFilter);
		Set<Silica> targetSilicas = new HashSet<>();
		for(Silica silica : writeSilicas)
		{
			if(!Analyzer.getLoopSet(silica.getCodepoint()).isEmpty() && Analyzer.getTransactionSet(silica.getCodepoint()).isEmpty())
			{	
				targetSilicas.add(silica);
			}
		}
		return targetSilicas;
	}
}
