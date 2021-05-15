package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;

public class Cache {
	private SQLiteDatabase db;
	
	public void testNotCachingSequence()
	{	

		if(db.isOpen())
			db.rawQuery("select name, grade from table", null);
		
		String query;
		if(db.isDatabaseIntegrityOk())
			query = "select name, grade from table";
		else
			query = "select name, grade from table";
		db.rawQuery(query, null);
	}
	/*
	public void testNotCachingLoop()
	{
		while(db.isOpen())
			db.rawQuery("select name, grade from table", null);
	}
	*/
	
	public void testNotCachingInvalidCase1()
	{
		db.rawQuery("select name, grade from table where name = ?", null);
		db.rawQuery("select name, grade from table where name = ?", null);
		
		while(db.isOpen())
			db.rawQuery("select name, grade from table where name = ?", null);
	}
	
	public void testNotCachingInvalidCase2()
	{
		db.rawQuery("select name, grade from table limit 0", null);
		db.rawQuery("select name, grade from table limit 0", null);
	}
	
	public void testNotCachingInvalidCase3()
	{
		db.rawQuery("select name, grade from table where name = unknown@dynamic_var@unknown@<com.space150.bww.fightforfandom.b.b: com.space150.bww.fightforfandom.a.a b(com.space150.bww.fightforfandom.a.a)>@94@33!!!", null);
		db.rawQuery("select name, grade from table where name = unknown@dynamic_var@unknown@<com.space150.bww.fightforfandom.b.b: com.space150.bww.fightforfandom.a.a b(com.space150.bww.fightforfandom.a.a)>@94@33!!!0", null);
	}

	/*
	public void testNotCachingInvalidCase4()
	{
		
		db.rawQuery("select name, grade from table", null);
		db.execSQL("insert into table values (1, 'bruce')");
		db.rawQuery("select name, grade from table", null);

	}
	*/
	
	//matches may relation but not must
	/*
	public void testNotCachingCase5()
	{
		db.rawQuery("select name, grade from student", null);
		if(db.isDatabaseIntegrityOk())
			db.execSQL("UPDATE student SET name = 'Texas', grade = 20000.00;");
		db.rawQuery("select name, grade from student", null);
		
	}
	*/

	public void testNotCachingLoopInvalid1()
	{
		while(db.isOpen())
		{
			db.rawQuery("select name, grade from student", null);
			if(db.isDatabaseIntegrityOk())
				db.execSQL("delete from student where id = 1");
		}
	}
	
	
	public void testNotCachingLoopInvalid2()
	{
		while(db.isOpen())
		{
			db.rawQuery("select name, grade from student", null);
			while(db.isDatabaseIntegrityOk())
				db.execSQL("delete from student where id = 1");
		}
	}
	
}
