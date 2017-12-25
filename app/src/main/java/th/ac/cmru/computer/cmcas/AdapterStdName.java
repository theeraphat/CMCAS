package th.ac.cmru.computer.cmcas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterStdName extends BaseAdapter {
    Context context;
    String[][] student_name;

    public AdapterStdName(Context context, String[][] student_name) {
        this.context = context;
        this.student_name = student_name;
    }

    @Override
    public int getCount() {
        return student_name.length;
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
            convertView = inflater.inflate(R.layout.adapter_std_name, parent, false);

        TextView tvStdCode = (TextView) convertView.findViewById(R.id.tv_std_code);
        TextView tvStdFirst = (TextView) convertView.findViewById(R.id.tv_std_first);
        TextView tvStdLast = (TextView) convertView.findViewById(R.id.tv_std_last);

        tvStdCode.setText(student_name[position][0]);
        tvStdFirst.setText(student_name[position][1]);
        tvStdLast.setText(student_name[position][2]);

        return convertView;
    }
}
