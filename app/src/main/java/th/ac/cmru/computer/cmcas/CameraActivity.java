package th.ac.cmru.computer.cmcas;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import th.ac.cmru.computer.cmcas.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback, Camera.ShutterCallback, SensorEventListener, Camera.AutoFocusCallback {
    int onCikBt=0;
    int[] answer;
    String course_id,course_code;

    static Camera camera;

    SurfaceHolder surfaceHolderPreview;

    Sensor mAccelerometer;
    SensorManager mSensorManager;

    float motionX = 0;
    float motionY = 0;
    float motionZ = 0;

    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    ImgProcessing imgProcessObj;
    ActivityCameraBinding binding;

    Intent intent;

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
        mSensorManager.unregisterListener(this, mAccelerometer);
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        dbHelper = new ChkAnsData(this);
        db = dbHelper.getWritableDatabase();
        answer = dbHelper.getItemKey(db, new String[]{course_id});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        surfaceHolderPreview = binding.sfvPreview.getHolder();
        surfaceHolderPreview.addCallback(this);
        surfaceHolderPreview.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer =  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        imgProcessObj = new ImgProcessing(this);

        intent = getIntent();
        course_id = intent.getStringExtra("course_id");
        course_code = intent.getStringExtra("course_code");
    }

    public void refreshCamera() {
        if(surfaceHolderPreview.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            camera.setPreviewDisplay(surfaceHolderPreview);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = camera.getParameters();

        int encode_format  = params.getPreviewFormat();
        switch (encode_format) {
            case 4:
                params.setPreviewFormat(ImageFormat.RGB_565);
                break;
            case 16:
                params.setPreviewFormat(ImageFormat.NV16);
                break;
            case 17:
                params.setPreviewFormat(ImageFormat.NV21);
                break;
            case 20:
                params.setPreviewFormat(ImageFormat.YUY2);
                break;
        }
        List<Camera.Size> previewSize = params.getSupportedPreviewSizes();
        List<Camera.Size> pictureSize = params.getSupportedPictureSizes();

        params.setPreviewSize(previewSize.get(0).width,previewSize.get(0).height);
        params.setPictureSize(pictureSize.get(0).width,pictureSize.get(0).height);

        params.setJpegQuality(100);

        camera.setParameters(params);

        try {
            camera.setPreviewDisplay(surfaceHolderPreview);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera != null) {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onShutter() {}

    @Override
    public void onAutoFocus(boolean success, Camera camera) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(Math.abs(event.values[0] - motionX) > 1
                || Math.abs(event.values[1] - motionY) > 1
                || Math.abs(event.values[2] - motionZ) > 1 ) {
            try {
                camera.autoFocus(this);
            } catch (RuntimeException e) {
                refreshCamera();
                Log.d(getLocalClassName(), e.getMessage());
            }
            motionX = event.values[0];
            motionY = event.values[1];
            motionZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        camera.stopPreview();

        Mat imgSource = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        Mat imgResize = new Mat();
        Imgproc.resize(imgSource,imgResize,new Size(imgProcessObj.resultWidth,imgProcessObj.resultHeight));
        // 1/10 imgProcessObj.writeImageJpg("1_Resize", imgResize);

        Mat imgBlur = new Mat();
        Imgproc.GaussianBlur(imgResize, imgBlur, new Size(5, 5), 2.2, 2);
        // 2/10 imgProcessObj.writeImageJpg("2_GBlur", imgBlur);

        Imgproc.blur(imgBlur, imgBlur, new Size(5, 5));
        // 3/10 imgProcessObj.writeImageJpg("3_Blur", imgBlur);

        Mat imgGray = new Mat();
        Imgproc.cvtColor(imgBlur, imgGray, Imgproc.COLOR_BGR2GRAY);
        // 4/10 imgProcessObj.writeImageJpg("4_Gray", imgGray);

        Mat imgThHold = new Mat();
        Imgproc.threshold(imgGray, imgThHold, 0, 255,
                Imgproc.THRESH_TOZERO + Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //Imgproc.adaptiveThreshold(imgGray, imgThHold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
        // 5/10 imgProcessObj.writeImageJpg("5_ThHold", imgThHold);

        Mat imgResizeG = new Mat();
        Imgproc.cvtColor(imgResize, imgResizeG, Imgproc.COLOR_BGR2GRAY);

        Mat imgEdge = new Mat();
        Imgproc.Canny(imgThHold, imgEdge, 0, 255);
        // 6/10 imgProcessObj.writeImageJpg("6_Canny", imgEdge);

        try {
            Point[] point = imgProcessObj.getRectPoint(imgResize, imgEdge);

            //Imgproc.cvtColor(imgResize, imgGray, Imgproc.COLOR_BGR2GRAY);
            Mat inputMat = imgProcessObj.warpPerspectiveTransform(imgResizeG, point);
            // 7/10 imgProcessObj.writeImageJpg("7_Warp", inputMat);

            Imgproc.GaussianBlur(inputMat, inputMat, new Size(15, 15), 2.2, 2);
            // 8/10 imgProcessObj.writeImageJpg("8_GBlur_Warp", inputMat);

            Imgproc.blur(inputMat, inputMat, new Size(9, 9));
            // 9/10 imgProcessObj.writeImageJpg("9_Blur_Warp", inputMat);

            Imgproc.adaptiveThreshold(inputMat, inputMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 15, 15);

            /*Imgproc.threshold(inputMat, inputMat, 0, 255,
                    Imgproc.THRESH_TOZERO + Imgproc.THRESH_OTSU + Imgproc.THRESH_TRUNC);*/
            // 10/10 imgProcessObj.writeImageJpg("10_Input_Warp", inputMat);

            if (onCikBt == 1) {
                int[][][] choice_result = imgProcessObj.getChoiceValue(inputMat);
                answer = getChoice(choice_result);
                dbHelper.updateItemKey(db, course_id,answer);
                Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
            } else if(onCikBt == 2) {
                String std_code = imgProcessObj.getStdCode(inputMat);

                if (std_code.length() < 1 || std_code.length() > 10) {
                    Log.d(getLocalClassName(), "std_code our of length +\""+std_code+"\"");
                    throw new Exception();
                }
                Log.d(getLocalClassName(), "std_code: "+std_code);
                int sum_ans=0;
                for(int ans:answer)
                    sum_ans+=ans;
                if(sum_ans < 1){
                    Toast.makeText(getApplicationContext(), R.string.answer_key_not_set, Toast.LENGTH_SHORT).show();
                    return;
                }

                int[][][] choice_value = imgProcessObj.getChoiceValue(inputMat);
                int[] value = getChoice(choice_value);
                int score = 0;
                boolean boo = false;
                for (int i = 0; i < value.length; i++) {
                    if ( answer[i] != 0 || value[i] != 0) {
                        if (answer[i] == value[i]) {
                            dbHelper.updateItemChoice(db, course_id, String.valueOf(i + 1), String.valueOf(value[i]));
                            score++;
                        }
                        boo = true;
                    }
                }

                if (boo) {
                    int guess = checkGuess(value) ? 1 : 0;
                    if (dbHelper.checkStdExists(db, course_id, std_code))
                        dbHelper.updateScore(db, course_id, std_code, String.valueOf(score), guess);
                    else
                        dbHelper.insertScore(db, course_id, std_code, String.valueOf(score), guess);
                    dbHelper.updateCourseCount(db, course_id);
                    Toast.makeText(getApplicationContext(),
                            std_code + " : " + score + (guess == 1 ? "\nพบการเดาสุ่ม" : "")
                            , Toast.LENGTH_SHORT).show();
                } else {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        camera.startPreview();
    }

    private int[] getChoice(int[][][] choice) {
        int block = choice.length;
        int row = choice[0].length;
        int col = choice[0][0].length;
        int[] result = new int[block*row];
        int idx=0;
        for(int i=0;i<block;i++) {
            for(int j=0;j<row;j++) {
                int sum=0;
                for(int k=0;k<col;k++) {
                    sum+=choice[i][j][k];
                    if(choice[i][j][k] == 1)
                        result[idx] = k+1;
                }
                if(sum>1)
                    result[idx] = 10;
                idx++;
            }
        }
        return result;
    }

    private boolean checkGuess(int[] choice) {
        int[] limit = {15, 30, 45, 60, 75, 90};
        for(int lim:limit) {
            int alone_guess=0;
            int sort_guess=0;
            for (int i = lim - 14; i < lim; i++) {
                if (choice[i - 1] == choice[i])
                    alone_guess++;
                if (choice[i] == choice[i-1] - 1 || choice[i] == choice[i-1] + 1)
                    sort_guess++;
            }
            if (alone_guess > 8 || sort_guess > 8)
                return true;
        }
        return false;
    }

    public void clickAnsSheet(View view){
        onCikBt=1;
        camera.takePicture(null, null, this);
    }

    public void clickSheet(View view){
        onCikBt=2;
        camera.takePicture(null, null, this);
    }

    public void clickShowScore(View view){
        intent = new Intent(getApplicationContext(), ShowScoreActivity.class);
        intent.putExtra("course_id", course_id);
        intent.putExtra("course_code", course_code);
        startActivity(intent);
    }
}
