package th.ac.cmru.computer.cmcas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import th.ac.cmru.computer.cmcas.databinding.ActivityStudentScoreBinding;

public class StudentScoreActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    int position;
    String course_id;
    String[][] student_score;

    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    ActivityStudentScoreBinding binding;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_student_score);
        binding.listStdScore.setOnItemClickListener(this);
        intent = getIntent();
        course_id = intent.getStringExtra("course_id");
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new ChkAnsData(this);
        db = dbHelper.getReadableDatabase();
        student_score = dbHelper.selectStdScore(db, course_id);
        binding.listStdScore.setAdapter(new AdapterStdScore(this, student_score));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.dialog_del_header);
        alertDialogBuilder.setMessage(student_score[position][0] + " " +
                student_score[position][1] + " " + student_score[position][2]);
        alertDialogBuilder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteStdScore();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void deleteStdScore() {
        if(dbHelper.deleteScore(db, course_id, student_score[position][0]))
            Toast.makeText(getApplicationContext(), R.string.del_success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_SHORT).show();
        onResume();
    }

    public void clickExportCSV(View view) {
        if(Build.VERSION.SDK_INT >= 23) {
            PermissionsRunTime permissionsRunTime = new PermissionsRunTime(getParent(), new ActivityCompat(), new ContextCompat());
            if (!permissionsRunTime.checkPermission(permissionsRunTime.WRITE_EXTERNAL_STORAGE_PERMISSION))
                return;
            exportCSVFile();
        } else {
            exportCSVFile();
        }
    }

    public void exportCSVFile() {
        String path = Environment.getExternalStorageDirectory() + "/"
                + intent.getStringExtra("course_code").replace("\\S+", "_") + ".csv";

        FileUtility fileUtility = new FileUtility(getParent());
        if(fileUtility.exportCSV(course_id, path))
            Toast.makeText(getParent(), path, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getParent(), R.string.exception, Toast.LENGTH_SHORT).show();
    }
}
