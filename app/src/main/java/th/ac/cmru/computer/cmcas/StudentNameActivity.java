package th.ac.cmru.computer.cmcas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;

import th.ac.cmru.computer.cmcas.databinding.ActivityStudentNameBinding;

public class StudentNameActivity extends AppCompatActivity {
    private static final int RESULT_CODE = 12345;
    private Cursor cursor;
    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    ActivityStudentNameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_student_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new ChkAnsData(this);
        db = dbHelper.getWritableDatabase();
        String[] queryColumns = new String[]{
                "std_code",
                "std_first",
                "std_last"
        };
        cursor = db.query("student", queryColumns, null, null, null, null, null);
        cursor.moveToFirst();
        String[][] student_name = new String[cursor.getCount()][queryColumns.length];
        int i=0;
        while (!cursor.isAfterLast()){
            student_name[i][0] = cursor.getString(0);
            student_name[i][1] = cursor.getString(1);
            student_name[i][2] = cursor.getString(2);
            i++;
            cursor.moveToNext();
        }
        binding.listStdName.setAdapter(new AdapterStdName(this, student_name));
    }

    @Override
    protected void onPause() {
        super.onPause();
        cursor.close();
        db.close();
    }

    public void clickImport(View view) {
        if(Build.VERSION.SDK_INT >= 23) {
            PermissionsRunTime permissionsRunTime = new PermissionsRunTime(this, new ActivityCompat(), new ContextCompat());
            if (!permissionsRunTime.checkPermission(permissionsRunTime.READ_EXTERNAL_STORAGE_PERMISSION))
                return;
            startActivityActionGetContent();
        } else {
            startActivityActionGetContent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startActivityActionGetContent();
    }

    private void startActivityActionGetContent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE && resultCode == RESULT_OK) {
            FileUtility fileUtility = new FileUtility(getApplicationContext());
            if(fileUtility.importCSV(data.getData().getPath()))
                Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_SHORT).show();

            onResume();
        }
    }

    public void clickTruncate(View view) {
        if(dbHelper.truncate(db,"student"))
            Toast.makeText(getApplicationContext(), R.string.del_success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_SHORT).show();
        onResume();
    }
}
