package sql.sand.detector;

import java.util.Set;

import sql.sand.abstraction.Silica;
import sql.sand.function.SilicaFinder;

public class UseRandomDetector {
	public static Set<Silica> detect(Set<String> classFilter)
	{	 
		//checking if query matches pattern select * | insert (something other than a left parenthesis) values 
		Set<Silica> targetSilicas = SilicaFinder.find(".*random().*", classFilter);
		return targetSilicas;
	}
}
