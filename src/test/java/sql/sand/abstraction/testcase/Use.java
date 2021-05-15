package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Use {

	SQLiteDatabase db;
	public void testUse()
	{
		db.execSQL("create table data(id text,name text,json text, primary key(name, id));");
		Cursor c = db.rawQuery("SELECT * FROM DATA", null);
		c.moveToFirst();
		do
		{
		 
			int index = c.getColumnIndex("user");
		 
			String value = c.getString(index);
			
			String id = c.getString(0);
			
			String query = "INSERT ... " + value + id;
		 
			db.execSQL(query);
	 
		} while (c.moveToNext());
	}
	

}
