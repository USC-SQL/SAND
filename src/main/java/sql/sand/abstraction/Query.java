package sql.sand.abstraction;

import java.util.List;

public class Query {

	private String query;
	private List<String> table;
	private List<String> columns;
	
	
	public Query(String query)
	{
		this.query = query;
	}
	
	public List<String> getTables()
	{
		return table;
	}
	
	public List<String> getColumns()
	{
		return columns;
	}
	
	public void setTable(List<String> table)
	{
		this.table = table;
	}
	
	public void setColumns(List<String> columns)
	{
		this.columns = columns;
	}
	
	public String toString()
	{
		return query;
	}
}
