package sql.sand.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class APIToQueryTest extends BaseTest{

	@Test
	public void testQueryAPI() {
		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.APIRead");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(SELECT).*", classFilter);
		assertEquals(targetSilicas.size(), 5);
		
		Silica s1 = null, s2 = null, s3 = null, s4 = null, s5 = null;
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), true);
			int line = silica.getCodepoint().getSourceLineNumer();
			if(line == 25)
				s1 = silica;
			else if(line == 31)
				s2 = silica;
			else if(line == 51)
				s3 = silica;
			else if(line == 61)
				s4 = silica;
			else if(line == 76)
				s5 = silica;
		}
		
		Set<String> value1 = new HashSet<>();
		value1.add("select column1, column2 from student where column1 = ? or column1 = ? group by column3 having column1 = 1 order by column1");
		assertEquals(Analyzer.getQuerySet(s1), value1);
		
		Set<String> value2 = new HashSet<>();
		value2.add("select * from class where column1 = abc limit 20");
		assertEquals(Analyzer.getQuerySet(s2), value2);
		
		Set<String> value3 = new HashSet<>();
		value3.add("select column1, column2 from record where column2 = ? order by column2");
		value3.add("select column1, column2 from record where column2 = ? order by column1");
		value3.add("select column1, column2 from record where column1 = ? order by column2");
		value3.add("select column1, column2 from record where column1 = ? order by column1");
		assertEquals(Analyzer.getQuerySet(s3), value3);
		
		Set<String> value4 = new HashSet<>();
		value4.add("select c1, c2, c3 from record where _id=? limit 1");
		assertEquals(Analyzer.getQuerySet(s4), value4);
		
	}
	
	@Test
	public void testInsertAPI() {
		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.APIWrite");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(INSERT).*", classFilter);
		assertEquals(targetSilicas.size(), 3);
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), true);
			assertEquals(Analyzer.getQuerySet(silica).size(), 1);
		}
	}
	
	@Test
	public void testUpdateAPI() {
		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.APIWrite");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(UPDATE).*", classFilter);
		assertEquals(targetSilicas.size(), 2);
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), true);
			assertEquals(Analyzer.getQuerySet(silica).size(), 2);
		}
	}
	
	@Test
	public void testDeleteAPI() {
		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.APIWrite");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(DELETE).*", classFilter);
		assertEquals(targetSilicas.size(), 2);
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), true);
			assertEquals(Analyzer.getQuerySet(silica).size(), 1);
		}
	}
	/*
	@Test
	public void testTransactionAPI() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.APIWrite");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(BEGIN|END).*", classFilter);
		assertEquals(targetSilicas.size(), 3);
		for(Silica silica : targetSilicas)
		{
			assertEquals(Analyzer.getQuerySet(silica).size(), 1);
		}
	}
	*/
}
