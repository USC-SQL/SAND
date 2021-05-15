package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Query {

	private SQLiteDatabase database;
	public void onStart()
	{
		onCreate();
	}
	public void onCreate()
	{
		insertRecord("abc");
		
		insert("peter");
	}
	
	public void insertRecord(String data)
	{
		insert(data);
	}
	
	public void insert(String data)
	{
		database.execSQL("INSERT ... " + data);
	}
	
	
	public void delete()
	{
		String value = getValue();
		database.execSQL("DELETE ..." + value);
	}
	
	public String getValue()
	{
		return "value";
	}
	
	public void getNameFromDB()
	{
		int id = 10;
		if(database.inTransaction())
			id = 14;
		getName(id);
	}
	public void getName(int id)
	{
		Cursor cursor = this.database.rawQuery("select name from testing where id = " + id, null);
		cursor.moveToFirst();
	}
	
	public void getDataFromDB()
	{
		getData(1);
	}
	
	public void getData(int id)
	{
		Cursor cursor = this.database.rawQuery("select data from testing where id = " + id, null);
		cursor.moveToFirst();
	}
	
	
	private static final String value = "ABC";
	private static final int index = 10;
	public void testStaticField()
	{
		Cursor cursor = this.database.rawQuery("select value from testing where id = " + value + index, null);
		cursor.moveToFirst();
	}
}
