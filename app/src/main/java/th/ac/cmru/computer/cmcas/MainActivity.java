package th.ac.cmru.computer.cmcas;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import th.ac.cmru.computer.cmcas.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    TextView tvDialog;
    EditText edtCode,edtName;

    private Cursor cursor;
    private SimpleCursorAdapter adapter;

    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    private Intent intent;

    private LayoutInflater layoutInflater;
    private AlertDialog.Builder alertDialogBuilder;

    ActivityMainBinding binding;

    static {
        if(OpenCVLoader.initDebug()){
            System.out.println("OpenCV loaded");
        } else {
            System.out.println("OpenCV not loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        binding.listCourse.setOnItemClickListener(this);
        binding.listCourse.setOnItemLongClickListener(this);
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
        db = dbHelper.getWritableDatabase();
        cursor = dbHelper.selectCourse(db);
        if(cursor.getCount()>0)
            binding.tvCourseHeader.setText(R.string.tv_select_course);
        else
            Toast.makeText(getApplicationContext(), R.string.how_to_add, Toast.LENGTH_LONG).show();
        String[] showColumns = new String[]{"course_code","course_name"};
        int[] views = new int[]{android.R.id.text1, android.R.id.text2};
        adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor, showColumns, views);
        binding.listCourse.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setCursorDataItem(parent, position);
        intent = new Intent(getBaseContext(), CameraActivity.class);
        putExtraIntent();

        if(Build.VERSION.SDK_INT >= 23) {
            PermissionsRunTime permissionsRunTime = new PermissionsRunTime(this, new ActivityCompat(), new ContextCompat());
            if (!permissionsRunTime.checkPermission(permissionsRunTime.CAMERA_PERMISSION))
                return;
            startCameraActivity();
        } else {
            startCameraActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startCameraActivity();
    }

    private void startCameraActivity() {
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        setCursorDataItem(parent,position);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(cursor.getString(1));
        alertDialogBuilder.setItems(R.array.array_course_choice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                switch(id){
                    case 0:
                        intent = new Intent(getBaseContext(),ShowScoreActivity.class);
                        putExtraIntent();
                        startActivity(intent);
                        break;
                    case 1:
                        updateCourse(cursor.getString(0),cursor.getString(1),cursor.getString(2));
                        break;
                    case 2:
                        deleteCourse(cursor.getString(0),cursor.getString(1),cursor.getString(2));
                        break;
                }
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        return true;
    }

    private void setCursorDataItem(AdapterView<?> parent, int position){
        cursor = (Cursor) parent.getAdapter().getItem(position);
        cursor.moveToPosition(position);
    }

    private void putExtraIntent(){
        intent.putExtra("course_id", cursor.getString(0));
        intent.putExtra("course_code", cursor.getString(1));
        intent.putExtra("course_name", cursor.getString(2));
    }

    public void clickCSV(View view) {
        intent = new Intent(getApplicationContext(), StudentNameActivity.class);
        startActivity(intent);
    }

    public void clickAddCourse(View view) {
        createCourse("", "");
    }

    private void createCourse(String course_code, String course_name) {
        createInflater();

        tvDialog.setText(R.string.dialog_add_header);
        edtCode.setText(course_code);
        edtName.setText(course_name);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.btn_add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String code_str = edtCode.getText().toString();
                String name_str = edtName.getText().toString();
                String valid_str = validation("", code_str, name_str);
                if(!valid_str.equals("")) {
                    Toast.makeText(getApplicationContext(), valid_str, Toast.LENGTH_LONG).show();
                    createCourse(code_str, name_str);
                } else {
                    if (dbHelper.insertCourse(db, code_str, name_str)) {
                        Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_LONG).show();
                    }
                    onResume();
                }

            }
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void updateCourse(final String course_id, String course_code, String course_name){
        createInflater();

        tvDialog.setText(R.string.dialog_upd_header);
        edtCode.setText(course_code);
        edtName.setText(course_name);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String code_str = edtCode.getText().toString();
                String name_str = edtName.getText().toString();
                String valid_str = validation(course_id, code_str, name_str);
                if(!valid_str.equals("")) {
                    Toast.makeText(getApplicationContext(), valid_str, Toast.LENGTH_LONG).show();
                    updateCourse(course_id, code_str, name_str);
                } else {
                    if (dbHelper.updateCourse(db, new String[]{course_id}, code_str, name_str)) {
                        Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_LONG).show();
                    }
                    onResume();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void deleteCourse(final String course_id, String course_code, String course_name){
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.dialog_del_header);
        alertDialogBuilder.setMessage(Html.fromHtml("<b>"+course_code+"</b><br>"+course_name));
        alertDialogBuilder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dbHelper.deleteCourse(db, new String[]{course_id}))
                    Toast.makeText(getApplicationContext(), R.string.del_success, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_LONG).show();
                onResume();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();

    }

    private void createInflater() {
        layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);

        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        tvDialog = (TextView) promptView.findViewById(R.id.tvDialog);
        edtCode = (EditText) promptView.findViewById(R.id.edtCode);
        edtName = (EditText) promptView.findViewById(R.id.edtName);
    }

    private String validation(String course_id, String inpCode, String inpName){
        String re = "([a-zA-Z0-9ก-ฮะ-ูแ-์๐-๙_-]*[\\s]*)*";
        if(inpCode.isEmpty() || inpName.isEmpty())
            return "กรุณากรอกข้อมูลให้ครบถ้วน";
        if(!inpCode.matches(re) || !inpName.matches(re))
            return "ห้ามใช้อักษรพิเศษ";
        if(checkCourseExists(course_id, inpCode))
            return "คุณไม่สามารถใช้รหัสวิชานี้ได้";
        return "";
    }

    private boolean checkCourseExists(String course_id,String course_code){
        cursor = db.query("course", new String[]{"course_code"}, "course_code=? AND _id!=?", new String[]{course_code,course_id}, null, null, null);
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }
}
