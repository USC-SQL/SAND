package sql.sand.antipattern.testcase;

import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;

public class Password {

	private SQLiteDatabase db;
	private EditText et;
	public void testReadablePassword()
	{
		String pwd = et.getText().toString();
		db.execSQL("UPDATE REG SET PASSWORD = " + pwd);
		
		
		db.rawQuery("SELECT id FROM REG WHERE PWD = " + pwd, null);
		
		
		db.execSQL("INSERT INTO REG (ID, PWD) VALUES (1, 'abc')");
	}
}
