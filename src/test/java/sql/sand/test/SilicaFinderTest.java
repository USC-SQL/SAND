package sql.sand.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import soot.Scene;
import soot.options.Options;
import sql.sand.abstraction.Silica;
import sql.sand.function.Analyzer;
import sql.sand.function.Reporter;
import sql.sand.function.SilicaFinder;

public class SilicaFinderTest extends BaseTest{

	@Test
	public void testSilicaFinder() {

		
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Finder");
		
		Set<Silica> targetSilicas2 = SilicaFinder.find("(SELECT).*", classFilter);
		assertEquals(targetSilicas2.size(), 2);
	}
	
	@Test
	public void testSilicaCallChain() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.CallChain");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(DELETE).*", classFilter);
		assertEquals(targetSilicas.size(), 4);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.getCallChain().size(), 3);
		}
		
		
	}
	
	@Test
	public void testIsHardCoded1()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(INSERT).*", classFilter);
		assertEquals(targetSilicas.size(), 2);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), false);
			assertEquals(silica.isHardCoded(), false);
		}
	}
	
	@Test
	public void testIsHardCoded2()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(select name).*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(silica.isAPI(), false);
			assertEquals(silica.isHardCoded(), false);
		}
	}
	
	@Test
	public void testIsHardCoded3()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.DataValue");
		for(Silica silica : SilicaFinder.find("insert.*", classFilter))
		{
			assertEquals(silica.isHardCoded(), true);
		}
	}
	
	
}
