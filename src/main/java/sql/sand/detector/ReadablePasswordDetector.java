package sql.sand.detector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import sql.sand.abstraction.Def;
import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class ReadablePasswordDetector {

	// 30 lines of code
	private static Set<String> cryptographicLibClass = crypSource();
	private static Set<String> crypSource()
	{		
		Set<String> source = new HashSet<>();
		source.add("javax.crypto.Cipher");
		return source;
	}
	public static Set<Silica> detect(Set<String> classFilter)
	{	 
		Set<Silica> targetSilicas = new HashSet<>();
		Set<Silica> relevantSilicas = SilicaFinder.find("(.*(password =|password=|pwd =|pwd=).*)|(insert into.*(password|pwd).*)", classFilter);
		for(Silica silica : relevantSilicas)
		{
			boolean isEncrypted = false;
			for(Def def : Analyzer.getDefSet(silica))
			{
				if(def.getType().equals("METHOD"))
				{
					for(String lib : cryptographicLibClass)
					{
						if(def.getName().contains(lib))
							isEncrypted = true;
					}
				}
			}
			if(!isEncrypted)
				targetSilicas.add(silica);
		}
		return targetSilicas;
	}
}
