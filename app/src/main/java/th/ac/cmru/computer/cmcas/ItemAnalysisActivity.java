package th.ac.cmru.computer.cmcas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import th.ac.cmru.computer.cmcas.databinding.ActivityItemAnalysisBinding;

public class ItemAnalysisActivity extends AppCompatActivity {
    String course_id;
    int[][] item;

    private Cursor cursor;
    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    Intent intent;
    ActivityItemAnalysisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_item_analysis);
        intent = getIntent();
        course_id = intent.getStringExtra("course_id");
    }

    @Override
    protected void onPause() {
        super.onPause();
        cursor.close();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new ChkAnsData(this);
        db = dbHelper.getReadableDatabase();
        String[] queryColumns = new String[]{
                "choice_no",
                "choice_key",
                "choice_1",
                "choice_2",
                "choice_3",
                "choice_4",
                "choice_5"
        };
        cursor = db.query(
                "choice",
                queryColumns,
                "course_id=?",
                new String[]{course_id},
                null,
                null,
                null);
        cursor.moveToFirst();
        item = new int[cursor.getCount()][queryColumns.length];
        int i=0;
        while (!cursor.isAfterLast()){
            item[i][0] = cursor.getInt(0);
            item[i][1] = cursor.getInt(1);
            item[i][2] = cursor.getInt(2);
            item[i][3] = cursor.getInt(3);
            item[i][4] = cursor.getInt(4);
            item[i][5] = cursor.getInt(5);
            item[i][6] = cursor.getInt(6);
            i++;
            cursor.moveToNext();
        }
        int count = dbHelper.selectCourseCount(db, new String[]{course_id});
        binding.listItem.setAdapter(new AdapterItem(this, item, count));
    }
}
