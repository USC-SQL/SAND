package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Report {

	SQLiteDatabase db;
	public void testSequence()
	{

		if(db.isDatabaseIntegrityOk())
		{
			db.rawQuery("SELECT * FROM table WHERE id = 10", null);
			db.rawQuery("SELECT * FROM table WHERE id = 12", null);
		}
		else
			db.rawQuery("SELECT * FROM table WHERE id = 14", null);
		db.rawQuery("SELECT * FROM table WHERE id = 15", null);
		db.rawQuery("SELECT * FROM table WHERE id = 16", null);
		
		
		if(db.isDbLockedByCurrentThread())
			db.rawQuery("SELECT * FROM table WHERE id = 19", null);
		else if(db.isOpen())
			db.rawQuery("SELECT * FROM table WHERE id = 21", null);
		else
			db.rawQuery("SELECT * FROM table WHERE id = 23", null);
		
	}

	public void testSequenceLoop()
	{
		if(db.isDatabaseIntegrityOk())
		{
			db.rawQuery("SELECT a FROM table", null);
			while(db.isDatabaseIntegrityOk())
			{
				db.rawQuery("SELECT b From table", null);
				db.rawQuery("SELECT c From table", null);
				db.rawQuery("SELECT d From table", null);
			}
		}
		else
		{
			while(db.isDatabaseIntegrityOk())
			{
				db.rawQuery("SELECT x From table", null);
				db.rawQuery("SELECT y From table", null);
				db.rawQuery("SELECT z From table", null);
			}
		}
	}
}
