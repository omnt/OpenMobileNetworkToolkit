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

      /*  this.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.logging_sp);
                Iperf3ResultsDataBase db = Iperf3ResultsDataBase.getDatabase(context);
                Iperf3RunResultDao iperf3RunResultDao = db.iperf3RunResultDao();
                if (preferences.getBoolean("enable_influx", false)) {
                } else {
                    Toast.makeText(context, "Influx Disabled!", Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<OneTimeWorkRequest> uploads = new ArrayList<>();
                WorkManager iperf3WM = WorkManager.getInstance(context);
                HashMap<String, Integer> cpySelectedRuns =
                    (HashMap<String, Integer>) selectedRuns.clone();
                for (Map.Entry<String, Integer> stringIntegerEntry : cpySelectedRuns.entrySet()) {
                    String uid = stringIntegerEntry.getKey();
                    Iperf3RunResult runResult = iperf3RunResultDao.getRunResult(uid);

                    Data.Builder data = new Data.Builder();
                    data.putString("iperf3LineProtocolFile", runResult.input.getParameter().getLineProtocolFile());
                    OneTimeWorkRequest iperf3UP =
                        new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class)
                            .setInputData(data.build())
                            .addTag("iperf3_upload")
                            .build();

                    uploads.add(iperf3UP);
                    iperf3WM.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
                        boolean iperf3_upload;
                        iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
                        if (iperf3_upload) {
                            //iperf3RunResultDao.updateUpload(uid, iperf3_upload);
                            selectedRuns.remove(stringIntegerEntry.getKey());
                            notifyItemChanged(stringIntegerEntry.getValue());
                        }

                    });

                }
                uploadBtn.setVisibility(View.INVISIBLE);
                iperf3WM.beginWith(uploads).enqueue();

            }
        });
*/
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


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        //        selectedUUID = test.uid;
        //        notifyDataSetChanged();
            }
        });
        if(Objects.equals(selectedUUID, test.uid)){
            holder.itemView.setBackgroundColor(Color.parseColor("#567845"));
        }
        String host = String.valueOf(test.input.getParameter().getHost());
        String dstPort = String.valueOf(test.input.getParameter().getPort());
        String bandwidth = String.valueOf(test.input.getParameter().getBitrate());
        String duration = String.valueOf(test.input.getParameter().getTime());
        String interval = String.valueOf(test.input.getParameter().getInterval());
        String bytes = String.valueOf(test.input.getParameter().getBytes());
        String streams = String.valueOf(test.input.getParameter().getParallel());
        String srcPort = String.valueOf(test.input.getParameter().getCport());

        String mode = String.valueOf(test.input.getParameter().getMode());
        String protocol = String.valueOf(test.input.getParameter().getProtocol());
        String direction = String.valueOf(test.input.getParameter().getDirection());

        ((TextView) holder.host.findViewById(R.id.text_parameter)).setText(host);
        ((TextView) holder.dstPort.findViewById(R.id.text_parameter)).setText(dstPort);
        ((TextView) holder.bandwidth.findViewById(R.id.text_parameter)).setText(bandwidth);
        ((TextView) holder.duration.findViewById(R.id.text_parameter)).setText(duration);
        ((TextView) holder.interval.findViewById(R.id.text_parameter)).setText(interval);
        ((TextView) holder.bytes.findViewById(R.id.text_parameter)).setText(bytes);
        ((TextView) holder.streams.findViewById(R.id.text_parameter)).setText(streams);
        ((TextView) holder.srcPort.findViewById(R.id.text_parameter)).setText(srcPort);

        ((TextView) holder.mode.findViewById(R.id.text_parameter)).setText(mode);
        ((TextView) holder.protocol.findViewById(R.id.text_parameter)).setText(protocol);
        ((TextView) holder.direction.findViewById(R.id.text_parameter)).setText(direction);
        holder.linearProgressIndicator.setIndicatorColor(Color.CYAN);
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
                break;
            case -100:
                holder.linearProgressIndicator.setMax(test.input.getParameter().getTime()-1);
                ArrayList<Interval> intervals = new ArrayList<Interval>();
                if(test.intervals != null)
                    intervals = test.intervals.getIntervalArrayList();
                int progress = intervals.size();
                holder.linearProgressIndicator.setProgress(progress);
                break;

        }

        if(!dstPort.isEmpty() || dstPort.equals("")){
            holder.dstPort.setVisibility(GONE);
        } else {
            holder.dstPort.setVisibility(VISIBLE);
        }
        if(!bandwidth.isEmpty() || bandwidth.equals("")) {
            holder.bandwidth.setVisibility(GONE);
        } else {
            holder.bandwidth.setVisibility(VISIBLE);
        }
        if(!duration.isEmpty() || duration.equals("")) {
            holder.duration.setVisibility(GONE);
        } else {
            holder.duration.setVisibility(VISIBLE);
        }
        if(!interval.isEmpty() || interval.equals("")){
            holder.interval.setVisibility(GONE);
        } else {
            holder.interval.setVisibility(VISIBLE);
        }
        if(!bytes.isEmpty() || bytes.equals("")) {
            holder.bytes.setVisibility(GONE);
        } else {
            holder.bytes.setVisibility(VISIBLE);
        }
        if(!streams.isEmpty() || streams.equals("")){
            holder.streams.setVisibility(GONE);
        } else {
            holder.streams.setVisibility(VISIBLE);
        }
        if(!srcPort.isEmpty() || srcPort.equals("")) {
            holder.srcPort.setVisibility(GONE);
        } else {
            holder.srcPort.setVisibility(VISIBLE);
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
        }

    }
    private Iperf3RunResult getItemByPosition(int position) {
        return iperf3RunResultDao.getRunResult(iperf3RunResultDao.getIDs().get(position));
    }

    @Override
    public int getItemCount() {
        return iperf3RunResultDao.getIDs().size();
    }

    private void setSelectedUUID(String selectedUUID) {
        this.selectedUUID = selectedUUID;

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
        private void setupParameterFlexBox(){
            parameterFlexBoxLayout = itemView.findViewById(R.id.parameter_iperf3_fl);

            host = (LinearLayout) li.inflate(R.layout.parameter_view, null);
            dstPort = (LinearLayout) li.inflate(R.layout.parameter_view, null);
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
//        gd.setStroke(2, 0xFF000000);
            cardViewError.setBackground(gd);



            cardViewError.addView(errorView);
            metricLL.addView(cardViewError);
            metricLL.addView(metricViewDL);
            metricLL.addView(metricViewUL);
            errorView.setVisibility(GONE);
        }

        public ViewHolder(View itemView) {
            super(itemView);
            li = LayoutInflater.from(context);
            itemView.setBackgroundColor(context.getColor(R.color.material_dynamic_tertiary30));
            setupLinearProgressIndicator();
            setupParameterFlexBox();
            setupMetricLinearLayout();
        }

    }
}
