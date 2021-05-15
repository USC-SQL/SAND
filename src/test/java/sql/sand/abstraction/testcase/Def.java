package sql.sand.abstraction.testcase;

import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Def {

	SQLiteDatabase db;
	Intent intent;
	List<String> dates;
	String column;
	
	public void testDef(String tableName)
	{
		String input = intent.getDataString();
		
		String date = dates.get(0);
		
		String sql = "SELECT " + column + " from " + tableName + " where _id = " + input + " order by " + date;
		
		Cursor cursor = db.rawQuery(sql, null);
		
		cursor.getCount();
		
	}
}
