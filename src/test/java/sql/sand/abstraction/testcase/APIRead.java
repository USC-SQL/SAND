package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class APIRead {

	private SQLiteDatabase database;
	
	public void query1()
	{
		String[] columns = new String[] {
			    "column1",
			    "column2"
			};
		String whereClause = "column1 = ? OR column1 = ?";
		
		String[] whereArgs = new String[] {
			    "value1",
			    "value2"
		};
		
		
		String orderBy = "column1";
			
		database.query("student", columns, whereClause, whereArgs, "column3", "column1 = 1", orderBy);
	}
	
	public void query2()
	{
		String value = "abc";
		database.query("class", null, "column1 = " + value, null, null, null, null, "20");
	}
	
	public void query3()
	{
		String whereClause;
		if(database.isDatabaseIntegrityOk())
			whereClause = "column1 = ?";
		else
			whereClause = "column2 = ?";
		
		String[] columns = new String[2];
		columns[1] = "column2";
		columns[0] = "column1";
		
		String orderBy;
		if(database.isWriteAheadLoggingEnabled())
			orderBy = "column1";
		else
			orderBy = "column2";
		database.query("record", columns, whereClause, null, null, null, orderBy);
			
	}
	
	private static final String[] a = new String[]{"c1", "c2", "c3"};
	public void query4(Long l)
	{
		while(database.isDatabaseIntegrityOk())
		{
			String l2 = l.toString();
			database.query("record", a, "_id=?", new String[]{l2}, null, null, null, "1");
			String l3 = l.toString();
			database.delete("record", "_id=?", new String[]{l3});
		}
	}
	
	public void query5()
	{
		getList(-1);
	}
	private void getList(long id)
	{
		String selection = null;
		if(id >= 0)
			selection = "categoryId = " + id;
		database.query("table", null, selection, null, null, null, null, null);
			
	}
}
