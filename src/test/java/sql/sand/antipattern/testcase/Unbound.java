package sql.sand.antipattern.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

public class Unbound {
	SQLiteDatabase db;
	TextView tv;
	

	public void testUnbounded()
	{
		db.execSQL("create table student(id text, name text, grade text, primary key(name, id));");
		Cursor c = db.rawQuery("select * from student where id > 10", null);
		c.moveToFirst();
		do
		{
			String a = "a" + tv.getPrivateImeOptions();
			System.out.println(a);
			String name = c.getString(1);
			tv.setText(name);
		} while (c.moveToNext());
	}
	
	public void testUnboundedNoLoop()
	{
		db.execSQL("create table student(id text, name text, grade text, primary key(name, id));");
		Cursor c = db.rawQuery("select * from student", null);
		c.moveToFirst();
	}
	
	
	public void testUnboundedLimit()
	{
		db.execSQL("create table student(id text, name text, grade text, primary key(name, id));");
		Cursor c = db.rawQuery("select * from student where id > 10 limit 10", null);
		c.moveToFirst();
		do
		{	 
		 
			String name = c.getString(1);
			tv.setText(name);
			
		} while (c.moveToNext());
	}
	
	public void testUnboundedDataContent()
	{
		db.execSQL("create table student(id text, name text, grade text, primary key(name, id));");
		Cursor c = db.rawQuery("select * from student where name = 'limit' or name = 'someone'", null);
		c.moveToFirst();
		do
		{	 
		 
			String name = c.getString(1);
			tv.setText(name);
			
		} while (c.moveToNext());
	}
}
