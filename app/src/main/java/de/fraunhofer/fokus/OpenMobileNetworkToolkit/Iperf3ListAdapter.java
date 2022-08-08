package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

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

        iperf3Runner iperf3R = new iperf3Runner(null, null, null).readBytes(this.iperf3DBHandler.getRunnerByID(ids[position]));

        convertView = inflater.inflate(R.layout.activity_iperf3_row_item, null);
        TextView command = (TextView) convertView.findViewById(R.id.firstLine);
        TextView runnerID = (TextView) convertView.findViewById(R.id.secondLine);
        TextView timestamp = (TextView) convertView.findViewById(R.id.editTextDate);

        command.setText(iperf3R.getCommand());
        runnerID.setText(iperf3R.getId());
        timestamp.setText(iperf3R.getTimestamp());

        return convertView;
    }
}
