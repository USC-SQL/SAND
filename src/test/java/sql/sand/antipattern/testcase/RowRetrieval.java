package sql.sand.antipattern.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class RowRetrieval {
	SQLiteDatabase db;
	
	
	//match
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
		
			
			//String grade = c.getString(2);
			//System.out.println(grade);
	 
		} while (c.moveToNext());
	}
	
	
	//match
	public void testRowRetrievalMultipleFilters()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = c.getInt(0);
			String grade = c.getString(2);
			
			if(id < 10 && grade.equals("A"))
			{
				int nameIndex = c.getColumnIndex("name");
			 
				String name = c.getString(nameIndex);
				
				String query = "INSERT ... " + name + id;
			 
				db.execSQL(query);
			}
	 
		} while (c.moveToNext());
	}
	
	//match
	public void testRowRetrievalNestedFilters()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			String grade = c.getString(2);
			int id = c.getInt(0);
			if(id < 10)
			{
				if(grade.equals("A"))
				{
					int nameIndex = c.getColumnIndex("name");
				 
					String name = c.getString(nameIndex);
					
					String query = "INSERT ... " + name + id;
				 
					db.execSQL(query);
				}
			}
	 
		} while (c.moveToNext());
	}
	
	
	//not match
	public void testRowRetrievalUnDirectUse()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 

			int nameIndex = c.getColumnIndex("name");
		 
			String name = c.getString(nameIndex);
			
			SQLiteStatement s = db.compileStatement(name);
			if(s.equals("abc"))
			{
				String query = "INSERT ... " + name;
			 
				db.execSQL(query);
			}
			
	 
		} while (c.moveToNext());
	}
	
	
	
	//not match
	public void testRowRetrievalNotDependent()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = c.getInt(0);
			
			String grade = c.getString(2);
			System.out.println(grade);
			
			if(id < 10)
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
	
	//not match
	public void testRowRetrievalNoFilter()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{	 
			int id = c.getInt(0);
			
			
			int nameIndex = c.getColumnIndex("name");
		 
			String name = c.getString(nameIndex);
			
			String query = "INSERT ... " + name + id;
		 
			db.execSQL(query);
			
		
			
			//String grade = c.getString(2);
			//System.out.println(grade);
	 
		} while (c.moveToNext());
	}
	
	//not match
	public void testRowRetrievalSingleValueFunction()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT COUNT (*) FROM STUDENT", null);
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
		
			
			//String grade = c.getString(2);
			//System.out.println(grade);
	 
		} while (c.moveToNext());
	}
	
	/*
	//match
	public void testRowRetrievalDependentUse()
	{
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{
			String id_s = c.getString(0);
			int id = Integer.getInteger(id_s);
			if(id < 10)
			{
				int nameIndex = c.getColumnIndex("name");
			 
				String name = c.getString(nameIndex);
				
				String query = "INSERT ... " + name + id;
			 
				db.execSQL(query);
			}
		} while (c.moveToNext());
		//String grade = c.getString(2);
		//System.out.println(grade);

	}
	
	//not match
	public void testRowRetrievalNotDependentUse()
	{
		int sum = 0;
		db.execSQL("create table student(id text,name text,grade text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		c.moveToFirst();
		do
		{
			String id_s = c.getString(0);
			int id = Integer.getInteger(id_s);
			sum = sum + id;
			if(id < 10)
			{
				int nameIndex = c.getColumnIndex("name");
			 
				String name = c.getString(nameIndex);
				
				String query = "INSERT ... " + name + id;
			 
				db.execSQL(query);
			}
		} while (c.moveToNext());
	}
	*/
	
}
