package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Selection {
	private SQLiteDatabase db;
	
	public void testNotMergingSelection()
	{	
		db.rawQuery("select id from table where grade > 90", null);
		while(db.isOpen())
			db.rawQuery("select id from table where grade < 10", null);
		
		if(db.isReadOnly())
			db.rawQuery("select id from table where grade between 10 and 20", null);
	}
}
