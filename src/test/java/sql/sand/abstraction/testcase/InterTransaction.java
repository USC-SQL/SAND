package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class InterTransaction {

	private SQLiteDatabase db;
	
	public void BTBT()
	{
		if(db.isDatabaseIntegrityOk())
			a();
		if(db.isDbLockedByCurrentThread())
			b();
		db.execSQL("INSERT");
	}
	
	public void a()
	{
		db.beginTransaction();
		
		db.execSQL("abc");
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void b()
	{
		db.beginTransaction();
		
		db.execSQL("abc");
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
