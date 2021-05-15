package sql.sand.antipattern.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LoopToJoinAugmented {
	private SQLiteDatabase db;
	
	public void testLoopToJoin()
	{
		Cursor c = db.rawQuery("select id from students", null);
		c.moveToFirst();
		do
		{
			int id = c.getInt(0);
			
			Cursor c2 = db.rawQuery("select comment from posts where id = " + id, null);
			c2.moveToFirst();
			
		} while(c.moveToNext());
	}
	
	public void testNotLoopToJoin()
	{
		do
		{
			Cursor c = db.rawQuery("select id from students", null);
			c.moveToFirst();

			int id = c.getInt(0);
			
			Cursor c2 = db.rawQuery("select comment from posts where id = " + id, null);
			c2.moveToFirst();
			
		} while(db.isOpen());
	}
	
	public void testNotLoopToJoin1()
	{
		Cursor c = db.rawQuery("select id from students", null);
		while(c.moveToNext())
		{
			Cursor c2 = db.rawQuery("select comment from posts", null);
			c2.getString(0);
		} 
	}
}
