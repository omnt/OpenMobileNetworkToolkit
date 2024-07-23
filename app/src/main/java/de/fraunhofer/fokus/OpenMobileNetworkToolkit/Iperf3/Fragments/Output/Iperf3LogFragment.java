/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Output;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Parser;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Utils;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.Metric;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3LogFragment extends Fragment {

    private static final String TAG = "Iperf3LogFragment";
    private View v;
    private Iperf3ResultsDataBase db;
    private Handler logHandler;
    private File file;
    private String uid;
    private Drawable runIcon;
    private Drawable uploadIcon;
    private ImageView runIconView;
    private ImageView uploadIconView;

    private TextView iperf3OutputViewer;
    private LinearLayout parameterLL;
    private Context ct;
    private LinearLayout metricLL;
    private Metric defaultReverseThroughput;
    private Metric defaultThroughput;
    private Metric defaultRTT;
    private Metric defaultJITTER;
    private Metric PACKET_LOSS;

    public Iperf3LogFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
    }

    private void setFields(Iperf3RunResult iperf3RunResult) {

    }
    private final Runnable logUpdate = new Runnable() {
        @Override
        public void run() {
            Iperf3RunResult iperf3RunResult = db.iperf3RunResultDao().getRunResult(uid);
            Log.d(TAG, "run: " + iperf3RunResult.result);
            runIcon = Iperf3Utils.getDrawableResult(requireContext(), iperf3RunResult.result);
            runIconView.setImageDrawable(runIcon);
            uploadIcon = Iperf3Utils.getDrawableUpload(ct, iperf3RunResult.result, iperf3RunResult.uploaded);
            uploadIconView.setImageDrawable(uploadIcon);

            BufferedReader br = null;
            StringBuilder text = new StringBuilder();

            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                iperf3OutputViewer.setText(String.format("no iPerf3 file found, with following path: \n %s", iperf3RunResult.input.iperf3rawIperf3file));
                logHandler.removeCallbacks(logUpdate);
                return;
            }
            String line;

            Iperf3Parser iperf3Parser = new Iperf3Parser(iperf3RunResult.input.iperf3rawIperf3file);
            iperf3Parser.addPropertyChangeListener(new PropertyChangeListener() {

                private void parseSum(Sum sum, Metric throughput){
                    SUM_TYPE sumType = sum.getSumType();
                    throughput.update(sum.getBits_per_second());
                    switch (sumType){
                        case UDP_DL:
                            defaultJITTER.update(((UDP_DL_SUM)sum).getJitter_ms());
                            PACKET_LOSS.update(((UDP_DL_SUM) sum).getLost_percent());
                        case TCP_DL:
                            if(throughput.getDirectionName().getText().equals("Throughput")){
                                throughput.getDirectionName().setText("Downlink Mbit/s");
                            }
                            break;
                        case UDP_UL:
                        case TCP_UL:
                            if(throughput.getDirectionName().getText().equals("Throughput")){
                                throughput.getDirectionName().setText("Uplink Mbit/s");
                            }
                            break;
                    }

                }
                public void propertyChange(PropertyChangeEvent evt) {

                    switch (evt.getPropertyName()){
                        case "interval":
                            Interval interval = (Interval) evt.getNewValue();
                            parseSum(interval.getSum(), defaultThroughput);
                            if(interval.getSumBidirReverse() != null) parseSum(interval.getSumBidirReverse(),
                                defaultReverseThroughput);
                            break;
                        case "start":
                            break;
                        case "end":
                            break;
                        case "error":
                            Error error = (Error) evt.getNewValue();
                            TextView errorView = new TextView(ct);
                            errorView.setText(error.getError());
                            errorView.setTextColor(ct.getColor(R.color.crimson));
                            errorView.setPadding(10, 10, 10, 10);
                            errorView.setTextSize(20);
                            metricLL.addView(errorView);
                            break;
                    }
                }
            });

            iperf3Parser.parse();
            if (iperf3RunResult.result != -100) {
                logHandler.removeCallbacks(logUpdate);
                return;
            }
            setFields(iperf3RunResult);
            logHandler.removeCallbacks(logUpdate);
            logHandler.postDelayed(this, 1000);
        }
    };




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iperf3_log, container, false);
        Iperf3RunResult iperf3RunResult =
            db.iperf3RunResultDao().getRunResult(this.getArguments().getString("uid"));
        ct = requireContext();
        LinearLayout mainLL = v.findViewById(R.id.iperf3_list_fragment);
        mainLL.setOrientation(LinearLayout.VERTICAL);

        LinearLayout firstRow = new LinearLayout(ct);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        mainLL.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams foo = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        foo.setMargins(30, 10, 0, 0);
        firstRow.setLayoutParams(foo);


        runIconView = new ImageView(ct);
        LinearLayout.LayoutParams runIconViewLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        runIconViewLayout.weight = 0.2F;
        runIconViewLayout.height = 100;
        runIconViewLayout.gravity = Gravity.RIGHT;
        runIconView.setLayoutParams(runIconViewLayout);
        runIcon = Iperf3Utils.getDrawableResult(ct, iperf3RunResult.result);
        runIconView.setImageDrawable(runIcon);
        runIconView.setTooltipText(String.format("Indicates the result of the iPerf3 run: %s", iperf3RunResult.result));
        runIconView.setLayoutParams(runIconViewLayout);


        uploadIconView = new ImageView(ct);
        uploadIconView.setTooltipText(String.format("Indicates if the iPerf3 run was uploaded to the server: %s", iperf3RunResult.uploaded));
        LinearLayout.LayoutParams uploadIconViewLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        uploadIconViewLayout.weight = 0.2F;
        uploadIconViewLayout.height = 100;
        uploadIconViewLayout.gravity = Gravity.RIGHT;
        uploadIcon = Iperf3Utils.getDrawableUpload(ct, iperf3RunResult.result, iperf3RunResult.uploaded);
        uploadIconView.setImageDrawable(uploadIcon);
        uploadIconView.setLayoutParams(uploadIconViewLayout);


        parameterLL = iperf3RunResult.input.getInputAsLinearLayoutKeyValue(new LinearLayout(ct), ct);



        firstRow.addView(parameterLL);




        LinearLayout headerWrapper = new LinearLayout(ct);
        LinearLayout.LayoutParams headerWrapperLayout = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        headerWrapper.setLayoutParams(headerWrapperLayout);
        headerWrapper.setOrientation(LinearLayout.VERTICAL);
        CardView cardView = new CardView(ct);
        LinearLayout.LayoutParams cardViewLayout = Iperf3Utils.getLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        0);
        headerWrapper.setPadding(30, 30, 30, 30);
        cardView.setLayoutParams(cardViewLayout);
        cardView.setRadius(10);
        cardView.setCardElevation(10);
        cardView.setUseCompatPadding(true);
        cardView.addView(headerWrapper);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implement click on card, goto input fragmet
            }
        });

        LinearLayout header = new LinearLayout(ct);
        LinearLayout.LayoutParams headerLayout = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 100);
        header.setLayoutParams(headerLayout);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView headerName = new TextView(ct);
        headerName.setText("iPerf3 run");
        LinearLayout.LayoutParams headerNameLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerNameLayout.weight = 0.6F;
        headerName.setLayoutParams(headerNameLayout);
        header.addView(headerName);
        header.addView(runIconView);
        header.addView(uploadIconView);
        ImageButton expandButton = new ImageButton(ct);
        expandButton.setImageResource(R.drawable.baseline_expand_more_24);
        firstRow.setVisibility(View.GONE);
        expandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (firstRow.getVisibility() == View.VISIBLE) {
                    firstRow.setVisibility(View.GONE);
                    expandButton.setImageResource(R.drawable.baseline_expand_more_24);
                } else {
                    firstRow.setVisibility(View.VISIBLE);
                    expandButton.setImageResource(R.drawable.baseline_expand_less_24);
                }

            }
        });
        LinearLayout.LayoutParams imageButtonLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageButtonLayout.weight = 0.2F;
        expandButton.setLayoutParams(imageButtonLayout);

        header.addView(expandButton);
        headerWrapper.addView(header);
        headerWrapper.addView(firstRow);

        uid = iperf3RunResult.uid;

        mainLL.addView(cardView);

        LinearLayout secondRow = new LinearLayout(ct);
        LinearLayout.LayoutParams secondRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);
        secondRowLayoutParams.setMargins(0, 20, 0, 20);
        secondRow.setLayoutParams(secondRowLayoutParams);
        ScrollView scrollView = new ScrollView(ct);
        iperf3OutputViewer = new TextView(ct);
        iperf3OutputViewer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(iperf3OutputViewer,
            1, 10, 1,
            TypedValue.COMPLEX_UNIT_SP);
        iperf3OutputViewer.setTextIsSelectable(true);

        metricLL = new LinearLayout(ct);
        metricLL.setOrientation(LinearLayout.VERTICAL);
        metricLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));



        defaultThroughput = new Metric(METRIC_TYPE.THROUGHPUT, ct);
        defaultReverseThroughput = new Metric(METRIC_TYPE.THROUGHPUT, ct);

        metricLL.addView(defaultThroughput.createMainLL("Throughput"));

        if(iperf3RunResult.input.iperf3BiDir) {
            metricLL.addView(defaultReverseThroughput.createMainLL("Throughput"));
            if(iperf3RunResult.input.iperf3IdxProtocol == 0) {
                //defaultRTT = new Metric(METRIC_TYPE.RTT);
                //metricLL.addView(defaultRTT.createOneDirection("RTT"));
            }
            if(iperf3RunResult.input.iperf3IdxProtocol == 1) {
                defaultJITTER = new Metric(METRIC_TYPE.JITTER, ct);
                metricLL.addView(defaultJITTER.createMainLL("Jitter ms"));
                PACKET_LOSS = new Metric(METRIC_TYPE.PACKET_LOSS, ct);
                metricLL.addView(PACKET_LOSS.createMainLL("Packet Loss %"));
            }
        }
        if(iperf3RunResult.input.iperf3Reverse) {
            if(iperf3RunResult.input.iperf3IdxProtocol == 1) {
                defaultJITTER = new Metric(METRIC_TYPE.JITTER, ct);
                metricLL.addView(defaultJITTER.createMainLL("Jitter ms"));
                PACKET_LOSS = new Metric(METRIC_TYPE.JITTER, ct);
                metricLL.addView(PACKET_LOSS.createMainLL("Packet Loss %"));
            }
        } else if(!iperf3RunResult.input.iperf3BiDir) {
            if(iperf3RunResult.input.iperf3IdxProtocol == 0) {
                //defaultRTT = new Metric(METRIC_TYPE.RTT);
                //metricLL.addView(defaultRTT.createOneDirection("RTT ms"));
            }
        }

        mainLL.addView(metricLL);




        mainLL.addView(secondRow);
        if(iperf3RunResult.input.iperf3rawIperf3file == null){
            iperf3OutputViewer.setText("iPerf3 file path empty!");
            return v;
        }
        file = new File(iperf3RunResult.input.iperf3rawIperf3file);


        logHandler = new Handler(Looper.myLooper());
        logHandler.post(logUpdate);
        return v;
    }

    public void onPause() {
        super.onPause();
        if(logHandler != null) logHandler.removeCallbacks(logUpdate);
    }


}