package sql.sand.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import CallGraph.StringCallGraph;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import sql.sand.abstraction.Query;
import sql.sand.analysis.helper.APIToQueryProcessor;
import sql.sand.analysis.helper.Parameter;
import sql.sand.analysis.helper.StringResult;
import usc.sql.string.JavaAndroid;

public class QueryAnalysis {

	private JavaAndroid stringAnalysis;
	private Map<String, List<Integer>> target;

	private static Set<String> failToParseQuery = new HashSet<>();
	private static Map<String, Statement> queryToParsedQuery = new HashMap<>();
	private static Map<String, String> queryToQueryWithoutUnknownMark = new HashMap<>();
	
	private Map<String, StringResult> stringAnalysisResult = new HashMap<>();
	private Set<String> sqliteAPIs = new HashSet<>();
	private static final boolean ANALYZE_API = true, ANALYZE_RAW = true;
	
	public QueryAnalysis(StringCallGraph callGraph)
	{
		List<Integer> paraSet = new ArrayList<>(Arrays.asList(0));
		target = new HashMap<>();
		
		if(ANALYZE_RAW)
		{
			target.put("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)>",paraSet);
			target.put("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String,java.lang.Object[])>",paraSet);
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[])>",paraSet);
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[],android.os.CancellationSignal)>",paraSet);
			
			
			List<Integer> paraSet2 = new ArrayList<>(Arrays.asList(1));
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQueryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,java.lang.String,java.lang.String[],java.lang.String)>", paraSet2);
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQueryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,java.lang.String,java.lang.String[],java.lang.String,android.os.CancellationSignal)>", paraSet2);
		}
		
		if(ANALYZE_API)
		{
			target.put("<android.database.sqlite.SQLiteDatabase: long insert(java.lang.String,java.lang.String,android.content.ContentValues)>", 
					new ArrayList<>(Arrays.asList(0, 2)));
			//target.put("<android.database.sqlite.SQLiteDatabase: long insertOrThrow(java.lang.String,java.lang.String,android.content.ContentValues)>", paraSet);
			//target.put("<android.database.sqlite.SQLiteDatabase: long insertWithOnConflict(java.lang.String,java.lang.String,android.content.ContentValues,int)>", paraSet);
			
			List<Integer> paraSet3 = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
			target.put("<android.database.sqlite.SQLiteDatabase: int update(java.lang.String,android.content.ContentValues,java.lang.String,java.lang.String[])>", paraSet3);
			//target.put("<android.database.sqlite.SQLiteDatabase: int updateWithOnConflict(java.lang.String,android.content.ContentValues,java.lang.String,java.lang.String[],int)>", paraSet3);
	
			List<Integer> paraSet4 = new ArrayList<>(Arrays.asList(0, 1, 2));
			target.put("<android.database.sqlite.SQLiteDatabase: int delete(java.lang.String,java.lang.String,java.lang.String[])>", paraSet4);
			
		
			
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor query(boolean,java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)>
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor query(boolean,java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String,android.os.CancellationSignal)>
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor queryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,boolean,java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)>
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor queryWithFactory(android.database.sqlite.SQLiteDatabase$CursorFactory,boolean,java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String,android.os.CancellationSignal)>
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String)>
			// <android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)>
			 
			List<Integer> paraSet5 = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String)>"
					, paraSet5);
	
			List<Integer> paraSet6 = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
			target.put("<android.database.sqlite.SQLiteDatabase: android.database.Cursor query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)>"
					, paraSet6);
		}
		
		for(String sig : target.keySet())
		{
			if(!sig.contains("execSQL") && !sig.contains("rawQuery"))
				sqliteAPIs.add(sig);
		}
		
		stringAnalysis = new JavaAndroid(callGraph, target, 1, null);
		
		processStringAnalysisResult();

		
	}

	private void processStringAnalysisResult()
	{
		Map<String, List<Parameter>> callPathIdToParameters = new HashMap<>();
		for(String callPathId : stringAnalysis.getInterpretedValues().keySet())
		{
			Set<String> stringValues = stringAnalysis.getInterpretedValues().get(callPathId);
			Map<String, Integer> irOps = stringAnalysis.getIROpStatistics().get(callPathId);
			//remove the para index from the id
			int paraIndex = Integer.parseInt(callPathId.substring(callPathId.lastIndexOf("@") + 1));
			callPathId = callPathId.substring(0, callPathId.lastIndexOf("@"));
			String apiSig = callPathId.substring(callPathId.lastIndexOf("@") + 1);
			//remove the API signature from the id
			callPathId = callPathId.substring(0, callPathId.lastIndexOf("@"));
			
			if(sqliteAPIs.contains(apiSig))
			{
				Parameter parameter = new Parameter(apiSig, paraIndex, stringValues);
				if(callPathIdToParameters.containsKey(callPathId))
					callPathIdToParameters.get(callPathId).add(parameter);
				else
				{
					List<Parameter> paraValues = new ArrayList<>();
					paraValues.add(parameter);
					callPathIdToParameters.put(callPathId, paraValues);
				}
			}
			else
			{
				//rawQuery and execSQL results can be directed put into the output
				stringAnalysisResult.put(callPathId, new StringResult(stringValues, false, irOps));
			}
		}
		//SQLite APIs need to be converted into raw queries
		for(String callPathId : callPathIdToParameters.keySet())
		{
			Set<String> stringValues = APIToQueryProcessor.convertAPIToQuery(callPathIdToParameters.get(callPathId));
			stringAnalysisResult.put(callPathId, new StringResult(stringValues, true, null));
		}
	}
	
	public Map<String, StringResult> getStringAnalysisResult()
	{
		return stringAnalysisResult;
	}
	
	public List<Integer> getTargetParameter(String sig)
	{
		return target.get(sig);
	}
	
	public static final String UNKNOWN_MARKER = "$$$";
	private static String replaceStringAnalysisUnknownMark(String inputQuery)
	{
		if(queryToQueryWithoutUnknownMark.containsKey(inputQuery))
			return queryToQueryWithoutUnknownMark.get(inputQuery);
		else
		{
			StringBuilder qb = new StringBuilder(inputQuery);
			while(qb.indexOf("unknown@") != -1)
			{
				int a1 = qb.indexOf("unknown@");
				int a2 = qb.indexOf("!!!");
				if(a2 == -1)
					break;
				else
					qb.replace(a1, a2+3, UNKNOWN_MARKER);
			}
			String query = qb.toString();
			queryToQueryWithoutUnknownMark.put(inputQuery, query);
			return query;
		}
	}
	
	public static Set<String> convertToQueryString(Set<String> queries)
	{
		Set<String> querySet = new HashSet<>();
		for(String query : queries)
		{
			query = replaceStringAnalysisUnknownMark(query);
			querySet.add(query);
		}
		return querySet;
	}

	public static String parseTableName(String query) {
		String tableName = null;
		if(query.startsWith("create table"))
		{
			if(query.contains("(") && query.contains(")"))
			{
				tableName = query.substring(0, query.indexOf("("))
						.replace("create table if not exists", "")
						.replace("create table", "")
						.trim();
			}
		}
		
		return tableName;
	}
	
	public static String parseTableForm(String value) {
		if(value.startsWith("create table"))
		{
			try
			{
				String query = value;
				query = query.replaceAll("\\r\\n|\\r|\\n|\\\\n|\\\\t", " ").trim().replaceAll(" +", " ").replace(" on conflict replace", "").replace(" on conflict ignore", "");
				if(failToParseQuery.contains(query))
					return null;
				Statement statement;
				if(queryToParsedQuery.containsKey(query))
					statement = queryToParsedQuery.get(query);
				else
					statement = (Statement) CCJSqlParserUtil.parse(query);
				if(statement instanceof CreateTable)
	            {
					List<String> columnTypes = new ArrayList<>();
					CreateTable createTable = (CreateTable) statement;
	                for(ColumnDefinition column : createTable.getColumnDefinitions())
	                {
	                	columnTypes.add(column.getColDataType().getDataType());
	                }
	                Collections.sort(columnTypes);
	                return columnTypes.toString();
	            }
			}
			catch (Throwable e) {
				failToParseQuery.add(value);
				System.err.println(value);
				//e.printStackTrace();
			}
		}
		return null;
	}
	
	public static List<String> parseTableColumns(String query) {
		List<String> columnNames = null;
		//collect table form info
		if(query.startsWith("create table"))
		{
			columnNames = new ArrayList<>();
			query = query.replaceAll("[\r\n]+", " ");

			if(query.contains("(") && query.contains(")"))
			{
				String[] columns = query.substring(query.indexOf("(")+1, query.lastIndexOf(")"))
						.split(",(?![^(]*\\))");
				//remove column name
				for(int i = 0; i < columns.length; i++)
				{
					String trimmed = columns[i].trim();
					if(!trimmed.contains("(") && trimmed.contains(" "))
					{
						String columnName = trimmed.substring(0, trimmed.indexOf(" "));
						columnNames.add(columnName);
					}
				}
			}
		}
		return columnNames;
	}

	public static Set<String> getRelatedTableNames(String inputQuery)
	{
		Set<String> tableNames = new HashSet<>();
		String[] queries = inputQuery.replace("insert or ignore", "insert")
				.replace("insert or replace", "insert")
				.replace("\\(  \\)", "").replace("[\r\n]+", " ").split("union");
		for(String query : queries)
		{
			try
			{
				query = replaceStringAnalysisUnknownMark(query);
				
				if(failToParseQuery.contains(query))
					continue;
				Statement statement;
				if(queryToParsedQuery.containsKey(query))
					statement = queryToParsedQuery.get(query);
				else
					statement = (Statement) CCJSqlParserUtil.parse(query);
				//System.out.println(statement.getClass());
				if(statement instanceof Insert)
				{
					Insert in = (Insert) statement;
					//insert into table values (x, y , z)
					if(in.getItemsList() instanceof ExpressionList)
					{
						String tableName = in.getTable().getName();
						if(tableName != null)
							tableNames.add(tableName);
					}
					//insert into table select * from table2
					else if(in.getSelect() != null)
					{
						tableNames.addAll(getRelatedTableNames(in.getSelect().toString()));
					}
		
				}
				else if(statement instanceof Update)
				{
					Update up = (Update) statement;
					//TODO: handle complicated expressions
					for(Table table : up.getTables())
					{
						String tableName = table.getName();
						if(tableName != null)
							tableNames.add(tableName);
					}
					
				}
				else if(statement instanceof Delete)
				{
					Delete de = (Delete) statement;
					Table table = de.getTable();
					if(table != null && table.getName() != null)
						tableNames.add(table.getName());
					
				}
				//the parser cannot parse replace statement properly, ignore for now.
				else if(statement instanceof Replace)
				{
					
				}
				else if(statement instanceof Select)
				{
					Select se = (Select) statement;
			
					PlainSelect pse = (PlainSelect) se.getSelectBody();
					
					String tableName = null;
					
					FromItem fromItem = pse.getFromItem();
					if(fromItem instanceof Table)
					{
						Table table = (Table) fromItem;
						tableName = table.getName();
						if(tableName != null)
							tableNames.add(tableName);
						
					}
					//TODO: instanceof SubSelect and SubJoin
					else if(fromItem instanceof SubSelect)
					{
						throw new Exception("Do not support identifying tables from SubSelect currently");
					}
					else if(fromItem instanceof SubJoin)
					{
						throw new Exception("Do not support identifying tables from SubJoin currently");
					}
					
				}
			}
			catch(Throwable e)
			{
				failToParseQuery.add(query);
				//e.printStackTrace();
			}
		}
		return tableNames;
	
	}
	
	public static List<String> getSelectedColumns(String inputQuery,
			Map<String, List<String>> tableToColumns) {
		List<String> selectedColumns = new ArrayList<>();;
		//Union queries must have the same column distribution, which means analyzing one of them is sufficient
		String query = inputQuery.split("union")[0];
		query = replaceStringAnalysisUnknownMark(query);
		if(query.startsWith("select"))
		{
			try {
				
				if(failToParseQuery.contains(query))
					return null;
				Statement statement;
				if(queryToParsedQuery.containsKey(query))
					statement = queryToParsedQuery.get(query);
				else
					statement = (Statement) CCJSqlParserUtil.parse(query);
				if(statement instanceof Select)
				{
					Select se = (Select) statement;
					PlainSelect pse = (PlainSelect) se.getSelectBody();
					String tableName = null;
					Map<String, String> tableAliasMapping = new HashMap<>();
					FromItem fromItem = pse.getFromItem();
					if(fromItem instanceof Table)
					{
						Table table = (Table) fromItem;
						tableName = table.getName();
						if(table.getAlias()!= null)
						{
							tableAliasMapping.put(table.getAlias().getName(), tableName);
						}
						
					}
					//TODO: instanceof SubSelect and SubJoin
					else if(fromItem instanceof SubSelect)
					{
						throw new Exception("Do not support identifying columns from SubSelect currently");
					}
					else if(fromItem instanceof SubJoin)
					{
						throw new Exception("Do not support identifying columns from SubJoin currently");
					}
					List<SelectItem> items = pse.getSelectItems();
					
					for(SelectItem item : items)
					{
						//select *
						if(item instanceof AllColumns)
						{
							//TODO: handle joins
							if(pse.getJoins() != null)
								throw new Exception("Do not support identifying all columns from joins currently");
							
							if(tableName != null)
							{
								if(tableToColumns.get(tableName) == null)
									throw new Exception("Could not identify table with name:" + tableName);
								else
									selectedColumns.addAll(tableToColumns.get(tableName));
							}
							else
								throw new Exception("Could not identify table name in:" + query);
						}
						//SELECT a.* FROM msgs a
						else if(item instanceof AllTableColumns)
						{
							//TODO: handle joins
							if(pse.getJoins() != null)
								throw new Exception("Do not support identifying all columns from joins currently");
							AllTableColumns allTableColumns = (AllTableColumns) item;
							tableName = allTableColumns.getTable().getName();
							if(tableToColumns.get(tableName) == null)
							{
								String aliasName = tableAliasMapping.get(tableName);
								if(aliasName != null)
								{
									if(tableToColumns.get(aliasName) != null)
										selectedColumns.addAll(tableToColumns.get(aliasName));
									else
										throw new Exception("Could not identify table with name:" + tableName + " or alias name:" + aliasName);
								}
								else
									throw new Exception("Could not identify table with name:" + tableName);
							}
							else
								selectedColumns.addAll(tableToColumns.get(tableName));
						}
						else if(item instanceof SelectExpressionItem)
						{
							SelectExpressionItem selectExpressionItem = (SelectExpressionItem) item;
							Expression exp = selectExpressionItem.getExpression();
							if(selectExpressionItem.getAlias() == null)
							{
								String column;
								if(exp.toString().contains("."))
									column = exp.toString().split("\\.")[1];
								else
									column = exp.toString();
								selectedColumns.add(column);
							}
							else
							{
								selectedColumns.add(selectExpressionItem.getAlias().getName());
							}
							
						}
					}				
				}
			} 
			catch (Throwable e) {
				failToParseQuery.add(query);
			}
		}
		if(selectedColumns.isEmpty())
			return null;
		else
		{
			//unknown field array
			if(selectedColumns.size() == 1 && selectedColumns.get(0).equals(UNKNOWN_MARKER))
				return null;
			else
				return selectedColumns;
		}
	}
	
	public static List<String> getDataValues(String inputQuery)
	{
		List<String> dataValues = new ArrayList<>();
		String[] queries = inputQuery.replace("insert or ignore", "insert")
				.replace("insert or replace", "insert")
				.replace("\\(  \\)", "").replace("[\r\n]+", " ").split("union");
		for(String query : queries)
		{
			try
			{
				query = replaceStringAnalysisUnknownMark(query);
				if(failToParseQuery.contains(query))
					continue;
				Statement statement;
				if(queryToParsedQuery.containsKey(query))
					statement = queryToParsedQuery.get(query);
				else
					statement = (Statement) CCJSqlParserUtil.parse(query);
				//System.out.println(statement.getClass());
				if(statement instanceof Insert)
				{
					Insert in = (Insert) statement;
					//insert into table values (x, y , z)
					if(in.getItemsList() instanceof ExpressionList)
					{
						ExpressionList list = (ExpressionList) in.getItemsList();
						
						//TODO: handle complicated expressions
						for(Expression exp : list.getExpressions())
						{
							dataValues.add(exp.toString());
						}
					}
					//insert into table select * from table2
					else if(in.getSelect() != null)
					{
						List<String> values = getDataValues(in.getSelect().toString());
						if(values != null)
							dataValues.addAll(values);
					}
				}
				else if(statement instanceof Update)
				{
					Update up = (Update) statement;
					//TODO: handle complicated expressions
					//update table set col1 = exp1, col2 = exp2
					for(Expression exp : up.getExpressions())
					{
						dataValues.add(exp.toString());
					}
					//update table ... where xxx
					addToDataValues(up.getWhere(), dataValues);
				}
				else if(statement instanceof Delete)
				{
					Delete de = (Delete) statement;
					addToDataValues(de.getWhere(), dataValues);
				}
				else if(statement instanceof Select)
				{
					Select se = (Select) statement;
					PlainSelect pse = (PlainSelect) se.getSelectBody();
					Expression where = pse.getWhere();
					addToDataValues(where, dataValues);
				}
			}
			catch(Throwable e)
			{
				failToParseQuery.add(query);
			}
		}
		if(dataValues.isEmpty())
			return null;
		else
			return dataValues;
	}
	
	private static void addToDataValues(Expression expression, List<String> dataValues)
	{
		//TODO: test if parameterization is allowed in numeric operations such as addition. Assume allow right now
		
		if(expression instanceof BinaryExpression)
		{
			BinaryExpression exp = (BinaryExpression) expression;
			if(exp instanceof AndExpression || exp instanceof OrExpression)
			{
				addToDataValues(exp.getLeftExpression(), dataValues);
				addToDataValues(exp.getRightExpression(), dataValues);
			}
			else
			{
				//TODO: parse complicated cases such as subselect, between, etc
				//http://jsqlparser.sourceforge.net/docs/net/sf/jsqlparser/expression/Expression.html
				if(exp.getRightExpression() instanceof SubSelect)
				{
					
				}
				else
				{
					dataValues.add(exp.getRightExpression().toString());
				}
			}
		}
			
	}

	public static Set<String> getQueryTableForms(String query) {
		
		return null;
	}
	
}

