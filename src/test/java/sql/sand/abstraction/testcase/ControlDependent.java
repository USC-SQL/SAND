package sql.sand.abstraction.testcase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ControlDependent {

	SQLiteDatabase db;
	public void testNestedControlDependent()
	{
		Cursor c = db.rawQuery("SELECT * FROM STUDENT", null);
		do
		{	 
			int id = Integer.getInteger(c.getString(0));
			
			String grade = c.getString(2);
			System.out.println(grade);
			if(id > 2)
			{
				if(id < 10)
				{
					int nameIndex = c.getColumnIndex("name");
				 
					String name = c.getString(nameIndex);
					
					String query = "INSERT ... " + name + id;
				 
					db.execSQL(query);
					//break;
				}
			}
		} while (c.moveToNext());
	}
	
	public void testInterproceduralControlDependent()
	{
		if(db.isOpen())
		{
			update();
		}
	}
	
	private void update()
	{
		Cursor c = db.rawQuery("ABC..", null);
		while(c != null)
		{
			System.out.println("abc");
			if (db.isOpen())
			{
				db.execSQL("UPDATE ...");
			}
		}
	}
}
