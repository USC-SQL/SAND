package sql.sand.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fj.P;
import org.junit.Test;

import sql.sand.abstraction.Silica;
import sql.sand.analysis.helper.Pair;
import sql.sand.detector.LoopToJoinDetector;
import sql.sand.detector.LoopToJoinDetectorAugmented;
import sql.sand.detector.NotCachingDetector;
import sql.sand.detector.NotCachingDetectorAugmented;
import sql.sand.detector.NotMergingProjectionDetector;
import sql.sand.detector.NotMergingProjectionDetector;
import sql.sand.detector.NotMergingSelectionDetector;
import sql.sand.detector.ReadablePasswordDetector;
import sql.sand.detector.TaintedQueryDetector;
import sql.sand.detector.UnbatchedWritesDetector;
import sql.sand.detector.UnbatchedWritesDetectorAugmented;
import sql.sand.detector.UnboundedQueryDetector;
import sql.sand.detector.UnnecessaryColumnRetrievalDetector;
import sql.sand.detector.UnnecessaryRowRetrievalDetector;
import sql.sand.detector.UnparamerizedDetector;

public class DetectorTest extends BaseTest{

	@Test
	public void testUnbatchedWritesDetector()
	{
		assertEquals(UnbatchedWritesDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.UnbatchedWrites"))).size(), 1);
	}
	
	@Test
	public void testUnbatchedWritesDetectorAugmented()
	{
		assertEquals(UnbatchedWritesDetectorAugmented.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.UnbatchedWritesAugmented"))).size(), 1);
	}
	
	@Test
	public void testNotMergingProjectionDetector()
	{
		//assertEquals(NotMergingProjectionDetector.detect(true, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Projection"))).size(), 0);
		
		assertEquals(NotMergingProjectionDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Projection"))).size(), 1);
	}
	
	@Test
	public void testNotMergingProjectionDetectorAugmented()
	{
		assertEquals(NotMergingProjectionDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Projection"))).size(), 1);
	}
	
	
	@Test
	public void testNotMergingSelectionDetector()
	{
		//assertEquals(NotMergingSelectionDetector.detect(true, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Selection"))).size(), 2);
		
		assertEquals(NotMergingSelectionDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Selection"))).size(), 3);
	}
	
	@Test
	public void testLoopToJoinDetector()
	{
		assertEquals(LoopToJoinDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.LoopToJoin"))).size(), 1);
	}
	
	@Test
	public void testLoopToJoinDetectorAugmented()
	{
		assertEquals(LoopToJoinDetectorAugmented.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.LoopToJoinAugmented"))).size(), 1);
	}
	@Test
	public void testTaintedQueryDetector()
	{
		assertEquals(TaintedQueryDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Taint"))).size(), 2);
	}
	
	@Test
	public void testUnparameterizedDetector()
	{
		assertEquals(UnparamerizedDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Parameterize"))).size(), 2);
	}
	
	@Test 
	public void testReadablePassward()
	{
		assertEquals(ReadablePasswordDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Password"))).size(), 3);
	}
	
	@Test
	public void testNotCachingDetector()
	{
		//assertEquals(NotCachingDetectorAugmented.detect(true, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Cache"))).size(), 2);

		Set<Pair<Silica,Silica>> silicas = NotCachingDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Cache")));
		assertEquals(silicas.size(), 1);
	}
	
	@Test
	public void testUnnecessaryColumnRetrievalDetector()
	{
		assertEquals(UnnecessaryColumnRetrievalDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.ColumnRetrieval"))).size(), 2);
	}
	
	@Test
	public void testUnnecessaryRowRetrievalDetector()
	{
		assertEquals(UnnecessaryRowRetrievalDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.RowRetrieval"))).size(), 3);
	}
	
	@Test
	public void testUnnecessaryRowRetrievalDetector1()
	{
		assertEquals(UnnecessaryRowRetrievalDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.RowRetrievalIfElse"))).size(), 0);
	}
	
	@Test
	public void testUnboundedQueryDetector()
	{
		Set<Silica> silicas = UnboundedQueryDetector.detect(new HashSet<>(Arrays.asList("sql.sand.antipattern.testcase.Unbound")));
		assertEquals(silicas.size(), 2);
		int isIterate = 0;
		for(Silica s : silicas)
			if(s.isIterate())
				isIterate++;
		assertEquals(isIterate, 1);
	}
	
}
