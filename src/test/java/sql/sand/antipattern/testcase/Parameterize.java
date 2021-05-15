package sql.sand.antipattern.testcase;

import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

public class Parameterize {
	
	private SQLiteDatabase db;
	private Set<String> ids;
	public void testUnparameterizedWrite()
	{
		for(String id : ids)
		{
			db.execSQL("insert into table (id, name) values (" + id + "," + "'abc'" + ")");
			db.execSQL("insert into table (id, name) values (?, ?)");
		}
		
	}
	
	public void testUnparameterizeRead()
	{
		for(String id : ids)
			db.rawQuery("select id from table where id > " + id, null);
		
		db.rawQuery("select id, name from table where id < ? and id > ?", null);
	}
}
