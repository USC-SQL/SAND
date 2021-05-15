package sql.sand.antipattern.testcase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IgnoreReturn {

	private SQLiteDatabase db;
	
	public void testIgnoreReturn()
	{
		
		Cursor c = db.rawQuery("Select ...", null);
		
		int i = c.getCount();
		System.out.println(i);
		
		
		
		long error = db.insert("table", "", new ContentValues());
		
		if(error == -1)
		{
			System.out.println();
		}
		
		
		db.update("table", new ContentValues(), null, null);
		
	}
}
