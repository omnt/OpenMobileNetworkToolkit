package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

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
        private final METRIC_TYPE metricType;
        private final Context ct;
        private LinearLayout mainLL;

        public Metric(METRIC_TYPE metricType, Context ct){
                this.metricType = metricType;
                this.ct = ct;
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
                case PING_RTT:
                case PING_PACKET_LOSS:
                    return String.format(Locale.getDefault(), "%.2f", value);
            }
            return Double.toString(value);
        }
        public LinearLayout createMainLL(String direction) {
            mainLL = new LinearLayout(ct);
            LinearLayout.LayoutParams foo1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            mainLL.setOrientation(LinearLayout.VERTICAL);
            mainLL.setLayoutParams(foo1);

            directionName = new TextView(ct);
            directionName.setText(direction);
            mainLL.addView(directionName);

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
            mainLL.addView(cardViewResult);
            return mainLL;
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
            ((TextView)last.getChildAt(1)).setText(String.format(" %s", getFormatedString(value)));
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

        public void resetMetric(){
            meanList.clear();
            this.maxValueSum = Double.MIN_VALUE;
            this.minValueSum = Double.MAX_VALUE;
        }
        public void setVisibility(int visibility){
            mainLL.setVisibility(visibility);
        }
    }
