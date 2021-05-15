package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class UnbatchedWrites {

	private SQLiteDatabase database;
	public void onClick()
	{
		//while(database.isDatabaseIntegrityOk())
		
			database.beginTransaction();
			for(int i=0;i<2;i++)
			{
				m2();
			}
			database.endTransaction();
			
			database.beginTransaction();
			database.endTransaction();
			for(int i=0;i<2;i++)
			{
				m2();
			}
			//database.endTransaction();
		
	}


	private void m2() {
		database.execSQL("UPDATE ...");
	}
}
