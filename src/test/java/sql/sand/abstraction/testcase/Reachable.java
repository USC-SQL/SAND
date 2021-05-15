package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Reachable {

	SQLiteDatabase db;
	
	public void testReachable()
	{
		while(db.isReadOnly())
		{
			db.rawQuery("SELECT name FROM data", null);
			
			db.isOpen();
			
			db.execSQL("SELECT ...");
		}
		
		db.execSQL("SELECT ...");
	}
	
	
	public void testInterReachable()
	{
		a();
		
		while(db.isDatabaseIntegrityOk())
		{
			if(db.isDatabaseIntegrityOk())
				b();
			
			db.execSQL("INSERT");
		}
	}
	
	public void a()
	{
		db.execSQL("INSERT a ...");
	}
	
	public void b()
	{
		db.execSQL("INSERT b ...");
	}
}
