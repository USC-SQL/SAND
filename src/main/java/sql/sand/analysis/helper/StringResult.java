package sql.sand.analysis.helper;

import java.util.Map;
import java.util.Set;

public class StringResult
{
	public Set<String> possibleValues;
	public boolean isAPI;
	public Map<String, Integer> irOp;
	public StringResult(Set<String> possibleValues, boolean isAPI, Map<String, Integer> irOp)
	{
		this.possibleValues = possibleValues;
		this.isAPI = isAPI;
		this.irOp = irOp;
	}
	
	public String toString()
	{
		return possibleValues.toString();
	}
}

