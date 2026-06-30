/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricView;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3RecyclerViewAdapter
    extends RecyclerView.Adapter<Iperf3RecyclerViewAdapter.ViewHolder> {
    private final String TAG = "Iperf3RecyclerViewAdapter";
    private final Iperf3ResultsDataBase db;
    private Context context;
    private final HashMap<String, Integer> selectedRuns;
    private final HashMap<CardView, Boolean> selectedCardViews;
    private final FloatingActionButton uploadBtn;
    private Iperf3RunResultDao iperf3RunResultDao;
    private Observer observer;
    private String selectedUUID;
    public Iperf3RecyclerViewAdapter(FloatingActionButton uploadBtn) {

        this.db = Iperf3ResultsDataBase.getDatabase(context);
        this.iperf3RunResultDao = db.iperf3RunResultDao();

        this.selectedRuns = new HashMap<>();
        this.selectedCardViews = new HashMap<>();
        this.uploadBtn = uploadBtn;
    }


    @NonNull
    @Override
    public Iperf3RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                   int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        MaterialCardView v = (MaterialCardView) inflater.inflate(R.layout.fragment_iperf3_row_item, parent, false);
        v.setFocusable(false);
        v.setClickable(false);
        ViewHolder viewHolder = new ViewHolder(v);
        selectedCardViews.put(v, false);
        return viewHolder;
    }

    public String getSelectedUUID(){
        return selectedUUID;
    }

    public boolean isNightMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Iperf3RunResult test = getItemByPosition(position);
        holder.metricViewDL.setVisibility(GONE);
        holder.metricViewUL.setVisibility(GONE);

        holder.metricViewUL.setMetricCalculator(test.metricUL == null ? new MetricCalculator(METRIC_TYPE.THROUGHPUT) : test.metricUL);
        holder.metricViewDL.setMetricCalculator(test.metricDL == null ? new MetricCalculator(METRIC_TYPE.THROUGHPUT) : test.metricDL);

        holder.itemView.setTag(iperf3RunResultDao.getIDs().get(position));

        String host = test.input.getParameter().getHost();
        int dstPort = test.input.getParameter().getPort();
        String bandwidth = test.input.getParameter().getBitrate();
        int duration = test.input.getParameter().getTime();
        double interval = test.input.getParameter().getInterval();
        String bytes = test.input.getParameter().getBytes();
        int streams = test.input.getParameter().getParallel() == null ? 0 : test.input.getParameter().getParallel();
        int srcPort = test.input.getParameter().getCport() == null ? 0 : test.input.getParameter().getCport();

        String mode = String.valueOf(test.input.getParameter().getMode());
        String protocol = String.valueOf(test.input.getParameter().getProtocol());
        String direction = String.valueOf(test.input.getParameter().getDirection());

        updateParam(holder.host, "HOST", host);
        updateParam(holder.dstPort, "PORT", dstPort == 0 ? null : String.valueOf(dstPort));
        updateParam(holder.bandwidth, "BW", (bandwidth == null || bandwidth.isEmpty() || bandwidth.equals("null")) ? null : bandwidth + "M");
        updateParam(holder.duration, "TIME", duration == 0 ? null : duration + "s");
        updateParam(holder.interval, "INT", interval == 0.0 ? null : interval + "s");
        updateParam(holder.bytes, "LIMIT", (bytes == null || bytes.isEmpty() || bytes.equals("null")) ? null : bytes);
        updateParam(holder.streams, "STRM", streams == 0 ? null : String.valueOf(streams));
        updateParam(holder.srcPort, "CPORT", srcPort == 0 ? null : String.valueOf(srcPort));

        updateParam(holder.mode, "MODE", mode);
        updateParam(holder.protocol, "PROTO", protocol);
        updateParam(holder.direction, "DIR", direction);
        
        holder.statusBadge.setVisibility(GONE);

        switch (test.result){
            case 0:
                holder.linearProgressIndicator.setIndicatorColor(Color.GREEN);
                holder.linearProgressIndicator.setProgress(1);
                holder.linearProgressIndicator.setMax(1);
                break;
            case -1:
                holder.linearProgressIndicator.setMax(1);
                holder.linearProgressIndicator.setProgress(1);
                holder.linearProgressIndicator.setIndicatorColor(Color.RED);
                holder.statusBadge.setVisibility(VISIBLE);
                holder.statusBadge.setText("FAILED");
                holder.statusBadge.setTextColor(Color.RED);
                break;
            case -100:
                holder.linearProgressIndicator.setIndicatorColor(Color.CYAN);
                
                // Calculate max intervals based on duration and interval setting
                int totalExpectedIntervals = (int) Math.ceil((double)test.input.getParameter().getTime() / test.input.getParameter().getInterval());
                holder.linearProgressIndicator.setMax(totalExpectedIntervals);
                
                ArrayList<Interval> intervals = new ArrayList<Interval>();
                if(test.intervals != null)
                    intervals = test.intervals.getIntervalArrayList();
                int progress = intervals.size();
                holder.linearProgressIndicator.setProgress(progress);

                holder.statusBadge.setVisibility(VISIBLE);
                holder.statusBadge.setText("RUNNING");
                holder.statusBadge.setTextColor(context.getColor(R.color.purple_500));
                break;

        }

        switch (test.input.getParameter().getDirection()){
            case BIDIR:
                holder.metricViewUL.setVisibility(VISIBLE);
                holder.metricViewDL.setVisibility(VISIBLE);
                if(holder.metricViewUL.getMetricCalculator() != null || holder.metricViewDL.getMetricCalculator() != null){
                    holder.metricViewUL.getMetricCalculator().calcAll();
                    holder.metricViewDL.getMetricCalculator().calcAll();
                    holder.metricViewUL.update();
                    holder.metricViewDL.update();
                }
                break;
            case UP:
                holder.metricViewUL.setVisibility(VISIBLE);
                holder.metricViewDL.setVisibility(GONE);
                if(holder.metricViewUL.getMetricCalculator() != null){
                    holder.metricViewUL.getMetricCalculator().calcAll();
                    holder.metricViewUL.update();
                }
                break;
            case DOWN:
                holder.metricViewUL.setVisibility(GONE);
                holder.metricViewDL.setVisibility(VISIBLE);
                if(holder.metricViewDL.getMetricCalculator() != null){
                    holder.metricViewDL.getMetricCalculator().calcAll();
                    holder.metricViewDL.update();
                }
                break;
        }
        holder.errorView.setVisibility(GONE);
        holder.cardViewError.setVisibility(GONE);
        if(test.result == -1) {
            holder.metricViewUL.setVisibility(GONE);
            holder.metricViewDL.setVisibility(GONE);
            String errorText = "Error!";
            test = iperf3RunResultDao.getRunResult(test.uid);
            if(test.error != null){
                errorText = test.error.getError();
            }
            holder.errorView.setText(errorText);
            holder.errorView.setVisibility(VISIBLE);
            holder.cardViewError.setVisibility(VISIBLE);
        }

    }

    private void updateParam(LinearLayout container, String label, String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null") || value.equals("0") || value.equals("0s") || value.equals("0.0s")) {
            container.setVisibility(GONE);
        } else {
            container.setVisibility(VISIBLE);
            ((TextView) container.findViewById(R.id.parameter_label)).setText(label);
            ((TextView) container.findViewById(R.id.text_parameter)).setText(value);
        }
    }

    private Iperf3RunResult getItemByPosition(int position) {
        return iperf3RunResultDao.getRunResult(iperf3RunResultDao.getIDs().get(position));
    }

    @Override
    public int getItemCount() {
        return iperf3RunResultDao.getIDs().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FlexboxLayout parameterFlexBoxLayout;
        private LinearLayout host;
        private LinearLayout dstPort;
        private LinearLayout bandwidth;
        private LinearLayout duration;
        private LinearLayout interval;
        private LinearLayout bytes;
        private LinearLayout streams;
        private LinearLayout srcPort;
        private LinearLayout mode;
        private LinearLayout protocol;
        private LinearLayout direction;
        private LayoutInflater li;
        private LinearProgressIndicator linearProgressIndicator;
        private MetricView metricViewDL;
        private MetricView metricViewUL;
        private TextView errorView;
        private LinearLayout metricLL;
        private CardView cardViewError;
        private MaterialButton cancel;
        private MaterialButton rerun;
        private TextView statusBadge;

        private void setupParameterFlexBox(){
            parameterFlexBoxLayout = itemView.findViewById(R.id.parameter_iperf3_fl);

            host = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            dstPort = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            bandwidth = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            duration = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            interval = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            bytes = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            streams = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            srcPort = (LinearLayout) li.inflate(R.layout.parameter_view, null);

            mode = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            protocol = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            direction = (LinearLayout) li.inflate(R.layout.parameter_view, null);

            parameterFlexBoxLayout.addView(host);
            parameterFlexBoxLayout.addView(dstPort);
            parameterFlexBoxLayout.addView(bandwidth);
            parameterFlexBoxLayout.addView(duration);
            parameterFlexBoxLayout.addView(interval);
            parameterFlexBoxLayout.addView(bytes);
            parameterFlexBoxLayout.addView(streams);
            parameterFlexBoxLayout.addView(srcPort);
            parameterFlexBoxLayout.addView(mode);
            parameterFlexBoxLayout.addView(protocol);
            parameterFlexBoxLayout.addView(direction);

        }
        public void setupLinearProgressIndicator(){
            linearProgressIndicator = itemView.findViewById(R.id.progress_indicator);
        }
        public void setupMetricLinearLayout(){
            metricLL = itemView.findViewById(R.id.metrics_iperf3_ll);
            metricViewDL = new MetricView(context);
            metricViewUL = new MetricView(context);
            metricViewDL.setup("Download [Mbit/s]");
            metricViewUL.setup("Upload [Mbit/s]");

            errorView = new TextView(context);
            errorView.setTextColor(context.getColor(R.color.material_dynamic_neutral0));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            errorView.setLayoutParams(params);

            cardViewError = new CardView(context);
            LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardViewParams.setMargins(10, 10, 10, 10);
            errorView.setPadding(10, 10, 10, 10);
            cardViewError.setLayoutParams(cardViewParams);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(context.getColor( R.color.material_dynamic_primary100));
            gd.setCornerRadius(10);
            cardViewError.setBackground(gd);

            cardViewError.addView(errorView);
            metricLL.addView(cardViewError);
            metricLL.addView(metricViewDL);
            metricLL.addView(metricViewUL);
            errorView.setVisibility(GONE);
            cardViewError.setVisibility(GONE);
        }

        public ViewHolder(View itemView) {
            super(itemView);
            li = LayoutInflater.from(context);
            statusBadge = itemView.findViewById(R.id.iperf3_status_badge);
            setupLinearProgressIndicator();
            setupParameterFlexBox();
            setupMetricLinearLayout();
        }

    }
}
