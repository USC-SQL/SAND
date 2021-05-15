package sql.sand.analysis.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportComparer {

	public static void main(String[] args) {
		List<String> file1 = readFile(args[0]);
		List<String> file2 = readFile(args[1]);
		for(String anti : file2)
		{
			if(!file1.contains(anti))
				System.out.println(anti);
		}
	}
	
	private static List<String> readFile(String path)
	{
		List<String> file = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(path))) 
		{
			file = stream.filter(line -> line.contains(".apk contains UnbatchedWrites num:"))
					.collect(Collectors.toList());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return file;
	}
}
