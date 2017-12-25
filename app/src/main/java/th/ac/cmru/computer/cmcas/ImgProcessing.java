package th.ac.cmru.computer.cmcas;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ImgProcessing extends AppCompatActivity {
    private String TAG;
    public int resultWidth = 800;
    public int resultHeight = 600;

    public static Context context;

    public ImgProcessing(Context context) {
        this.context = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().toString();
    }

    public Point[] getRectPoint(Mat imgResize, Mat imgEdge) {
        double maxArea = -1;
        int maxAreaIdx = -1;
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(imgEdge, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        MatOfPoint temp_contour; //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize * 0.05, true);
                if (approxCurve.total() == 4) {
                    Log.d(TAG, "approxCurve.total: "+String.valueOf(approxCurve.total()));
                    maxCurve = approxCurve;
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    largest_contours.add(temp_contour);
                }
            }
        }

        double tmp_double[];
        Point[] point = new Point[4];

        try {
            tmp_double = maxCurve.get(0, 0);
            point[0] = new Point(tmp_double[0], tmp_double[1]);
            tmp_double = maxCurve.get(1, 0);
            point[1] = new Point(tmp_double[0], tmp_double[1]);
            tmp_double = maxCurve.get(2, 0);
            point[2] = new Point(tmp_double[0], tmp_double[1]);
            tmp_double = maxCurve.get(3, 0);
            point[3] = new Point(tmp_double[0], tmp_double[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "p1("+point[0].x+","+point[0].y+")");
        Log.d(TAG, "p2("+point[1].x+","+point[1].y+")");
        Log.d(TAG, "p3("+point[2].x+","+point[2].y+")");
        Log.d(TAG, "p4("+point[3].x+","+point[3].y+")");

        //Imgproc.cvtColor(imgResize, imgResize, Imgproc.COLOR_BayerBG2RGB);
        //Imgproc.drawContours(imgResize, contours, maxAreaIdx, new Scalar(255, 255, 255), 1); //will draw the largest square/rectangle

        Imgproc.circle(imgResize, new Point(point[0].x, point[0].y), 20, new Scalar(0, 0, 255), 5); // red
        Imgproc.circle(imgResize, new Point(point[1].x, point[1].y), 20, new Scalar(0, 255, 0), 5); // green
        Imgproc.circle(imgResize, new Point(point[2].x, point[2].y), 20, new Scalar(255, 0, 0), 5); // blue
        Imgproc.circle(imgResize, new Point(point[3].x, point[3].y), 20, new Scalar(0, 255, 255), 5); // yellow

        // 1/3 writeImageJpg("Point",imgResize);

        return point;
    }

    public Mat warpPerspectiveTransform(Mat imgInput,Point[] point) {

        List<Point> src_pnt = new ArrayList<Point>();

        if( (point[0].x > resultWidth/2) && (point[0].y < resultHeight/2) ) {
            src_pnt.add(point[1]);
            src_pnt.add(point[2]);
            src_pnt.add(point[3]);
            src_pnt.add(point[0]);
            Log.d(TAG, "Change point to TOP_RIGHT");
        } else if( (point[0].x > resultWidth/2) && (point[0].y > resultHeight/2) ) {
            src_pnt.add(point[2]);
            src_pnt.add(point[3]);
            src_pnt.add(point[0]);
            src_pnt.add(point[1]);
            Log.d(TAG, "Change point to BUTTON_RIGHT");
        }else if( (point[0].x < resultWidth/2) && (point[0].y > resultHeight/2) ) {
            src_pnt.add(point[3]);
            src_pnt.add(point[0]);
            src_pnt.add(point[1]);
            src_pnt.add(point[2]);
            Log.d(TAG, "Change point to BUTTON_LEFT");
        } else {
            src_pnt.add(point[0]);
            src_pnt.add(point[1]);
            src_pnt.add(point[2]);
            src_pnt.add(point[3]);
            Log.d(TAG, "Point YES");
        }

        Mat startM = Converters.vector_Point2f_to_Mat(src_pnt);

        List<Point> dst_pnt = new ArrayList<Point>();

        dst_pnt.add(new Point(0, 0));
        dst_pnt.add(new Point(0, resultHeight));
        dst_pnt.add(new Point(resultWidth, resultHeight));
        dst_pnt.add(new Point(resultWidth, 0));

        Mat endM = Converters.vector_Point2f_to_Mat(dst_pnt);

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(imgInput,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }

    public int[][][] getChoiceValue(Mat image){
        int[] pixel_c = {44,172,300,425,552,681};
        int pixel_r = 293;
        int pixel_w = 100;
        int pixel_h = 285;
        int col_num = 6;
        int row_s = 15;
        int col_s = 5;
        int step_c = 20;
        int step_r = 19;
        int[][][] result = new int[col_num][row_s][col_s];

        for(int i=0;i<col_num;i++) {
            Mat column = image.submat(pixel_r, pixel_r + pixel_h, pixel_c[i], pixel_c[i] + pixel_w);
            for (int r=0; r<row_s; r++) {
                int max = Integer.MIN_VALUE;
                int min = Integer.MAX_VALUE;
                int[] value = new int[col_s];
                for (int c=0; c<col_s; c++) {
                    Mat cell = column.submat(r*step_r, (r+1)*step_r, c*step_c, (c+1)*step_c);

                    Imgproc.rectangle(image,
                            new Point(c*step_c + pixel_c[i], r*step_r + pixel_r),
                            new Point((c+1)*step_c + pixel_c[i], (r+1)*step_r + pixel_r),
                            new Scalar(0, 0, 255));

                    Scalar sum = Core.sumElems(cell);
                    value[c] = (int)sum.val[0];
                    if(sum.val[0] > max)
                        max = (int)sum.val[0];
                    if(sum.val[0] < min)
                        min = (int)sum.val[0];
                }
                int range = ( max - min ) / 2 + min;
                if (range > 10000) {
                    int ii = 0;
                    for (int val : value) {
                        int on = val > range ? 0 : 1;
                        result[i][r][ii++] = on;
                    }
                }
            }
            /*Imgproc.rectangle(image,
                    new Point(pixel_c[i], pixel_r),
                    new Point(pixel_c[i] + pixel_w, pixel_r + pixel_h),
                    new Scalar(0, 0, 255));*/
        }
        // 2/3 writeImageJpg("Choice", image);
        return result;
    }

    public String getStdCode(Mat image) {
        int pixel_c = 25;
        int pixel_r = 75;
        int pixel_w = 190;
        int pixel_h = 190;
        int row_s = 10;
        int col_s = 10;
        int step_c = 19;
        int step_r = 19;

        String result="";
        int minn = -1;
        Mat column = image.submat(pixel_r, pixel_r + pixel_h, pixel_c, pixel_c + pixel_w);
        for (int c=0; c<col_s; c++) {
            int min = Integer.MAX_VALUE;
            String num = "";
            for (int r=0; r<row_s; r++) {
                Mat cell = column.submat(r*step_r, (r+1)*step_r, c*step_c, (c+1)*step_c);

                Imgproc.rectangle(image,
                        new Point(r*step_r + pixel_c, c*step_c + pixel_r),
                        new Point((r+1)*step_r + pixel_c, (c+1)*step_c + pixel_r),
                        new Scalar(0,0,255));

                Scalar sum = Core.sumElems(cell);
                if ( sum.val[0] < min && ( minn < 0 || sum.val[0] - minn < 10000 ) ) {
                    if (min < Integer.MAX_VALUE)
                        minn = (int) sum.val[0];
                    min = (int) sum.val[0];
                    num = String.valueOf(r);
                }
            }
            result+=num;
        }
        // 3/3 writeImageJpg("Std_Code",column);

        return result;
    }

    /*public void writeImageJpg(String pre_name,Mat outputMat) {
        int imageNum = 0;
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagesFolder = new File(Environment.getExternalStorageDirectory()
                , "DCIM/CMCAS");
        imagesFolder.mkdirs();
        String fileName = imageNum + "_" + pre_name + ".jpg";
        File output = new File(imagesFolder, fileName);

        while (output.exists()){
            imageNum++;
            fileName = imageNum + "_" + pre_name + ".jpg";
            output = new File(imagesFolder, fileName);
        }

        Uri uri = Uri.fromFile(output);
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        ContentValues image = new ContentValues();
        String dateTaken = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        image.put(MediaStore.Images.Media.TITLE, output.toString());
        image.put(MediaStore.Images.Media.DISPLAY_NAME, output.toString());
        image.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
        image.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        image.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
        image.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        image.put(MediaStore.Images.Media.ORIENTATION, 0);
        String path = output.getParentFile().toString().toLowerCase();
        String name = output.getParentFile().getName().toLowerCase();
        image.put(MediaStore.Images.ImageColumns.BUCKET_ID, path.hashCode());
        image.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
        image.put(MediaStore.Images.Media.SIZE, output.length());
        image.put(MediaStore.Images.Media.DATA, output.getAbsolutePath());

        if(Imgcodecs.imwrite(output.toString(), outputMat)){
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
            Toast.makeText(context.getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
        }
    }*/
}
