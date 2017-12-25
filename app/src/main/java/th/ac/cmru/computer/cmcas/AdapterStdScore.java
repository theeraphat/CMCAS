package th.ac.cmru.computer.cmcas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterStdScore extends BaseAdapter {
    Context context;
    String[][] student_score;

    public AdapterStdScore(Context context, String[][] student_score) {
        this.context = context;
        this.student_score = student_score;
    }

    @Override
    public int getCount() {
        return student_score.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null)
            convertView = inflater.inflate(R.layout.adapter_std_score, parent, false);

        TextView tvStdCode = (TextView) convertView.findViewById(R.id.tv_std_code);
        TextView tvStdFirst = (TextView) convertView.findViewById(R.id.tv_std_first);
        TextView tvStdLast = (TextView) convertView.findViewById(R.id.tv_std_last);
        TextView tvStdScore = (TextView) convertView.findViewById(R.id.tv_std_score);
        TextView tvStdGuess = (TextView) convertView.findViewById(R.id.tv_std_guess);

        tvStdCode.setText(student_score[position][0]);
        tvStdFirst.setText(student_score[position][1]);
        tvStdLast.setText(student_score[position][2]);
        tvStdScore.setText(student_score[position][3]);
        tvStdGuess.setText(student_score[position][4]);

        return convertView;
    }
}
