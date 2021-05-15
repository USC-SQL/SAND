package sql.sand.abstraction.testcase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Loop {
	
	public void testNestedLoop(SQLiteDatabase db)
	{
		while(db.isOpen())
		{
			for(int i = 0; i < 10; i++)
			{
				db.execSQL("INSERT INTO users (_id, column2) VALUES ('1', 'value2');");
			}
		}
	}
	
	public void testInterproceduralLoop(SQLiteDatabase db)
	{
		while(db.isOpen())
		{
			insert(db);
		}
	}
	
	private void insert(SQLiteDatabase db)
	{
		if(db != null)
		{
			while(db.isOpen())
			{
				ContentValues cv = new ContentValues();
				cv.put("a", "bc");
				db.insert("table_name", null , cv);
			}
		}
	}


	public void testDoWhileLoop(SQLiteDatabase db)
	{
		Cursor c = db.rawQuery("select * from student where id > 10", null);
		c.moveToFirst();
		do
		{
			String a = "a" + db.getPath();
			db.execSQL("UPDATE " + a);
			String name = c.getString(1);
			System.out.println(name);
		} while (c.moveToNext());
	}
}
