package th.ac.cmru.computer.cmcas;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterItem extends BaseAdapter {
    Context context;
    int count;
    int[][] item;
    String[] choice_thai = {"","(ก)","(ข)","(ค)","(ง)","(จ)"};

    public AdapterItem(Context context, int[][] item, int count) {
        this.context = context;
        this.count = count;
        this.item = item;
    }

    @Override
    public int getCount() {
        return item.length;
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
            convertView = inflater.inflate(R.layout.adapter_item, parent, false);

        TextView tvItemNo = (TextView) convertView.findViewById(R.id.tv_item_no);
        TextView tvItemPercent = (TextView) convertView.findViewById(R.id.tv_item_percent);
        TextView tvItemChoice1 = (TextView) convertView.findViewById(R.id.tv_item_choice1);
        TextView tvItemChoice2 = (TextView) convertView.findViewById(R.id.tv_item_choice2);
        TextView tvItemChoice3 = (TextView) convertView.findViewById(R.id.tv_item_choice3);
        TextView tvItemChoice4 = (TextView) convertView.findViewById(R.id.tv_item_choice4);
        TextView tvItemChoice5 = (TextView) convertView.findViewById(R.id.tv_item_choice5);

        int[] choice = {
                item[position][2],
                item[position][3],
                item[position][4],
                item[position][5],
                item[position][6],
        };
        float percent = -1;
        int key = item[position][1];
        if (key > 0)
        switch (key) {
            case 1:
                if(choice[0] > 0)
                    percent = (float)choice[0]/count*100;
                else
                    percent = 0;
                tvItemChoice1.setTextColor(Color.GREEN);
                tvItemChoice2.setTextColor(Color.RED);
                tvItemChoice3.setTextColor(Color.RED);
                tvItemChoice4.setTextColor(Color.RED);
                tvItemChoice5.setTextColor(Color.RED);
                break;
            case 2:
                if(choice[1] > 0)
                    percent = (float)choice[1]/count*100;
                else
                    percent = 0;
                tvItemChoice1.setTextColor(Color.RED);
                tvItemChoice2.setTextColor(Color.GREEN);
                tvItemChoice3.setTextColor(Color.RED);
                tvItemChoice4.setTextColor(Color.RED);
                tvItemChoice5.setTextColor(Color.RED);
                break;
            case 3:
                if(choice[2] > 0)
                    percent = (float)choice[2]/count*100;
                else
                    percent = 0;
                tvItemChoice1.setTextColor(Color.RED);
                tvItemChoice2.setTextColor(Color.RED);
                tvItemChoice3.setTextColor(Color.GREEN);
                tvItemChoice4.setTextColor(Color.RED);
                tvItemChoice5.setTextColor(Color.RED);
                break;
            case 4:
                if(choice[3] > 0)
                    percent = (float)choice[3]/count*100;
                else
                    percent = 0;
                tvItemChoice1.setTextColor(Color.RED);
                tvItemChoice2.setTextColor(Color.RED);
                tvItemChoice3.setTextColor(Color.RED);
                tvItemChoice4.setTextColor(Color.GREEN);
                tvItemChoice5.setTextColor(Color.RED);
                break;
            case 5:
                if(choice[4] > 0)
                    percent = (float)choice[4]/count*100;
                else
                    percent = 0;
                tvItemChoice1.setTextColor(Color.RED);
                tvItemChoice2.setTextColor(Color.RED);
                tvItemChoice3.setTextColor(Color.RED);
                tvItemChoice4.setTextColor(Color.RED);
                tvItemChoice5.setTextColor(Color.GREEN);
                break;
            default:
                tvItemChoice1.setTextColor(Color.GRAY);
                tvItemChoice2.setTextColor(Color.GRAY);
                tvItemChoice3.setTextColor(Color.GRAY);
                tvItemChoice4.setTextColor(Color.GRAY);
                tvItemChoice5.setTextColor(Color.GRAY);
                break;
        }

        if (percent > 90)
            tvItemPercent.setTextColor(Color.rgb(0, 255, 0));
        else if (percent > 76)
            tvItemPercent.setTextColor(Color.rgb(100, 200, 150));
        else if (percent > 61)
            tvItemPercent.setTextColor(Color.rgb(150, 200, 100));
        else if (percent > 46)
            tvItemPercent.setTextColor(Color.rgb(160, 180, 150));
        else if (percent > 31)
            tvItemPercent.setTextColor(Color.rgb(200, 150, 0));
        else if (percent > 15)
            tvItemPercent.setTextColor(Color.rgb(200, 70, 30));
        else
            tvItemPercent.setTextColor(Color.rgb(255, 0, 0));

        tvItemNo.setText(String.valueOf(item[position][0]));
        tvItemPercent.setText(percent>-1?String.format("%.2f",percent)+" "+choice_thai[key]:"-");
        tvItemChoice1.setText(String.valueOf(choice[0]));
        tvItemChoice2.setText(String.valueOf(choice[1]));
        tvItemChoice3.setText(String.valueOf(choice[2]));
        tvItemChoice4.setText(String.valueOf(choice[3]));
        tvItemChoice5.setText(String.valueOf(choice[4]));

        return convertView;
    }
}
