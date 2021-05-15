package sql.sand.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.usc.sql.graphs.cfg.CFGInterface;
import soot.Scene;
import soot.SceneTransformer;
import soot.options.Options;
import sql.sand.abstraction.Codepoint;
import sql.sand.abstraction.Def;
import sql.sand.abstraction.Silica;
import sql.sand.abstraction.Transaction;
import sql.sand.abstraction.Use;
import sql.sand.analysis.helper.Pair;
import sql.sand.function.Analyzer;
import sql.sand.function.SilicaFinder;

public class AnalyzerTest extends BaseTest{

	@Test
	public void testQuerySet1()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(INSERT).*", classFilter);
		assertEquals(targetSilicas.size(), 2);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(Analyzer.getQuerySet(silica).size(), 1);
		}
	}
	
	@Test
	public void testQuerySet2()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(select name).*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		for(Silica silica : targetSilicas)
		{
			Set<String> result = new HashSet<>();
			result.add("select name from testing where id = 10");
			result.add("select name from testing where id = 14");
			assertEquals(Analyzer.getQuerySet(silica), result);
		}
	}
	
	@Test
	public void testQuerySet3()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(select data).*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(Analyzer.getQuerySet(silica).iterator().next(), "select data from testing where id = 1");
		}
	}
	
	@Test
	public void testQuerySet4()
	{
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Query");
		
		Set<Silica> targetSilicas = SilicaFinder.find("(select value).*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(Analyzer.getQuerySet(silica).iterator().next(), "select value from testing where id = abc10");
		}
	}
	@Test
	public void testNestedControlDependent() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.ControlDependent");
		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Pair<Codepoint,Integer>> controlDependents = Analyzer.getControlDependentSet(silica.getCodepoint());
		assertEquals(controlDependents.size(), 3);
		for(Pair<Codepoint,Integer> cp : controlDependents)
		{
			assertEquals(cp.getFirst().getCallChain().size(), 1);
		}
	}
	
	@Test
	public void testInterControlDependent() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.ControlDependent");
		Set<Silica> targetSilicas = SilicaFinder.find("UPDATE.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Pair<Codepoint,Integer>> controlDependents = Analyzer.getControlDependentSet(silica.getCodepoint());
		assertEquals(controlDependents.size(), 3);
		for(Pair<Codepoint,Integer> cp : controlDependents)
		{
			if(cp.getFirst().getSourceLineNumer() == 46 || cp.getFirst().getSourceLineNumer() == 49)
				assertEquals(cp.getFirst().getCallChain().size(), 2);
			else
				assertEquals(cp.getFirst().getCallChain().size(), 1);
		}
	}
	
	@Test
	public void testUseDependent() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.ControlDependent");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		for(Use use : Analyzer.getUseSet(silica))
		{
			if(use.getSelectedColumn() != null)
			{
				Set<Pair<Codepoint,Integer>> controlDependents = Analyzer.getControlDependentSet(use.getCodepoint());
				if(use.getCodepoint().getSourceLineNumer() == 17)
					assertEquals(controlDependents.size(), 1);
				else if(use.getCodepoint().getSourceLineNumer() == 20)
					assertEquals(controlDependents.size(), 2);
				else if (use.getCodepoint().getSourceLineNumer() == 26)
					assertEquals(controlDependents.size(), 3);
			}
		}

	}
	
	@Test
	public void testLoop() {

		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Loop");

		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);

		assertEquals(targetSilicas.size(), 2);
		
		for(Silica silica : targetSilicas)
		{
			assertEquals(Analyzer.getLoopSet(silica.getCodepoint()).size(), 2);
			/*
			for(Codepoint cp : Analyzer.getLoopSet(silica.getCodepoint()))
			{
			
				for(CFGInterface cfg : cp.getCallChain().values())
					System.out.println(cfg.getSignature());
				System.out.println();
			}
			*/
		}

	}

	@Test
	public void testDoWhileLoop() {

		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Loop");

		Set<Silica> targetSilicas = SilicaFinder.find("UPDATE.*", classFilter);

		assertEquals(targetSilicas.size(), 1);

		Silica silica = targetSilicas.iterator().next();

		assertEquals(Analyzer.getLoopSet(silica.getCodepoint()).size(), 1);

			/*
			for(Codepoint cp : Analyzer.getLoopSet(silica.getCodepoint()))
			{

				for(CFGInterface cfg : cp.getCallChain().values())
					System.out.println(cfg.getSignature());
				System.out.println();
			}
			*/


	}

	
	@Test
	public void testTransaction() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Transaction");
		Set<Silica> targetSilicas = SilicaFinder.find("UPDATE.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Codepoint> transaction = Analyzer.getTransactionSet(silica.getCodepoint());
		assertEquals(transaction.size(), 1);
		assertEquals(transaction.iterator().next().getCallChain().size(), 3);

	}
	
	
	@Test
	public void testInterTransaction() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.InterTransaction");
		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Codepoint> transaction = Analyzer.getTransactionSet(silica.getCodepoint());
		assertEquals(transaction.size(), 0);
		//System.out.println(Analyzer.getBeginTransactionSet(silica.getCodepoint()));
		//aassertEquals(transaction.iterator().next().getCallChain().size(), 3);

	}
	
	@Test
	public void testNestedTransaction() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Transaction");
		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Codepoint> transaction = Analyzer.getTransactionSet(silica.getCodepoint());
		assertEquals(transaction.size(), 2);
		for(Codepoint cp : transaction)
		{
			assertEquals(cp.getCallChain().size(), 1);
		}
	}
	
	@Test
	public void testUnbalancedTransaction() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.UnbalancedTransaction");
		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		
		Set<Codepoint> transaction = Analyzer.getTransactionSet(silica.getCodepoint());
		assertEquals(transaction.size(), 0);
		
	}
	
	@Test
	public void testDef() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Def");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);
		
		Silica silica = targetSilicas.iterator().next();
		Set<Def> defs = Analyzer.getDefSet(silica);
		assertEquals(defs.size(), 4);
	}
	
	@Test
	public void testUse() {

		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Use");

		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);

		
		Silica silica = targetSilicas.iterator().next();

		Set<Use> uses = Analyzer.getUseSet(silica);
		
		Use targetUse = null;
		for(Use use : uses)
		{
			if(use.getSelectedColumn() != null)
			{
				if(use.getLocation().getSourceLineNumber() == 25)
					targetUse = use;
			}
		}
		assertNotNull(targetUse);
		assertEquals(targetUse.getSelectedColumn().size(), 2);
	}
	
	/*
	@Test
	public void testUseAnnotation() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.UseRow");

		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 2);

		Use targetUse1 = null, targetUse2 = null;
		for(Silica silica : targetSilicas)
		{
			Set<Use> uses = Analyzer.getUseSet(silica);
			for(Use use : uses)
			{
				if(use.getLocation().getSourceLineNumber() == 31)
					targetUse1 = use;
				else if(use.getLocation().getSourceLineNumber() == 58)
					targetUse2= use;
			}
		}
		assertNotNull(targetUse1);
		Set<String> output1 = new HashSet<>();
		output1.add("id");
		assertEquals(targetUse1.getFilterByColumn(), output1);
		
		assertNotNull(targetUse2);
		Set<String> output2 = new HashSet<>();
		output2.add("id");
		output2.add("grade");
		assertEquals(targetUse2.getFilterByColumn(), output2);
	}
	*/
	
	@Test
	public void testInterUse1() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.InterUse1");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);

		
		Silica silica = targetSilicas.iterator().next();

		Set<Use> uses = Analyzer.getUseSet(silica);
		assertEquals(uses.size(), 6);

	}
	
	@Test
	public void testInterUse2() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.InterUse2");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);

		
		Silica silica = targetSilicas.iterator().next();

		Set<Use> uses = Analyzer.getUseSet(silica);
		for(Use use : uses)
			System.out.println(use);
		assertEquals(uses.size(), 7);

	}
	
	@Test
	public void testInterUse3() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.InterUse3");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 1);

		
		Silica silica = targetSilicas.iterator().next();

		Set<Use> uses = Analyzer.getUseSet(silica);
		
		Use targetUse = null;
		for(Use use : uses)
		{
			if(use.getLocation().getSourceLineNumber() == 19)
			{
				targetUse = use;
				
			}
		}
		assertNotNull(targetUse);
		assertEquals(targetUse.getSelectedColumn().iterator().next(), "name");

	}
	
	
	public void testDominator() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Dominator");
		Set<Silica> targetSilicas = SilicaFinder.find(".*", classFilter);
		assertEquals(targetSilicas.size(), 6);
		
		for(Silica silica : targetSilicas)
		{
			String prefix = silica.getStringQueries().iterator().next().toLowerCase();
			if(prefix.startsWith("select"))
			{
				assertEquals(Analyzer.getDominatorSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 1);
			}
			else if(prefix.startsWith("update"))
			{
				assertEquals(Analyzer.getDominatorSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 1);
			}
			else if(prefix.startsWith("replace"))
			{
				assertEquals(Analyzer.getDominatorSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 0);
			}
		}

	}
	
	@Test
	public void testReachable() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Reachable");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 3);
		
		Silica silica = null;
		for(Silica s : targetSilicas)
		{
			if(s.getLocation().getSourceLineNumber() == 13)
				silica = s;
		}

		assertNotNull(silica);
		
		assertEquals(Analyzer.getReachableSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 2);
	}
	@Test
	public void testReachable1() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Reachable");
		Set<Silica> targetSilicas = SilicaFinder.find("SELECT.*", classFilter);
		assertEquals(targetSilicas.size(), 3);
		
		Silica silica = null;
		for(Silica s : targetSilicas)
		{
			if(s.getLocation().getSourceLineNumber() == 20)
				silica = s;
		}

		assertNotNull(silica);
		
		assertEquals(Analyzer.getReachableSet(silica.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 0);
	}
	
	@Test
	public void testInterReachable() {
		Set<String> classFilter = new HashSet<>();
		classFilter.add("sql.sand.abstraction.testcase.Reachable");
		Set<Silica> targetSilicas = SilicaFinder.find("INSERT.*", classFilter);
		assertEquals(targetSilicas.size(), 3);
		
		Silica silica1 = null;
		for(Silica targetSilica : targetSilicas)
		{
			if(targetSilica.getLocation().getSourceLineNumber() == 39)
				silica1 = targetSilica;
		}
		
		assertNotNull(silica1);

		assertEquals(Analyzer.getReachableSet(silica1.getCodepoint(), Analyzer.getCodepointsFromSilicas(targetSilicas)).size(), 2);
	}
	
	
}
