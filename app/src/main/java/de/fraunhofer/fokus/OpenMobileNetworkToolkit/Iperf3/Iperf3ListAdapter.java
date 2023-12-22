/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    Iperf3ResultsDataBase db;
    ArrayList<String> uids;

    public Iperf3ListAdapter(Context context, ArrayList<String> uids) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.db = Iperf3ResultsDataBase.getDatabase(context);
        this.uids = uids;
    }

    public void setUids(ArrayList<String> uids) {
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
        return (long) position;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.fragment_iperf3_row_item, null);
        TextView command = convertView.findViewById(R.id.firstLine);
        TextView uploaded = convertView.findViewById(R.id.secondLine);
        TextView timestamp = convertView.findViewById(R.id.thirdLine);
        TextView iperf3State = convertView.findViewById(R.id.iperf3State);
        ImageView icon = convertView.findViewById(R.id.iperf3RunningIndicator);

        Drawable drawable =
            ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline, null);

        Iperf3RunResult test = (Iperf3RunResult) getItem(position);

        command.setText(test.input.measurementName);
        iperf3State.setText("" + test.result);
        if (test.result == -100) {
            iperf3State.setText("RUN");
        }
        timestamp.setText(test.input.timestamp.toString());
        uploaded.setText("Uploaded: " + test.uploaded);

        drawable = Iperf3Utils.getDrawable(context, test.result);
        icon.setImageDrawable(drawable);
        return convertView;
    }
}
