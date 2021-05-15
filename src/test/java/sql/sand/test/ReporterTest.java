package sql.sand.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import sql.sand.abstraction.Silica;
import sql.sand.analysis.helper.Pair;
import sql.sand.detector.NotCachingDetector;
import sql.sand.detector.NotMergingProjectionDetector;
import sql.sand.detector.NotMergingSelectionDetector;
import sql.sand.function.Reporter;

public class ReporterTest extends BaseTest{

	@Test
	public void testReportPairInSequence()
	{
		Set<Pair<Silica, Silica>> silicaPairs = NotMergingSelectionDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.abstraction.testcase.Report")));
		Reporter reporter = new Reporter("testreporter.txt");	
		int seqNum = reporter.reportPairInSequence(silicaPairs, "reportTest", "NotMergingSelectionDetector");
		//reporter.clear();
		assertEquals(seqNum, 6);		
	}
	
	@Test
	public void testReportPairInSequenceLoop()
	{
		Set<Pair<Silica, Silica>> silicaPairs = NotMergingProjectionDetector.detect(false, new HashSet<>(Arrays.asList("sql.sand.abstraction.testcase.Report")));
		Reporter reporter = new Reporter("testreporter.txt");
		int seqNum = reporter.reportPairInSequence(silicaPairs, "reportTest", "NotMergingProjectionDetector");
		reporter.clear();
		assertEquals(seqNum, 1);	

		
	}
}
