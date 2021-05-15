package sql.sand.abstraction.testcase;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class APIWrite {
	
	private SQLiteDatabase database;
	
	public void insertConstant()
	{
		ContentValues contentValues = new ContentValues();

        contentValues.put("name", "yj");
        contentValues.put("surname", "l");
        contentValues.put("marks", "A");
        database.insert("student", null ,contentValues);
	}
	
	public void insertVariable(String para)
	{
		ContentValues contentValues = new ContentValues();

        contentValues.put("name", "yj");
        contentValues.put("surname", "l");
        contentValues.put(para, "A");
        database.insert("student", null ,contentValues);
	}
	
	public void insertNonString(Integer input)
	{
		ContentValues contentValues = new ContentValues();

		int i = 1;
        contentValues.put("name", "yj");
        contentValues.put("surname", "l");
        contentValues.put("score", i);
        contentValues.put("we", input);
        database.insert("student", null ,contentValues);
	}
	
	public void updateConstant()
	{
		ContentValues contentValues = new ContentValues();

        contentValues.put("name", "yj");
        contentValues.put("surname", "l");
        contentValues.put("marks", "A");
        
        String whereClause;
        if(contentValues.size() > 0)
        	whereClause = "column1 = ? OR column1 = ?";
        else
        	whereClause = "column1 = ? AND column1 = ?";
		
		String[] whereArgs = new String[] {
			    "value1",
			    "value2"
		};
		
		database.update("grade", contentValues, whereClause, whereArgs);
	}
	
	public void updateNoWhere(String column)
	{
		ContentValues contentValues = new ContentValues();

        contentValues.put("name", "yj");
        contentValues.put("surname", "l");
        contentValues.put(column, "A");
		
        String table;
        if(database.isDatabaseIntegrityOk())
        	table = "grade";
        else
        	table = "GG";
		database.update(table, contentValues, null, null);
	}
	
	public void delete()
	{
		database.delete("all", "A = 'abc'", null);
	}
	
	public void deleteNoWhere()
	{
		database.delete("all", null, null);
	}
	
	public void transaction()
	{
		database.beginTransaction();
		database.endTransaction();
		
		while (database.isDatabaseIntegrityOk()) {
			database.beginTransaction();
			
		}
	}
}
