package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class UnbalancedTransaction {

	private SQLiteDatabase db;
	
	public void onUpgrade()
	{
		for(int i = 0; i < 10; i++)
		{
			
			o();
			
			s();
		}
	}
	public void s()
	{
		db.execSQL("INSERT ...");
	}
	
	public void o()
	{
		if(db.enableWriteAheadLogging())
		db.beginTransaction();
		
		try
		{
			db.setTransactionSuccessful();
			
			db.endTransaction();
		}
		catch(Throwable th)
		{
			db.endTransaction();
		}
	}
	
}
