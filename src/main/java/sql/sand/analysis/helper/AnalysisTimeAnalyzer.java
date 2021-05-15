package sql.sand.analysis.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalysisTimeAnalyzer {
	private static List<String> patterns = new ArrayList<>();
	public static void main(String[] args) {
		printInstanceInfo(args);
		//printTimeAndLineInfo(args);
	}
	private static void printInstanceInfo(String[] args) {
		List<String> file = readResultFile(args[1]);
		List<String> fps = readFPFile(args[2]);
		List<String> fileNonAug = readResultFile(args[3]);
		Map<String, Integer> patternToNumFp = new HashMap<>();
		Map<String, String> fpApps = new HashMap<>();
		for(String fp : fps)
		{
			if(fp.contains(".apk contains"))
			{
				fpApps.put(fp.split("->")[0], fp.split("->")[1]);
			}
			else
			{
				String pattern = fp.substring(fp.indexOf(":")+1, fp.indexOf("-"));
				int num = patternToNumFp.containsKey(pattern)? patternToNumFp.get(pattern) : 0;
				patternToNumFp.put(pattern, num + 1);
			}
		}
		Map<String, Set<String>> patternToApps = new LinkedHashMap<>();
		Set<String> appsContainPattern = new HashSet<>();
		Map<String, Integer> patternToNumIns = new LinkedHashMap<>();
		List<Integer> callChainLengthsOfInstance = new ArrayList<>();
		List<Integer> querySizeOfInstance = new ArrayList<>();
		for(String line : file)
		{
			if(line.contains(".apk contains "))
			{
				if(fpApps.containsKey(line))
				{
					line = fpApps.get(line);
				}
				String[] data = line.split(" ");
				String pattern = data[2];
				String app = data[0];
				if(!patternToApps.containsKey(pattern))
					patternToApps.put(pattern, new HashSet<>());
				
				if(line.contains("num:0"))
					continue;
				
				int instance = Integer.parseInt(line.split(" num:")[1]);
				int num = patternToNumIns.containsKey(pattern)? patternToNumIns.get(pattern) : 0;
				patternToNumIns.put(pattern, num + instance);
				patternToApps.get(pattern).add(app);
				appsContainPattern.add(app);
			}
			else if(line.contains(" Length:"))
			{
				callChainLengthsOfInstance.add(Integer.parseInt(line.split(":")[1]));
			}
			else if(line.contains(" possible size:"))
			{
				querySizeOfInstance.add(Integer.parseInt(line.split(":")[1]));
			}
				
		}
		System.out.println("-------------- Call chain length of detected instances --------------");
		Collections.sort(callChainLengthsOfInstance);
		System.out.println("Max call chain length:"+callChainLengthsOfInstance.get(callChainLengthsOfInstance.size()-1));

		System.out.println("Average call chain length:"+Math.round(callChainLengthsOfInstance.stream().mapToDouble(a -> a).average().getAsDouble()));
		int totalSize = callChainLengthsOfInstance.size();
		int interProceduralSize = 0;
		for(int i : callChainLengthsOfInstance)
		{
			if(i > 1)
				interProceduralSize++;
		}
		System.out.println("Percentage of inter-procedural detected instances:" + Math.round(interProceduralSize*1.0 / totalSize * 100) + "%");
		
		System.out.println("-------------- Query possible values of detected instances --------------");
		int totalInstanceSize = querySizeOfInstance.size();
		int multiQuerySize = 0;
		for(int i : querySizeOfInstance)
		{
			if(i > 1)
				multiQuerySize++;
		}
		System.out.println("Percentage of inter-procedural detected instances:" + Math.round(multiQuerySize*1.0 / totalInstanceSize * 100) + "%");
		
		
		
		//for # of apps that contain at least one SQL antipattern.
		for(String line : fileNonAug)
		{
			if(line.contains(".apk contains NotUsingParameterizedQueries"))
			{
				String[] data = line.split(" ");
				String app = data[0];
				if(line.contains("num:0"))
					continue;
				appsContainPattern.add(app);
			}
		}
		
		System.out.println("-------------- 4.2 Table 2 --------------");
		String[] patterns = {"UnbatchedWrites", "NotMergingProjectionPredicates", "NotMergingSelectionPredicates", "LoopToJoin"
				, "TaintedQuery", "NotUsingParameterizedQueries", "NotCaching", "UnnecessaryColumnRetrieval", "UnnecessaryRowRetrieval",
				"UnboundedQuery", "ReadablePassword"};
		for(String pattern : patterns)
		{
			int tp = (patternToNumIns.get(pattern)-(patternToNumFp.containsKey(pattern)? patternToNumFp.get(pattern):0));
			double precision = tp * 100.0 / patternToNumIns.get(pattern);
			System.out.println(pattern + " # TPs:" + tp + " Precision:" + precision + "%" + " # Apps:" +
					patternToApps.get(pattern).size()) ;
		}
		System.out.println("\nNumber of apps that contains at least one antipattern:"+ appsContainPattern.size() + "\n");
	}
	private static void printTimeAndLineInfo(String[] args) {
		List<String> file = readLogFile(args[0]);

		Map<String, Long> appToSootTime = new LinkedHashMap<>(); 
		Map<String, List<Long>> appToDetectorTime = new LinkedHashMap<>(); 
		
		List<Double> relevantPercent = new ArrayList<>();
		List<Double> silicaPercent = new ArrayList<>();
		List<Double> silicaOverRelevantPercent = new ArrayList<>();
		List<Integer> silicaCallChainLength = new ArrayList<>();
		
		int numOfAppsContainDB = 0;
		int numOfApps = 0;
		boolean startOfApp = false;
		List<Integer> allTotalLines = new ArrayList<>();
		long sootTime = 0;
		boolean count = false;
		for(String line : file)
		{
			if(line.startsWith("Soot started on"))
				startOfApp = true;
			//Total_lines:347490 Relevant_lines:988 Silica_lines:0
			if(line.contains("Total_lines:"))
			{
				String[] code = line.split(" ");
				int total = Integer.parseInt(code[0].split(":")[1]);
				
				if(startOfApp)
				{
					allTotalLines.add(total);
					startOfApp = false;
				}
				
				int relevant = Integer.parseInt(code[1].split(":")[1]);
				int silica  = Integer.parseInt(code[2].split(":")[1]);
				if(relevant!=0)
				{
					relevantPercent.add(relevant * 1.0 / total);
					silicaPercent.add(silica * 1.0 / total);
				}
				if(relevant != 0)
					silicaOverRelevantPercent.add(silica * 1.0 / relevant);
				
				if(count)
				{
					numOfApps++;
					if(relevant != 0)
						numOfAppsContainDB++;
					count  = false;
				}
				
			}
			else if(line.contains("Call chain length:"))
			{
				if(!line.equals("Call chain length:[]"))
				{
					String length = line.split(":")[1];
					String[] lengths = length.substring(1, length.length() - 1).split(",");
					for(String l : lengths)
					{
						silicaCallChainLength.add(Integer.parseInt(l.replace(" ", "")));
					}
				}
			}
			else if(line.contains("Running soot takes"))
			{
				sootTime = Long.parseLong(line.split(" ")[3].replace("ms", ""));
				count = true;
			}
			else if(line.contains(".apk spends"))
			{
				//Running NotMergingProjectionPredicates on /home/yingjun/Documents/SQLUsage/appset/1000apps/air.bahraniapps.expressivecomiccreator.apk spends 23ms
				String[] data = line.split(" ");
				String pattern = data[1];
				String app = data[3];
				Long time = Long.parseLong(data[5].replace("ms", ""));
				if(pattern.equals("UnbatchedWrites"))
					time -= sootTime;
				if(!patterns.contains(pattern))
					patterns.add(pattern);
				//if(time > 50000000)
				//{
					
				//	System.out.println(time / 60000 + " " + app + " " + pattern);
					//time = 0L;
				//}
				if(!appToSootTime.containsKey(app))
					appToSootTime.put(app, sootTime);
				
				if(!appToDetectorTime.containsKey(app))
				{
					List<Long> times = new ArrayList<>();
					times.add(time);
					appToDetectorTime.put(app, times);
				}
				else
				{
					appToDetectorTime.get(app).add(time);
				}
			}
		}
		
		List<Long> totalTime = new ArrayList<>();
		List<Double> sootPercent = new ArrayList<>();
		for(String app : appToDetectorTime.keySet())
		{
			long soot = appToSootTime.get(app);
			List<Long> detectorTimes = appToDetectorTime.get(app);
			long sum = 0;
			for(long detectorTime : detectorTimes)
			{
				sum += detectorTime;
			}
			sum += soot;
			totalTime.add(sum);
			double percent = soot * 1.0 / sum;
			sootPercent.add(percent);
		}
		
		Collections.sort(totalTime);
		
		OptionalDouble average = totalTime.stream().mapToDouble(a -> a).average();
		double mean = average.isPresent() ? average.getAsDouble() : 0;
		double median = totalTime.get(totalTime.size() / 2);
		

		DecimalFormat df2 = new DecimalFormat("#.##");
		
		System.out.println("-------------- 4.3 Analysis Time --------------");
		System.out.println("Average detection time for one app:" + Math.round(mean / 1000) + " s");
		System.out.println("Median detection time for one app:" + Math.round(median / 1000) + " s");
		System.out.println("Soot occupy analysis time:" + Math.round(sootPercent.stream().mapToDouble(a -> a).average().getAsDouble() * 100) + "%");
		
		for(int i = 0; i < totalTime.size(); i++)
		{
			if(totalTime.get(i) > 60000)
			{
				System.out.println("Percentage of apps that takes less than 60s:" + Math.round(i*1.0 / totalTime.size() * 100) + "%");
				break;
			}
		}
		
		System.out.println("\n-------------- 4.2 app size --------------");
		int lessThan10K = 0;
		int B10Kto100K = 0;
		int More100K = 0;
		for(int lines : allTotalLines)
		{
			if(lines < 10000)
				lessThan10K++;
			else if(lines > 100000)
				More100K++;
			else
				B10Kto100K++;
		}
		int size = allTotalLines.size();
		System.out.println("Less than 10K: " + Math.round(lessThan10K * 100.0 / size) + "% ");
		System.out.println("Between 10K to 100K: " + Math.round(B10Kto100K * 100.0 / size) + "% ");
		System.out.println("More than 100K: " + Math.round(More100K * 100.0 / size) + "% ");
		System.out.println();
		System.out.println("\n-------------- 3.5 Percentage of non-silica related --------------");
		//System.out.println("Removing non-db-related from total:"+df2.format((1 - relevantPercent.stream().mapToDouble(a -> a).average().getAsDouble())*100) + "%");
		System.out.println("Removing non-silicas-related from total:"+df2.format((1 - silicaPercent.stream().mapToDouble(a -> a).average().getAsDouble())*100) + "%");
		//System.out.println("Removing non-silicas-related from db-related:"+df2.format((1 - silicaOverRelevantPercent.stream().mapToDouble(a -> a).average().getAsDouble())*100) + "%");

		
		System.out.println("\n-------------- 3.1 Call chain length --------------");
		Collections.sort(silicaCallChainLength);
		System.out.println("Max call chain length:"+silicaCallChainLength.get(silicaCallChainLength.size()-1));

		System.out.println("Average call chain length:"+Math.round(silicaCallChainLength.stream().mapToDouble(a -> a).average().getAsDouble()));
		int totalSize = silicaCallChainLength.size();
		int interProceduralSize = 0;
		for(int i : silicaCallChainLength)
		{
			if(i > 1)
				interProceduralSize++;
		}
		System.out.println("Percentage of inter-procedural silicas:" + Math.round(interProceduralSize*1.0 / totalSize * 100) + "%");
		
		//System.out.println("Percentage of apps that use DB:" +  Math.round(numOfAppsContainDB*1.0 / numOfApps * 100) + "%");
		//print(appToDetectorTime);
	}

	private static void print(Map<String, List<Long>> appToDetectorTime)
	{

		for(int i = 0; i < patterns.size(); i++)
		{
			List<Long> patternTimes = new ArrayList<>();
			for(List<Long> times : appToDetectorTime.values())
			{
				patternTimes.add(times.get(i));
			}
			Collections.sort(patternTimes);
			

			double mean = patternTimes.stream().mapToDouble(a -> a).average().getAsDouble();
			double median = patternTimes.get(patternTimes.size() / 2);
			System.out.println(patterns.get(i));
			System.out.println("Average:" + Math.round(mean) + " ms" + " " + "Median:" + Math.round(median) + " ms");
		}
	
	}
	private static List<String> readLogFile(String path)
	{
		List<String> file = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(path))) 
		{
			file = stream.filter(line -> (line.contains(".apk spends") || line.contains("Running soot takes") || line.contains("Total_lines:") || line.contains("Call chain length:") || line.startsWith("Soot started on")))
					.collect(Collectors.toList());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return file;
	}
	
	private static List<String> readResultFile(String path)
	{
		List<String> file = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(path))) 
		{
			file = stream.filter(line -> (line.contains(".apk contains ") || line.startsWith("Antipattern:") || line.startsWith("Index:")
					|| line.contains(" possible size:") || line.contains(" Length:") || line.startsWith("Is ")))
					.collect(Collectors.toList());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return file;
	}
	
	private static List<String> readFPFile(String path)
	{
		List<String> file = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(path))) 
		{
			file = stream.filter(line -> (line.contains(".apk contains ") || line.startsWith("Index:")))
					.collect(Collectors.toList());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return file;
	}
}
