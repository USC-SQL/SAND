package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Dominator {

	SQLiteDatabase db;
	
	public void testDominator()
	{
		if(db.isReadOnly())
		{
			db.rawQuery("SELECT name FROM data", null);
			
			db.isOpen();
			
			db.execSQL("INSERT ...");
		}
		
		db.execSQL("DELETE ...");
	}
	
	public void testInterDominator()
	{
		a();
		c();
		db.execSQL("DELETE ...");
	}
	
	public void a()
	{
		if(db.isReadOnly())
			db.execSQL("REPLACE ...");
	}
	public void c()
	{
		b();
	}
	public void b()
	{
		db.execSQL("UPDATE ...");
	}
}
