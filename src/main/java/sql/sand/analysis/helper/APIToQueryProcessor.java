package sql.sand.analysis.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import usc.sql.ir.InterRe;

public class APIToQueryProcessor {

	
	public static Set<String> convertAPIToQuery(List<Parameter> parameters)
	{
		Set<String> stringValues = new HashSet<>();
		String sig = parameters.get(0).methodSig;
		//sort the parameters by index
		Comparator<Parameter> byIndex = (Parameter p1, Parameter p2)->(p1.index - p2.index);
		Collections.sort(parameters, byIndex);
		if(sig.contains("query"))
		{
			/*
			 query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String)>
			 query(java.lang.String,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,java.lang.String,java.lang.String,java.lang.String)
			 database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
			 */
			buildQuery(parameters, stringValues);
		}
		else if(sig.contains("insert"))
			buildInsert(parameters, stringValues);
		else if(sig.contains("update"))
			buildUpdate(parameters, stringValues);
		else if(sig.contains("delete"))
			buildDelete(parameters, stringValues);
		return stringValues;
	}
	
	private static void buildDelete(List<Parameter> parameters, Set<String> stringValues)
	{
		Set<String> tables = null;
		Set<String> where = null;
		for(Parameter para : parameters)
		{
			if(para.index == 0)
				tables = para.values;
			else if(para.index == 1)
				where = para.values;
			else 
				//Ignore selectionArgs 
				;
		}
		stringValues.addAll(buildDeleteStatement(tables, where));
	}
	
	private static Set<String> buildDeleteStatement(Set<String> tables, Set<String> where)
	{
		Set<String> statements = new HashSet<>();
		if(isEmptyOrNull(tables))
        	return statements;
		
		for(String table : tables)
        {
            StringBuilder sql = new StringBuilder(120);

            sql.append("DELETE FROM ");
            sql.append(table);
            if(isEmptyOrNull(where))
            {
            	statements.add(sql.toString());
            }
            else
            {
            	for(String whereClause : where)
            	{
            		StringBuilder sqlSta = new StringBuilder(sql);
            		sqlSta.append(" WHERE ");
                    sqlSta.append(whereClause);
                    statements.add(sqlSta.toString());
            	}
               
            }

        }
		
		return statements;
	}
	
	private static void buildUpdate(List<Parameter> parameters, Set<String> stringValues)
	{
		Set<String> tables = null;
		Set<String> contentValues = null;
		Set<String> where = null;
		for(Parameter para : parameters)
		{
			if(para.index == 0)
				tables = para.values;
			else if(para.index == 1)
				contentValues = para.values;
			else if(para.index == 2)
				where = para.values;
			else 
				//Ignore selectionArgs 
				;
		}
		stringValues.addAll(buildUpdateStatement(tables, contentValues, where));
	}
	private static Set<String> buildUpdateStatement(Set<String> tables, Set<String> contentValues, Set<String> where)
	{
		Set<String> statements = new HashSet<>();
		if(isEmptyOrNull(tables) || isEmptyOrNull(contentValues))
        	return statements;
		
		for(String table : tables)
        {
            StringBuilder sql = new StringBuilder(120);
            sql.append("UPDATE ");
            sql.append(table);
            sql.append(" SET ");

            Map<String, String> values = new HashMap<>();
	        //TODO: handle conditional values of content values
	        for(String contentValue : contentValues)
	        {
	        	values.put(contentValue.split(InterRe.CONTENT_VALUE_OPERATOR)[0], 
	        			contentValue.split(InterRe.CONTENT_VALUE_OPERATOR).length==1? "Unknown@DYNAMIC_VAR":contentValue.split(InterRe.CONTENT_VALUE_OPERATOR)[1]);
	        }
	        
            // move all bind args to one array
            //int setValuesSize = values.size();
            //int bindArgsSize = (whereArgs == null) ? setValuesSize : (setValuesSize + whereArgs.length);
            //Object[] bindArgs = new Object[bindArgsSize];
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName);
                i++;
                //bindArgs[i++] = values.get(colName);
                sql.append("=?");
            }
            //if (whereArgs != null) {
            //    for (i = setValuesSize; i < bindArgsSize; i++) {
            //        bindArgs[i] = whereArgs[i - setValuesSize];
            //    }
            //}
            if(isEmptyOrNull(where))
            {
            	statements.add(sql.toString());
            }
            else
            {
            	for(String whereClause : where)
            	{
            		StringBuilder sqlSta = new StringBuilder(sql);
            		sqlSta.append(" WHERE ");
                    sqlSta.append(whereClause);
                    statements.add(sqlSta.toString());
            	}
               
            }

        }
		
		return statements;
	}
	private static void buildInsert(List<Parameter> parameters, Set<String> stringValues) {
		Set<String> tables = null;
		Set<String> contentValues = null;
		for(Parameter para : parameters)
		{
			if(para.index == 0)
				tables = para.values;
			else if(para.index == 2)
				contentValues = para.values;
		}
		stringValues.addAll(buildInsertStatement(tables, contentValues));
		
	}

	private static Set<String> buildInsertStatement(Set<String> tables, Set<String> contentValues)
	{
		Set<String> statements = new HashSet<>();
	    if(isEmptyOrNull(tables) || isEmptyOrNull(contentValues))
	        	return statements;
        for(String table : tables)
        {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            //sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
	        sql.append(table);
	        sql.append('(');
	
	        Map<String, String> initialValues = new HashMap<>();
	        //TODO: handle conditional values of content values
	        for(String contentValue : contentValues)
	        {
	        	
	        	initialValues.put(contentValue.split(InterRe.CONTENT_VALUE_OPERATOR)[0], 
	        			contentValue.split(InterRe.CONTENT_VALUE_OPERATOR).length==1 ? "Unknown@DYNAMIC_VAR":contentValue.split(InterRe.CONTENT_VALUE_OPERATOR)[1]);
	        }
	        
	        //Object[] bindArgs = null;
	        int size = (initialValues != null && !initialValues.isEmpty())
	                ? initialValues.size() : 0;
	        if (size > 0) {
	            //bindArgs = new Object[size];
	            int i = 0;
	            for (String colName : initialValues.keySet()) {
	                sql.append((i > 0) ? "," : "");
	                sql.append(colName);
	                i++;
	                //bindArgs[i++] = initialValues.get(colName);
	            }
	            sql.append(')');
	            sql.append(" VALUES (");
	            for (i = 0; i < size; i++) {
	                sql.append((i > 0) ? ",?" : "?");
	            }
	        } 
	        else {
	            //sql.append(nullColumnHack + ") VALUES (NULL");
	        	return statements;
	        }
	        sql.append(')');
	        statements.add(sql.toString());
        }
        return statements;
	}
	private static void buildQuery(List<Parameter> parameters, Set<String> stringValues) {
		boolean distinct = false;
		Set<String> tables = null;
		String[] columns = null;
		Set<String> where = null;
		Set<String> groupBy = null;
		Set<String> having = null;
		Set<String> orderBy = null;
		Set<String> limit = null;
		for(Parameter para : parameters)
		{
			switch (para.index) {
			case 0:
				tables = para.values;
		    case 1: 
		    {
		    	//TODO: handle conditional values of array elements
		    	columns = new String[para.values.size()];
		    	int i = 0;
		    	for(String column : para.values)
		    	{
		    		columns[i] = column;
		    		i++;
		    	}
		        break; 
		    }

		    case 2: 
		        where = para.values; 
		        break; 
		    case 3: 
		        //Ignore selectionArgs 
		        break; 
		    case 4: 
		        groupBy = para.values; 
		        break; 
		    case 5: 
		        having = para.values; 
		        break; 
		    case 6: 
		        orderBy = para.values; 
		        break; 
		    case 7: 
		        limit = para.values; 
		        break; 
		    } 
		}
		
		//TODO: return a combination of possible values
		Set<String> query = buildQueryString(distinct, tables,
				columns, where, groupBy,
				having, orderBy,
				limit);
		stringValues.addAll(query);
	}

	private static Set<String> buildQueryString(
            boolean distinct, Set<String> tables, String[] columns, Set<String> where,
            Set<String> groupBy, Set<String> having, Set<String> orderBy, Set<String> limit) {
		
		Set<String> queries = new HashSet<>();
		
        if (isEmptyOrNull(groupBy) && !isEmptyOrNull(having)) {
            System.err.println(
                    "HAVING clauses are only permitted when using a groupBy clause");
            return queries;
        }
       

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }
        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        
        Queue<String> queue = new LinkedList<>();
        queue.add(query.toString());
        
        //table
        if(isEmptyOrNull(tables))
        	return queries;
        else 
        {
        	String oldQuery = queue.poll();
        	for(String table : tables)
        	{
        		String newQuery = new String(oldQuery);
        		newQuery += table;
        		queue.add(newQuery);
        	}
        }
        appendClause(queue, " WHERE ", where);
        appendClause(queue, " GROUP BY ", groupBy);
        appendClause(queue, " HAVING ", having);
        appendClause(queue, " ORDER BY ", orderBy);
        appendClause(queue, " LIMIT ", limit);

        while(!queue.isEmpty())
        	queries.add(queue.poll());
        return queries;
	}
	private static void appendClause(Queue<String> queue, String name, Set<String> clauses)
	{
		if(!isEmptyOrNull(clauses))
		{
			List<String> oldQueries = new ArrayList<>();
			while(!queue.isEmpty())
				oldQueries.add(queue.poll());
			for(String clause : clauses)
			{
				for(String oldQuery : oldQueries)
				{
					queue.add(appendClause(oldQuery, name, clause));
				}
			}
		}
	}
    private static String appendClause(String s, String name, String clause) {
        if (!isEmpty(clause)) 
        	return s + name + clause;
        else
        	return s;
    }

    /**
     * Add the names that are non-null in columns to s, separating
     * them with commas.
     */
    private static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;

        for (int i = 0; i < n; i++) {
            String column = columns[i];

            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }
    
	private static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0 || str.equals("null");
	}
	
	private static boolean isEmptyOrNull(Set<String> set)
	{
		return (set == null || set.isEmpty());
	}
	
}
