package sql.sand.main;


import soot.*;
import sql.sand.detector.*;
import sql.sand.function.Reporter;

import java.util.*;

import android.media.MediaRouter.UserRouteInfo;

public class Main {

	/*
	 * args[0]: androidJar
	 * args[1]: apkPath
	 * args[2]: detectorNames
	 * args[3]: outputPath
	 */
	public static void main(String[] args) {
		setupAndInvokeSoot(args[0], args[1], args[2], args[3]);
	}

	static void setupAndInvokeSoot(String androidJarPath, String apkPath, String detectorNames, String outputPath) {
        String packName = "wjtp";
        String phaseName = "wjtp.string";
        String[] sootArgs = {
                "-w",
                //"-p", "cg.cha", "enabled:true",
                "-p", phaseName, "enabled:true",
                "-f", "n",
                "-keep-line-number",
                "-keep-offset",
                "-allow-phantom-refs",
                "-process-multiple-dex",
                "-process-dir", apkPath,
                "-src-prec", "apk",
                "-force-android-jar", androidJarPath
        };
		// Create the phase and add it to the pack
		Pack pack = PackManager.v().getPack(packName);
		pack.add(new Transform(phaseName, new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName,
											 Map<String, String> options) {
				for(String detectorName : detectorNames.split(":"))
				{

					long t1 = System.currentTimeMillis();
					try{
		                runDetector(apkPath, detectorName, outputPath);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
	                long t2 = System.currentTimeMillis();
	                System.out.println("Running " + detectorName + " on " + apkPath + " spends " + (t2-t1) + "ms");
				}
			}
		}));
		soot.Main.main(sootArgs);
	}
	
	private static final String 
			UNBATCHEDWRITES = "UnbatchedWrites",
			UNBATCHEDWRITES_AUG = "UnbatchedWrites_AUG",
			NOT_MERGING_PROJECTION = "NotMergingProjectionPredicates",
			NOT_MERGING_SELECTION = "NotMergingSelectionPredicates",
			TAINTED_QUERY = "TaintedQuery",
			UNPARAMETERIZED = "NotUsingParameterizedQueries",
			UNPARAMETERIZED_AUG = "NotUsingParameterizedQueries_AUG",
			READABLE_PASSWORD = "ReadablePassword",
			LOOP_TO_JOIN = "LoopToJoin",
			NOT_CACHING = "NotCaching",
			UNNECESSARYCOLUMNRETRIEVAL = "UnnecessaryColumnRetrieval",
			UNNECESSARYROWRETRIEVAL = "UnnecessaryRowRetrieval",
			UNBOUNDEDQUERY = "UnboundedQuery";
	
			
	private static void runDetector(String apkPath, String detectorName, String outputPath)
	{
		Reporter reporter = new Reporter(outputPath);
		switch(detectorName)
		{
			case UNBATCHEDWRITES:
			{
			//	reporter.reportSingle(UnbatchedWritesDetector.detect(null), apkPath, UNBATCHEDWRITES);
			//	break;
			//}
			//case UNBATCHEDWRITES_AUG:
			//{
				reporter.reportSingle(UnbatchedWritesDetectorAugmented.detect(null), apkPath, UNBATCHEDWRITES);
				break;
			}
			case NOT_CACHING:
			{
				reporter.reportPair(NotCachingDetector.detect(false, null), apkPath, NOT_CACHING);
				break;
			}
			case NOT_MERGING_PROJECTION:
			{
				reporter.reportPair(NotMergingProjectionDetector.detect(false, null), apkPath, NOT_MERGING_PROJECTION);
				break;
			}
			case NOT_MERGING_SELECTION:
			{
				reporter.reportPair(NotMergingSelectionDetector.detect(false, null), apkPath, NOT_MERGING_SELECTION);
				break;
			}
			case TAINTED_QUERY:
			{
				reporter.reportSingle(TaintedQueryDetector.detect(null), apkPath, TAINTED_QUERY);
				break;
			}
			case UNPARAMETERIZED:
			{
			//	reporter.reportSingle(UnparamerizedDetector.detect(null), apkPath, UNPARAMETERIZED);
			//	break;
			//}
			//case UNPARAMETERIZED_AUG:
			//{
				reporter.reportSingle(UnparamerizedDetectorAugmented.detect(null), apkPath, UNPARAMETERIZED);
				break;
			}
			case READABLE_PASSWORD:
			{
				reporter.reportSingle(ReadablePasswordDetector.detect(null), apkPath, READABLE_PASSWORD);
				break;
			}
			case LOOP_TO_JOIN:
			{
				//if(augmented)
					reporter.reportPair(LoopToJoinDetectorAugmented.detect(false, null), apkPath, LOOP_TO_JOIN);
				//else
				//	reporter.reportPair(LoopToJoinDetector.detect(false, null), apkPath, LOOP_TO_JOIN);
				break;
			}
			case UNNECESSARYCOLUMNRETRIEVAL:
			{
				reporter.reportSingle(UnnecessaryColumnRetrievalDetector.detect(null), apkPath, UNNECESSARYCOLUMNRETRIEVAL);
				break;
			}
			case UNNECESSARYROWRETRIEVAL:
			{
				reporter.reportSingle(UnnecessaryRowRetrievalDetector.detect(null), apkPath, UNNECESSARYROWRETRIEVAL);
				break;
			}
			case UNBOUNDEDQUERY:
			{
				reporter.reportSingle(UnboundedQueryDetector.detect(null), apkPath, UNBOUNDEDQUERY);
				break;
			}
			default:
			{
				System.err.println("Detector is not defined");
			}
		}
	}
	
	
	
}
