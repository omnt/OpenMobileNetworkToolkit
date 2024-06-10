/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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

import com.github.anastr.speedviewlib.SpeedView;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_UL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_DL_SUM;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

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
    private SpeedView speedView;
    private LinearLayout parameterLL;
    private Context ct;
    private LinearLayout metricLL;
    private Metric defaultReverseThroughput;
    private Metric defaultThroughput;
    private Metric defaultRTT;
    private Metric defaultJITTER;
    private Metric PACKET_LOSS;
    public class Metric {
        private LinearLayout mean;
        private LinearLayout median;
        private LinearLayout max;
        private LinearLayout min;
        private LinearLayout last;
        private TextView directionName;
        private ArrayList<Double> meanList = new ArrayList<>();
        private double maxValueSum = Double.MIN_VALUE;
        private double minValueSum = Double.MAX_VALUE;
        private METRIC_TYPE metricType;
        public Metric(METRIC_TYPE metricType){
            this.metricType = metricType;
        }

        private LinearLayout createTile(String key) {
            LinearLayout ll = new LinearLayout(ct);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ct.getColor(R.color.cardview_dark_background));
            gd.setCornerRadius(10);
            gd.setStroke(2, 0xFF000000);
            ll.setBackground(gd);
            ll.setMinimumHeight(ll.getWidth());
            ll.setGravity(Gravity.CENTER);

            ll.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams foo = new LinearLayout.LayoutParams(200, 150);
            foo.weight = 1;
            foo.setMargins(10, 10, 10, 10);
            ll.setLayoutParams(foo);
            TextView keyView = new TextView(ct);
            keyView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams keyViewLayoutParams = new LinearLayout.LayoutParams(200, 50);
            keyViewLayoutParams.setMargins(0, 0, 0, 10);
            keyView.setLayoutParams(keyViewLayoutParams);
            keyView.setTypeface(null, Typeface.BOLD);

            keyView.setText(key);
            ll.addView(keyView);
            TextView valueView = new TextView(ct);
            valueView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams valueViewLayoutParams = new LinearLayout.LayoutParams(200, 50);
            valueViewLayoutParams.setMargins(0, 0, 0, 0);
            valueView.setLayoutParams(valueViewLayoutParams);
            ll.addView(valueView);
            return ll;
        }

        private LinearLayout createLL(String key) {
            LinearLayout ll = null;
            switch (key) {
                case "mean":
                    mean = createTile(key);
                    ll = mean;
                    break;
                case "median":
                    median = createTile(key);
                    ll = median;
                    break;
                case "max":
                    max = createTile(key);
                    ll = max;
                    break;
                case "min":
                    min = createTile(key);
                    ll = min;
                    break;
                case "last":
                    last = createTile(key);
                    ll = last;
                    break;
            }
            return ll;
        }
        private String getFormatedString(double value){
            switch (this.metricType){
                case THROUGHPUT:
                    return String.format(Locale.getDefault(), "%.2f", value/1e+6);
                case RTT:
                case PACKET_LOSS:
                case JITTER:
                    return String.format(Locale.getDefault(), "%.2f", value);
            }
            return Double.toString(value);
        }
        private LinearLayout createOneDirection(String direction) {
            LinearLayout oneDirection = new LinearLayout(ct);
            LinearLayout.LayoutParams foo1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            oneDirection.setOrientation(LinearLayout.VERTICAL);
            oneDirection.setLayoutParams(foo1);

            directionName = new TextView(ct);
            directionName.setText(direction);
            oneDirection.addView(directionName);

            LinearLayout cardViewResult = new LinearLayout(ct);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardViewResult.setOrientation(LinearLayout.HORIZONTAL);
            cardViewResult.setLayoutParams(cardParams);

            cardViewResult.addView(createLL("mean"));
            cardViewResult.addView(createLL("median"));
            cardViewResult.addView(createLL("max"));
            cardViewResult.addView(createLL("min"));
            cardViewResult.addView(createLL("last"));
            oneDirection.addView(cardViewResult);
            return oneDirection;
        }

        public double calcMean(){
            return meanList.stream().mapToDouble(a -> a).sum()/meanList.size();
        }

        public double calcMedian(){
            this.getMeanList().sort(Double::compareTo);
            return meanList.get(Math.round(meanList.size()/2));
        }

        public double calcMax(){
            return meanList.stream().mapToDouble(a -> a).max().getAsDouble();
        }

        public double calcMin(){
            return meanList.stream().mapToDouble(a -> a).min().getAsDouble();
        }
        public void update(Double value){
            this.meanList.add(value);

            ((TextView)mean.getChildAt(1)).setText(String.format(" %s", getFormatedString(calcMean())));
            ((TextView)median.getChildAt(1)).setText(String.format(" %s", getFormatedString(calcMedian())));
            ((TextView)max.getChildAt(1)).setText(String.format(" %s", getFormatedString(calcMax())));
            ((TextView)min.getChildAt(1)).setText(String.format(" %s", getFormatedString(calcMin())));
            ((TextView)last.getChildAt(1)).setText(String.format(" %s", getFormatedString(meanList.get(meanList.size()-1))));
        }

        public ArrayList<Double> getMeanList() {
            return meanList;
        }
        public void setMaxValueSum(double maxValueSum) {
            this.maxValueSum = maxValueSum;
        }
        public void setMinValueSum(double minValueSum) {
            this.minValueSum = minValueSum;
        }
        public double getMaxValueSum() {
            return maxValueSum;
        }
        public double getMinValueSum() {
            return minValueSum;
        }

        public void setMeanList(ArrayList<Double> meanList) {
            this.meanList = meanList;
        }

        public LinearLayout getMean() {
            return mean;
        }

        public void setMean(LinearLayout mean) {
            this.mean = mean;
        }

        public LinearLayout getMedian() {
            return median;
        }

        public void setMedian(LinearLayout median) {
            this.median = median;
        }

        public LinearLayout getMax() {
            return max;
        }

        public void setMax(LinearLayout max) {
            this.max = max;
        }

        public LinearLayout getMin() {
            return min;
        }

        public void setMin(LinearLayout min) {
            this.min = min;
        }

        public LinearLayout getLast() {
            return last;
        }

        public void setLast(LinearLayout last) {
            this.last = last;
        }

        public TextView getDirectionName() {
            return directionName;
        }

        public void setDirectionName(TextView directionName) {
            this.directionName = directionName;
        }
    }

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
                            PACKET_LOSS.update((double) ((UDP_DL_SUM) sum).getLost_percent());
                        case TCP_DL:
                            if(throughput.directionName.getText().equals("Throughput")){
                                throughput.directionName.setText("Downlink Mbit/s");
                            }
                            break;
                        case UDP_UL:
                        case TCP_UL:
                            if(throughput.directionName.getText().equals("Throughput")){
                                throughput.directionName.setText("Uplink Mbit/s");
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



        defaultThroughput = new Metric(METRIC_TYPE.THROUGHPUT);
        defaultReverseThroughput = new Metric(METRIC_TYPE.THROUGHPUT);

        metricLL.addView(defaultThroughput.createOneDirection("Throughput"));

        if(iperf3RunResult.input.iperf3BiDir) {
            metricLL.addView(defaultReverseThroughput.createOneDirection("Throughput"));
            if(iperf3RunResult.input.iperf3IdxProtocol == 0) {
                //defaultRTT = new Metric(METRIC_TYPE.RTT);
                //metricLL.addView(defaultRTT.createOneDirection("RTT"));
            };
            if(iperf3RunResult.input.iperf3IdxProtocol == 1) {
                defaultJITTER = new Metric(METRIC_TYPE.JITTER);
                metricLL.addView(defaultJITTER.createOneDirection("Jitter ms"));
                PACKET_LOSS = new Metric(METRIC_TYPE.PACKET_LOSS);
                metricLL.addView(PACKET_LOSS.createOneDirection("Packet Loss %"));
            };
        };
        if(iperf3RunResult.input.iperf3Reverse) {
            if(iperf3RunResult.input.iperf3IdxProtocol == 1) {
                defaultJITTER = new Metric(METRIC_TYPE.JITTER);
                metricLL.addView(defaultJITTER.createOneDirection("Jitter ms"));
                PACKET_LOSS = new Metric(METRIC_TYPE.JITTER);
                metricLL.addView(PACKET_LOSS.createOneDirection("Packet Loss %"));
            };
        } else if(!iperf3RunResult.input.iperf3BiDir) {
            if(iperf3RunResult.input.iperf3IdxProtocol == 0) {
                //defaultRTT = new Metric(METRIC_TYPE.RTT);
                //metricLL.addView(defaultRTT.createOneDirection("RTT ms"));
            };
        }

        mainLL.addView(metricLL);




        mainLL.addView(secondRow);
        if(iperf3RunResult.input.iperf3rawIperf3file == null){
            iperf3OutputViewer.setText(String.format("iPerf3 file path empty!"));
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