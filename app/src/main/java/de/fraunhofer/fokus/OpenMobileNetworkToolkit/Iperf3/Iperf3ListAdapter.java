package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    Iperf3ResultsDataBase db;
    ArrayList<String> uids;

    public void setUids(ArrayList<String> uids) {
        this.uids = uids;
    }


    public Iperf3ListAdapter(Context context, ArrayList<String> uids) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.db = Iperf3ResultsDataBase.getDatabase(context);
        this.uids = uids;
    }



    @Override
    public int getCount() {
        return this.uids.size();
    }

    @Override
    public Object getItem(int position) {
        return this.db.iperf3RunResultDao().getRunResult(this.uids.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.fragment_iperf3_row_item, null);
        TextView command = (TextView) convertView.findViewById(R.id.firstLine);
        TextView runnerID = (TextView) convertView.findViewById(R.id.secondLine);
        TextView timestamp = (TextView) convertView.findViewById(R.id.thirdLine);
        TextView iperf3State = (TextView) convertView.findViewById(R.id.iperf3State);
      //  ImageView icon = (ImageView) convertView.findViewById(R.id.runningIndicator);

        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline, null);

        Iperf3RunResult test = (Iperf3RunResult) getItem(position);

        command.setText("Measurement: "+test.input.measurementName);
        iperf3State.setText(""+test.result);
        timestamp.setText("Moved: "+ test.moved);
        runnerID.setText("Uploaded: "+ test.uploaded);
/*
        switch (tmp..getThreadState()){
            case "NEW":
                break;
            case "RUNNABLE":
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_directions_run, null);
                break;
            case "BLOCKED":
                break;
            case "WAITING":
                break;
            case "WATING":
                break;
            case "TIMED_WAITING":
                break;
            case "TERMINATED":
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_all, null);
                iperf3State.setText(Integer.toString(iperf3R.getIperf3State()));
                break;
            default:
        }
*/
        //icon.setImageDrawable(drawable);
        return convertView;
    }
}
