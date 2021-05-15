package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Projection {
	private SQLiteDatabase db;
	
	public void testNotMergingProjection()
	{	

		if(db.isOpen())
			db.rawQuery("select name, grade from table", null);
		db.rawQuery("select id from table", null);
	}
}
