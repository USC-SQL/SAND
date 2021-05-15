package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class CallChain {
	
	SQLiteDatabase db;
	
	
	public void testCallChain1()
	{

		delete();
		if(db.isDatabaseIntegrityOk())
			delete();
		
	}
	
	
	public void delete()
	{
		db.execSQL("DELETE ...");
	}
	
	
	public void testCallChain2()
	{
		testCallChain1();
	}
	
	public void testCallChain3()
	{
		testCallChain1();
	}

}
