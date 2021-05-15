package sql.sand.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import sql.sand.abstraction.Silica;
import sql.sand.function.SilicaFinder;

public class QueryParserTest extends BaseTest{

	@Test
	public void testSilicaSelectedColumns() {
		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.SelectedColumns");

		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 5);
		
		
		for(Silica silica : targetSilicas)
		{
			String query = silica.getStringQueries().iterator().next();
			int selectColumnSize = silica.getQueryToSelectedColumns().get(query).size();
			if(query.startsWith("select avg"))
			{
				assertEquals(selectColumnSize, 1);
			}	
			else if(query.startsWith("select *"))
			{
				assertEquals(selectColumnSize, 3);
			}
			else if(query.startsWith("select id"))
			{
				assertEquals(selectColumnSize, 1);
			}
			else if(query.startsWith("select a.pkg_name"))
			{
				assertEquals(selectColumnSize, 2);
			}
			else if(query.startsWith("select a.*"))
			{
				assertEquals(selectColumnSize, 3);
			}
		}
	}
	
	@Test
	public void testSilicaDataValues() {

		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.DataValue");


		for(Silica silica : SilicaFinder.find("insert.*", classFilter))
		{
			assertEquals(silica.getQueryToDataValues().entrySet().
					iterator().next().getValue().size(), 2);
		}
		
		for(Silica silica :  SilicaFinder.find("select.*", classFilter))
		{
			assertEquals(silica.getQueryToDataValues().entrySet().
					iterator().next().getValue().size(), 2);
		}
		
		for(Silica silica : SilicaFinder.find("update.*", classFilter))
		{
			assertEquals(silica.getQueryToDataValues().entrySet().
					iterator().next().getValue().size(), 2);
		}
		
		for(Silica silica : SilicaFinder.find("delete.*", classFilter))
		{
			assertEquals(silica.getQueryToDataValues().entrySet().
					iterator().next().getValue().size(), 2);
		}

	}
}
