package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class UnbatchedWritesAugmented {

	private SQLiteDatabase database;
	public void onClick()
	{
		for(int j = 0; j < 10; j++)
		{
			database.beginTransaction();
			for(int i=0;i<2;i++)
			{
				m2();
			}
			database.endTransaction();
		}
			
	}


	private void m2() {
		database.execSQL("UPDATE ...");
	}
}
