package sql.sand.antipattern.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ColumnRetrieval {
	SQLiteDatabase db;
	
	//the grade column is not used
	public void testColumnRetrieval()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			String id = c.getString(0);
			
			int nameIndex = c.getColumnIndex("name");
		 
			String name = c.getString(nameIndex);
			
			String query = "INSERT ... " + name + id;
		 
			db.execSQL(query);
	 
		} while (c.moveToNext());
	}
	
	public void testSafeEstimationWhenUnknownInt(int i)
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);

		String id = c.getString(i);

		String query = "INSERT ... " + id;
	 
		db.execSQL(query);

	}
	
	public void testSafeEstimationWhenUnknownString(String column)
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);

		
		int nameIndex = c.getColumnIndex(column);
	 
		String name = c.getString(nameIndex);
		
		String query = "INSERT ... " + name;
	 
		db.execSQL(query);

	}
	
	public void testMultipleCursor(Cursor other)
	{
		Cursor c = db.rawQuery("select id, name from student", null);
		db.execSQL(other.getString(0) +  other.getString(1));
		
	}
	
	public void testRetrieveColumnsOneByOne(String id)
	{
		Cursor c = db.rawQuery("select username, text, date, systemuser from issue_comments where issue_id = ?", new String[]{id});
		c.moveToFirst();
		while(!c.isAfterLast())
		{
			new Test(c.getString(0),c.getString(1), c.getString(2).equals("a"), c.getInt(3));
		}
		
	}
	
	public void testColumnRetrievalLimit0()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT limit 0", null);
		c.moveToFirst();
		do
		{	 
			String id = c.getString(0);
			
			int nameIndex = c.getColumnIndex("name");
		 
			String name = c.getString(nameIndex);
			
			String query = "INSERT ... " + name + id;
		 
			db.execSQL(query);
	 
		} while (c.moveToNext());
	}
	
	public void testInterColumnRetrieval()
	{
		db.execSQL("create table student(id text,name text,grade text);");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		if(c != null)
		{
			while(!c.isAfterLast())
			{
				try
				{
					get(c);
					c.moveToNext();
				}
				catch(Exception e3)
				{
					
				}
			}
		}
	}
	
	public void get(Cursor cursor)
	{
		System.out.println(cursor.getString(0));
		System.out.println(cursor.getString(1));
		System.out.println(cursor.getString(2));

	}
	
	
	public void testInterColumnRetrieval2()
	{
		//db.execSQL("create table teacher(idddd text);");
		Cursor c = query();
		c.moveToFirst();
		String id = c.getString(0);
		System.out.println(id);
	}
	String field;
	public Cursor query()
	{
		return db.rawQuery("SELECT group_concat("+ field +") FROM teacher", null);

	}
}

class Test
{
	public Test(String a, String b, boolean c, int d)
	{
		
	}
}

