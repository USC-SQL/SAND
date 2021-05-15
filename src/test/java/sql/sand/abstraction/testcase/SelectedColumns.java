package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class SelectedColumns {

	SQLiteDatabase db;
	
	public void testCreatTable()
	{
		db.execSQL("create table if not exists all_package_names(id text,pkg_name varchar primary key,time_limit varchar,total_time varchar,notification varchar)");
		
		db.execSQL("create table data(id text,name text,json text, primary key(name, id));");
	}
	
	public void testSelectedColumns()
	{
		db.rawQuery("select * from data", null);
		
		db.rawQuery("select id from data where id > 0", null);
		
		db.rawQuery("SELECT a.pkg_name,b.name FROM all_package_names a LEFT JOIN data b ON a.pkg_name=b.name", null);
		
		db.rawQuery("SELECT a.* FROM data a", null);

		db.rawQuery("SELECT AVG(id) AS avg FROM data WHERE id > 10", null);
	
	}
	
}
