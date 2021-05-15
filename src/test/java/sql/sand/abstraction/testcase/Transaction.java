package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Transaction {

	private SQLiteDatabase db;
	
	public void BTBT()
	{
		BT();
	}
	public void BT()
	{
		db.beginTransaction();
	}
	
	public void ET()
	{
		db.setTransactionSuccessful();
		db.endTransaction();
		
	}
	
	public void testTransaction()
	{
		
		BTBT();
		db.execSQL("UPDATE table_name SET column1 = 'a' WHERE _id = 1");
		ET();
	}
	
	public void testNestedTransaction()
	{
		while(db.isOpen())
			db.beginTransaction();
		
		db.beginTransaction();
		
		db.endTransaction();
		
		db.beginTransaction();
		
		db.execSQL("INSERT ...");
		
		db.endTransaction();
		
		while(db.isOpen())
			db.endTransaction();
	}
}

