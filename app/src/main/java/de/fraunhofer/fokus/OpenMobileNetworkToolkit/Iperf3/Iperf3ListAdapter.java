package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.LinkedList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    Iperf3DBHandler iperf3DBHandler;
    Iperf3Fragment.ListElem[] listElems;

    public Iperf3ListAdapter(Context context, Iperf3Fragment.ListElem[] elements) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.iperf3DBHandler = Iperf3DBHandler.getInstance(context);
        this.listElems = elements;
    }

    @Override
    public int getCount() {
        return this.listElems.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Iperf3Fragment.ListElem tmp = this.listElems[position];


        convertView = inflater.inflate(R.layout.fragment_iperf3_row_item, null);
        TextView command = (TextView) convertView.findViewById(R.id.firstLine);
        TextView runnerID = (TextView) convertView.findViewById(R.id.secondLine);
        TextView timestamp = (TextView) convertView.findViewById(R.id.thirdLine);
        TextView iperf3State = (TextView) convertView.findViewById(R.id.iperf3State);
        ImageView icon = (ImageView) convertView.findViewById(R.id.runningIndicator);

        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline, null);


        command.setText("Measurement: "+tmp.input.getMeasurementName());
        iperf3State.setText("Result:"+ tmp.result);
        timestamp.setText("Moved: "+ !tmp.moved);
        runnerID.setText("Uploaded: "+ !tmp.uploaded);
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
        icon.setImageDrawable(drawable);
        return convertView;
    }
}
