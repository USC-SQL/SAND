package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Implicit {
	private SQLiteDatabase db;
	
	public void testImplicitRead()
	{
		db.rawQuery("select * from table where id = 1", null);
		
		db.rawQuery("select count(*) from table where id = 2", null);
		
	}
	
	public void testImplictInsert()
	{
		db.execSQL("insert into table values (1, 'bruce')");
		
		db.execSQL("insert into table (id, name) values (1, 'bruce')");
	}
}
