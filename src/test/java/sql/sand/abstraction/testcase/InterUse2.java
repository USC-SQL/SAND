package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InterUse2 {

	SQLiteDatabase db;
	public void readItem()
	{
		Cursor cursor = getItemCursor();
		String id = cursor.getString(0);
		String name = cursor.getString(1);
		System.out.println(id + name);
	}
	
	public Cursor getItemCursor()
	{
		Cursor cursor = db.rawQuery("SELECT id, name FROM data", null);
		return cursor;
	}
}
