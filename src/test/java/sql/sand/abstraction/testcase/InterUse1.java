package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InterUse1 {
	
	SQLiteDatabase db;
	public String getName()
	{
		Cursor cursor = db.rawQuery("SELECT name FROM data", null);
		String name = cursor.getString(0);
		return name;
	}
	public void testInterUse()
	{
		String name = getName();
		usePassToCallee(name);
	}
	
	public void usePassToCallee(String name)
	{
		db.execSQL("DELECT" + name);
	}
	

	
}
