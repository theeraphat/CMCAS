package th.ac.cmru.computer.cmcas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChkAnsData extends SQLiteOpenHelper{
    private Cursor cursor;
    private ContentValues cv;

    public ChkAnsData(Context context) {
        super(context, "ChkAnsData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(
                "CREATE TABLE course (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "course_code TEXT," +
                    "course_name TEXT," +
                    "course_value INTEGER DEFAULT 0" +
                ");"
        );
        db.execSQL(
                "CREATE TABLE student (" +
                        "std_code INTEGER PRIMARY KEY," +
                        "std_first TEXT," +
                        "std_last TEXT" +
                 ");"
        );
        db.execSQL(
                "CREATE TABLE score (" +
                    "course_id INTEGER REFERENCES course(_id) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "std_code INTEGER REFERENCES student(std_code) ON UPDATE CASCADE ON DELETE NO ACTION," +
                    "std_score INTEGER," +
                    "std_guess INTEGER," +
                    "PRIMARY KEY(course_id, std_code)" +
                ");"
        );
        db.execSQL(
                "CREATE TABLE choice (" +
                    "course_id INTEGER REFERENCES course(_id) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "choice_no INTEGER," +
                    "choice_key INTEGER DEFAULT 0," +
                    "choice_1 INTEGER DEFAULT 0," +
                    "choice_2 INTEGER DEFAULT 0," +
                    "choice_3 INTEGER DEFAULT 0," +
                    "choice_4 INTEGER DEFAULT 0," +
                    "choice_5 INTEGER DEFAULT 0," +
                    "PRIMARY KEY(course_id, choice_no)" +
                ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS course");
        db.execSQL("DROP TABLE IF EXISTS student");
        db.execSQL("DROP TABLE IF EXISTS score");
        db.execSQL("DROP TABLE IF EXISTS choice");
        onCreate(db);
    }

    public Cursor selectCourse(SQLiteDatabase db) {
        String[] queryColumns = new String[]{
                "_id",
                "course_code",
                "course_name"
        };
        return db.query("course", queryColumns, null, null, null, null, "course_code");
    }

    public boolean insertCourse(SQLiteDatabase db,String course_code,String course_name){
        cv = new ContentValues();
        cv.put("course_code", course_code);
        cv.put("course_name", course_name);
        int course_id = (int) db.insert("course", null, cv);

        cv = new ContentValues();
        for (int i=1;i<=90;i++) {
            cv = new ContentValues();
            cv.put("course_id", course_id);
            cv.put("choice_no", i);
            db.insert("choice", null, cv);
        }
        return course_id>0?true:false;
    }

    public boolean updateCourse(SQLiteDatabase db, String[] course_id, String course_code, String course_name) {
        cv = new ContentValues();
        cv.put("course_code", course_code);
        cv.put("course_name", course_name);
        return db.update("course", cv, "_id=?", course_id)>0?true:false;
    }

    public boolean deleteCourse(SQLiteDatabase db, String[] course_id) {
        return db.delete("course", "_id=?", course_id)>0?true:false;
    }

    public int selectCourseCount(SQLiteDatabase db, String[] course_id) {
        cursor = db.query("course", new String[]{"course_value"}, "_id=?", course_id, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void updateCourseCount(SQLiteDatabase db, String course_id) {
        db.execSQL("UPDATE course SET course_value = course_value + 1 WHERE _id="+course_id);
    }

    public String[][] selectStdScore(SQLiteDatabase db,String course_id) {
        cursor = db.rawQuery("SELECT " +
                    "sc.std_code," +
                    "CASE WHEN sd.std_first IS NULL THEN '-' ELSE sd.std_first END," +
                    "CASE WHEN sd.std_last IS NULL THEN '-' ELSE sd.std_last END," +
                    "sc.std_score," +
                    "CASE sc.std_guess WHEN 1 THEN 'มั่ว' ELSE '' END " +
                "FROM score sc " +
                    "LEFT JOIN student sd ON sc.std_code = sd.std_code " +
                "WHERE sc.course_id = " + course_id
                , null);
        String[][] student_score = new String[cursor.getCount()][5];
        int i=0;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                student_score[i][0] = cursor.getString(0);
                student_score[i][1] = cursor.getString(1);
                student_score[i][2] = cursor.getString(2);
                student_score[i][3] = cursor.getString(3);
                student_score[i][4] = cursor.getString(4);
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        return student_score;
    }

    public boolean checkStdExists(SQLiteDatabase db, String course_id, String std_code) {
        cursor = db.query("score",
                new String[]{"std_code"},
                "course_id=? AND std_code=?",
                new String[]{course_id, std_code},
                null, null, null);
        int cournt = cursor.getCount();
        cursor.close();
        return cournt>0?true:false;
    }

    public void insertScore(SQLiteDatabase db, String course_id, String std_code, String std_score, int std_guess) {
        cv = new ContentValues();
        cv.put("course_id", course_id);
        cv.put("std_code", std_code);
        cv.put("std_score", std_score);
        cv.put("std_guess", std_guess);
        db.insert("score", null, cv);
    }

    public void updateScore(SQLiteDatabase db, String course_id, String std_code, String std_score, int std_guess) {
        cv = new ContentValues();
        cv.put("std_score", std_score);
        cv.put("std_guess", std_guess);
        db.update("score", cv, "course_id=? AND std_code=?", new String[]{course_id, std_code});
    }

    public boolean deleteScore(SQLiteDatabase db, String course_id, String std_code) {
        return db.delete("score", "course_id=? AND std_code=?", new String[]{course_id, std_code})>0?true:false;
    }

    public void updateItemChoice(SQLiteDatabase db, String course_id, String choice_no, String choice) {
        choice = "choice_"+choice;
        cv = new ContentValues();
        cv.put(choice, 0);
        db.execSQL("UPDATE choice SET " + choice + " = " + choice +" + 1 " +
                "WHERE course_id="+course_id+" and choice_no="+choice_no);
    }

    public int[] getItemKey(SQLiteDatabase db, String[] course_id) {
        int idx=0;
        int[] answer = new int[90];
        cursor = db.query("choice", new String[]{"choice_key"}, "course_id=?", course_id, null, null, null);
        cursor.moveToFirst();
        do {
            answer[idx++] = cursor.getInt(0);
        } while (cursor.moveToNext());
        cursor.close();
        return answer;
    }

    public void updateItemKey(SQLiteDatabase db, String course_id, int[] item_yes) {
        for(int i=0;i<item_yes.length;i++) {
            cv = new ContentValues();
            cv.put("choice_key", item_yes[i]);
            db.update("choice", cv, "course_id=? AND choice_no=?", new String[]{course_id, String.valueOf(i+1)});
        }
    }

    public void insertStdName(SQLiteDatabase db, String[] text) {
        cv = new ContentValues();
        cv.put("std_code", text[0]);
        cv.put("std_first", text[1]);
        cv.put("std_last", text[2]);
        db.insert("student", null, cv);
        db.close();
    }

    public boolean truncate(SQLiteDatabase db, String table_name) {
        return db.delete(table_name, null, null)>0?true:false;
    }
}
