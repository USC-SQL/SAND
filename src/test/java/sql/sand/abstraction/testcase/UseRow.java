package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UseRow {
	
	SQLiteDatabase db;

	public void testUseRowAnnotation()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = Integer.getInteger(c.getString(0));
			
			String grade = c.getString(2);
			System.out.println(grade);
			if(id > 2)
			{
				if(id < 10)
				{
					int nameIndex = c.getColumnIndex("name");
				 
					String name = c.getString(nameIndex);
					
					String query = "INSERT ... " + name + id;
				 
					db.execSQL(query);
					break;
				}
			}
		} while (c.moveToNext());
	}
	
	
	public void testUseRowAnnotation2()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = Integer.getInteger(c.getString(0));
			
			String grade = c.getString(2);
		
			if(id > 2 && grade.equals("A"))
			{
					int nameIndex = c.getColumnIndex("name");
				 
					String name = c.getString(nameIndex);
					
					String query = "INSERT ... " + name + id;
				 
					db.execSQL(query);
			}
			
			//String grade = c.getString(2);
			//System.out.println(grade);
	 
		} while (c.moveToNext());
	}
	
}
