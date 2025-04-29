/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric;

import java.util.ArrayList;
import java.util.Locale;

public class MetricCalculator {
    private ArrayList<Double> meanList = new ArrayList<>();
    private double maxValueSum = Double.MIN_VALUE;
    private double minValueSum = Double.MAX_VALUE;
    private final METRIC_TYPE metricType;

    private double median;
    private double mean;
    private double max;
    private double min;
    private double last;

    public MetricCalculator(METRIC_TYPE metricType) {
        this.metricType = metricType;
    }

    public METRIC_TYPE getMetricType() {
        return metricType;
    }

    public double getMedian() {
        return median;
    }

    public double getMean() {
        return mean;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getLast() {
        return last;
    }

    public void calcAll(){
        if(meanList.isEmpty()){
            return;
        }
        calcMin();
        calcMax();
        calcMedian();
        calcMean();
    }

    private void calcMean() {
        mean = meanList.stream().mapToDouble(a -> a).sum() / meanList.size();
    }

    private void calcMedian() {
        this.meanList.sort(Double::compareTo);
        median = meanList.get(Math.round(meanList.size() / 2));
    }

    private void calcMax() {
        max = meanList.stream().mapToDouble(a -> a).max().getAsDouble();
    }

    private void calcMin() {
        min = meanList.stream().mapToDouble(a -> a).min().getAsDouble();

    }

    public void update(Double value) {
        this.meanList.add(value);
        this.last = value;
    }

    public String getFormattedString(double value) {
        switch (this.metricType) {
            case THROUGHPUT:
                return String.format(Locale.getDefault(), "%.2f", value / 1e+6);
            case RTT:
            case PACKET_LOSS:
            case JITTER:
            case PING_RTT:
            case PING_PACKET_LOSS:
                return String.format(Locale.getDefault(), "%.2f", value);
        }
        return Double.toString(value);
    }

    public ArrayList<Double> getMeanList() {
        return meanList;
    }

    public void resetMetric() {
        meanList.clear();
        this.maxValueSum = Double.MIN_VALUE;
        this.minValueSum = Double.MAX_VALUE;
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

}
