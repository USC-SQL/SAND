



package sql.sand.abstraction;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

/**
 * Created by yingjunlyu on 3/27/20.
 */

public class Toy {
    SQLiteDatabase db;
    TextView tx;
    boolean c;
    public void example()
    {
        TextView text = tx;
        boolean condition  = c;


        String userInput = text.getText().toString();
        db.beginTransaction();
        while(condition)
        {
            db.execSQL("INSERT INTO students (name) VALUES ('" + userInput + "')");
        }
        db.endTransaction();
        Cursor cursor = db.rawQuery("SELECT name FROM students", null);
        String name = cursor.getString(0);
        text.setText(name);


        db.execSQL("INSERT INTO students (name) VALUES ('" + userInput + "')");

        ContentValues cv = new ContentValues();
        cv.put("name", userInput);
        db.insert("students", null, cv);

        db.delete("students", "id > 10", null);

        db.rawQuery("SELECT name FROM students WHERE id > 10", null);

        db.query("students", new String[] {"name"}, "id > 10", null, null, null, null);
    }


    public void addStudent(String name)
    {

    }
}
