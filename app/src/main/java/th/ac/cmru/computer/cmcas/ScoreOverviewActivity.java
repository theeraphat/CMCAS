package th.ac.cmru.computer.cmcas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;

import th.ac.cmru.computer.cmcas.databinding.ActivityScoreOverviewBinding;

public class ScoreOverviewActivity extends AppCompatActivity {
    private Cursor cursor;
    private SQLiteDatabase db;
    private ChkAnsData dbHelper;

    ActivityScoreOverviewBinding binding;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_score_overview);
        intent = getIntent();
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
        cursor = db.query(
                "score",
                new String[]{"std_score"},
                "course_id=?",
                new String[]{intent.getStringExtra("course_id")},
                null,
                null,
                null);
        int count = cursor.getCount();
        int[] score = new int[count];

        if(count > 0) {
            int idx = 0;
            cursor.moveToFirst();
            do {
                score[idx++] = cursor.getInt(0);
            } while (cursor.moveToNext());

            Arrays.sort(score);
            binding.tvMaxValue.setText(String.valueOf(score[count - 1]));
            binding.tvMinValue.setText(String.valueOf(score[0]));

            float sum = 0;
            for (int i : score)
                sum += i;
            float mean = sum/count;
            binding.tvMeanValue.setText(String.format("%.2f", mean));

            int med;
            if (count % 2 == 0)
                med = (score[count / 2 - 1] + score[count / 2]) / 2;
            else
                med = score[idx / 2];
            binding.tvMedianValue.setText(String.valueOf(med));
            String mode = "";
            HashMap<Integer, Integer> map = new HashMap<>();
            for (int i : score) {
                try {
                    map.put(i, map.get(i) + 1);
                } catch (Exception e) {
                    map.put(i, 1);
                }
            }
            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            int value = 0,repeat = 0;
            for (int key : map.keySet()){
                int val = map.get(key);
                if (max == val) {
                    repeat++;
                    mode += ","+key;
                }
                if (max < val) {
                    max = val;
                    mode = String.valueOf(key);
                    repeat = 0;
                    value = val;
                }
                if (min > val)
                    min = val;
            }
            mode += " ("+value+")";
            if(max != min && repeat < 2)
                binding.tvModeValue.setText(mode);

            float variance=0;
            for (int i : score)
                variance += Math.pow((i - mean), 2);
            variance /= count-1;
            if (!Float.isNaN(variance)) {
                binding.tvStdDivValue.setText(String.format("%.2f", Math.sqrt(variance)));
                binding.tvVarianceValue.setText(String.valueOf(String.format("%.2f", variance)));
            }
        }
    }
}
