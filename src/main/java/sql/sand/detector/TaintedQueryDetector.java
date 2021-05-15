package sql.sand.detector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sql.sand.abstraction.Def;
import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class TaintedQueryDetector {


	private static Set<String> taintSources = taintSource();
	private static Set<String> taintSource()
	{		
		Set<String> source = new HashSet<>();
		try{
	        BufferedReader br = null;
	        String line;
	        InputStream stream = TaintedQueryDetector.class.getResourceAsStream("/susi_source.txt");
	        br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	        while ((line = br.readLine()) != null) {
	        	String s = line.split(">")[0]+">";
	        	if(s.contains("<"))
	        	{
	        		source.add(s.toLowerCase());
	        	}
	        }
	        br.close();
		 }
	     catch(IOException ex)
	     {
	     	ex.printStackTrace();
	     }
		 return source;
	}
	//43 lines of code
	public static Set<Silica> detect(Set<String> classFilter)
	{	 
		Set<Silica> targetSilicas = new HashSet<>();
		Set<Silica> relevantSilicas = SilicaFinder.find("(INSERT|UPDATE|DELETE|SELECT).*", classFilter);
		List<Integer> length = new ArrayList<>();
		for(Silica silica : relevantSilicas)
		{
			length.add(silica.getCallChain().size());
			for(Def def : Analyzer.getDefSet(silica))
			{
				if(def.getType().equals("METHOD"))
				{
					if(taintSources.contains(def.getName()))
						targetSilicas.add(silica);
				}
			}
		}
		return targetSilicas;
	}
}
