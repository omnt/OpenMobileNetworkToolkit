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

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    Iperf3DBHandler iperf3DBHandler;
    String[] ids;

    public Iperf3ListAdapter(Context context, String[] ids) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.iperf3DBHandler = Iperf3DBHandler.getInstance(context);
        this.ids = ids;
    }

    @Override
    public int getCount() {
        return this.ids.length;
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

        Iperf3Runner iperf3R = new Iperf3Runner(null, null, null, null, null).readBytes(this.iperf3DBHandler.getRunnerByID(ids[position]));

        convertView = inflater.inflate(R.layout.fragment_iperf3_row_item, null);
        TextView command = (TextView) convertView.findViewById(R.id.firstLine);
        TextView runnerID = (TextView) convertView.findViewById(R.id.secondLine);
        TextView timestamp = (TextView) convertView.findViewById(R.id.thirdLine);
        ImageView icon = (ImageView) convertView.findViewById(R.id.runningIndicator);

        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline, null);;

        switch (iperf3R.getState()){
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
                break;
            default:
        }

        icon.setImageDrawable(drawable);

        command.setText(iperf3R.getCommand());
        runnerID.setText(iperf3R.getId());
        timestamp.setText(iperf3R.getTimestamp());
        return convertView;
    }
}
