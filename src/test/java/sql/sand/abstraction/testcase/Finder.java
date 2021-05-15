package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Finder {
	SQLiteDatabase db;
	public void testSilicaFinder()
	{
		
		db.execSQL("INSERT INTO table_name (column1, column2, column3) VALUES (value1, value2, value3);");
		
		while(db.isOpen())
			db.delete("table_name", null, null);
		
		db.rawQuery("SELECT * FROM table_name WHERE _id > 0", null);
		
		if(db.isDatabaseIntegrityOk())
			db.rawQuery("SELECT column1 FROM table_name LIKE '%abc%'", null);
	}

}
