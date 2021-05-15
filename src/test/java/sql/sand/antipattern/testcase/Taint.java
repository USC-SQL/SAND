package sql.sand.antipattern.testcase;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class Taint {

	private SQLiteDatabase db;
	private Intent intent;
	public void testTaintedQuery()
	{
		String id = intent.getDataString();
		
		String a = id;
		
		String b = a + "name";
		
		
		db.execSQL("insert" + b);
	} 
	
	
	public void testInterTaintQuery()
	{
		String id = intent.getStringExtra("abc");
		taint(id);
	}
	
	public void taint(String id)
	{
		db.execSQL("delete" + id);
	}
}
