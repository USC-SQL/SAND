package sql.sand.antipattern.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RowRetrievalIfElse {
	SQLiteDatabase db;
	
	
	//not match
	public void testRowRetrieval()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = c.getInt(0);
			
			if(id < 10)
			{
				int nameIndex = c.getColumnIndex("name");
			 
				String name = c.getString(nameIndex);
				
				String query = "INSERT ... " + name + id;
			 
				db.execSQL(query);
			}
			else
			{
				String name = c.getString(1);
				
				String query = "INSERT ... " + name + id;
			 
				db.execSQL(query);
				
			}
			
			//String grade = c.getString(2);
			//System.out.println(grade);
	 
		} while (c.moveToNext());
	}
}
