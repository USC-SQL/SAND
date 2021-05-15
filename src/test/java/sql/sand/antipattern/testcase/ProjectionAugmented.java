package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class ProjectionAugmented {
	private SQLiteDatabase db;
	
	
	public void testNotMergingProjectionAugmented(String para)
	{	
		if(db.isOpen())
			db.rawQuery("select name, grade from table", null);
		db.rawQuery("select id from table", null);
		
		if(db.isOpen())
			db.rawQuery("select name, grade from "+ para, null);
		db.rawQuery("select id from table" + para, null);
	}
}
