package th.ac.cmru.computer.cmcas;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

import th.ac.cmru.computer.cmcas.databinding.ActivityShowScoreBinding;

public class ShowScoreActivity extends AppCompatActivity {
    String course_id;
    LocalActivityManager mLocalActivityManager;
    Intent intent;

    ActivityShowScoreBinding binding;

    @Override
    protected void onPause() {
        super.onPause();
        mLocalActivityManager.dispatchPause(!isFinishing());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalActivityManager.dispatchResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_show_score);

        intent = getIntent();
        course_id = intent.getStringExtra("course_id");

        binding.tvShowScoreHeader.setText(intent.getStringExtra("course_code"));

        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        binding.thShowScore.setup(mLocalActivityManager);
        binding.thShowScore.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                mLocalActivityManager.dispatchResume();
            }
        });

        binding.thShowScore.addTab(binding.thShowScore
                .newTabSpec("Overview")
                .setIndicator("ภาพรวม")
                .setContent(
                        new Intent(this, ScoreOverviewActivity.class)
                                .putExtra("course_id", intent.getStringExtra("course_id"))
                )
        );

        Intent i = new Intent(this, StudentScoreActivity.class);
        i.putExtra("course_id", course_id);
        i.putExtra("course_code", intent.getStringExtra("course_code"));
        binding.thShowScore.addTab(binding.thShowScore
                .newTabSpec("Detail")
                .setIndicator("คะแนน")
                .setContent(i)
        );

        binding.thShowScore.addTab(binding.thShowScore
                .newTabSpec("Rate")
                .setIndicator("อัตราการทำถูก")
                .setContent(
                        new Intent(this, ItemAnalysisActivity.class)
                                .putExtra("course_id", intent.getStringExtra("course_id"))
                )
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String path = Environment.getExternalStorageDirectory() + "/"
                    + intent.getStringExtra("course_code").replace("\\S+", "_") + ".csv";

            FileUtility fileUtility = new FileUtility(getApplicationContext());
            if (fileUtility.exportCSV(course_id, path))
                Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), R.string.exception, Toast.LENGTH_SHORT).show();
        }
    }
}
