package sql.sand.detector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class UnbatchedWritesDetectorAugmented {

	/**
	 * Check if 
	 * (1) a query is the write type
	 * (2) it is executed inside a loop
	 * (3) it is not executed within a transaction
	 */
	
	//30 lines of code
	public static Set<Silica> detect(Set<String> classFilter)
	{
		Set<Silica> writeSilicas = SilicaFinder.find("^(INSERT|UPDATE|DELETE).*", classFilter);
		Set<Silica> targetSilicas = new HashSet<>();
		for(Silica silica : writeSilicas)
		{
			Set<Codepoint> loopSet = Analyzer.getLoopSet(silica.getCodepoint());
			if(!loopSet.isEmpty())
			{	
				Set<Codepoint> transactionSet =  Analyzer.getTransactionSet(silica.getCodepoint());
				if(transactionSet.isEmpty())
				{
					targetSilicas.add(silica);
				}
				else
				{
					boolean allTransactionOutsideLoop = true;
					for(Codepoint transaction : transactionSet)
					{
						Set<Codepoint> transactionLoopSet = Analyzer.getLoopSet(transaction);
						transactionLoopSet.retainAll(loopSet);
						if(!transactionLoopSet.isEmpty())
							allTransactionOutsideLoop = false;
					}
					if(!allTransactionOutsideLoop)
					{
						silica.setIsPlus(true);
						targetSilicas.add(silica);
					}
				}
			}
		}
		return targetSilicas;
	}
	
	
}
