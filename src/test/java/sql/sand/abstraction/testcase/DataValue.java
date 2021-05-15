package sql.sand.abstraction.testcase;

import android.database.sqlite.SQLiteDatabase;

public class DataValue {

	SQLiteDatabase db;
	public void testInsert()
	{
		db.execSQL("insert into table (id, name) values (10, Unknown@METHOD@<invoking_method_signature>@<containing_method_signature>@source_line_number@bytecode_offset!!!)");
		
		db.execSQL("insert into table select * from table2 where id > 20 and id < 40");
	}
	
	public void testSelect()
	{
		db.rawQuery("select id from table where name = Unknown@PARA@<method_signature>@parameter_index!!! or id < 10", null);
	}
	
	public void testUpdate()
	{
		db.execSQL("UPDATE table_name SET column1 = value1 WHERE id > Unknown@PARA@<method_signature>@parameter_index!!!");
	}
	
	public void testDelete()
	{
		db.execSQL("delete from table where id < 10 and id > 5");
	}
	
}
