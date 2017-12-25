package th.ac.cmru.computer.cmcas;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class PermissionsRunTime extends AppCompatActivity {
    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    public static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    ContextCompat contextCompat;
    ActivityCompat activityCompat;
    Activity activity;

    public PermissionsRunTime(Activity activity, ActivityCompat activityCompat , ContextCompat contextCompat) {
        this.activity = activity;
        this.activityCompat = activityCompat;
        this.contextCompat = contextCompat;
    }

    public boolean checkPermission(String permission){
        if (contextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
            activityCompat.requestPermissions(activity, new String[]{permission}, 1);
            return false;
        }
        return true;
    }
}
