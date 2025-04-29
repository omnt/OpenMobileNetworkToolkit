/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;

import static de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE.*;

import com.google.gson.Gson;
import com.influxdb.client.JSON;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.STREAM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Streams;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_BIDIR_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_UL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_BIDIR_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_UL_SUM;
import org.json.JSONException;
import org.json.JSONObject;

public class Interval {
    private final Streams streams;
    private Sum sum;
    public Sum sumBidirReverse;

    public Interval(){
        streams = new Streams();
    }

    private SUM_TYPE getSumType(JSONObject data) throws JSONException {
        boolean sender = data.getBoolean("sender");
        if(sender){
            if(data.has("retransmits")) return SUM_TYPE.TCP_UL;
            if(data.has("packets")) return SUM_TYPE.UDP_UL;
        }
        if(data.has("jitter_ms")) return SUM_TYPE.UDP_DL;
        return SUM_TYPE.TCP_DL;
    }
    public String getNotificationString(){
        StringBuilder notificationString = new StringBuilder();
        notificationString.append("Throughput: ");
        notificationString.append(Math.round(this.getSum().getBits_per_second() / 1e6));
        notificationString.append(" Mbit/s");
        notificationString.append("\n");
        notificationString.append("Direction: ");
        notificationString.append(this.getSum().getSumType());
        notificationString.append("\n");
        if(sumBidirReverse != null){
            notificationString.append("--------------------\n");
            notificationString.append("Throughput: ");
            notificationString.append(Math.round(this.getSumBidirReverse().getBits_per_second() / 1e6));
            notificationString.append(" Mbit/s");
            notificationString.append("\n");
            notificationString.append("Direction: ");
            notificationString.append(this.getSumBidirReverse().getSumType());
            notificationString.append("\n");
        }
        return notificationString.toString();
    }
    public Sum identifySum(JSONObject data) throws JSONException {
        Sum identifiedSum = null;
        switch (getSumType(data)){
            case TCP_DL:
                identifiedSum = new TCP_DL_SUM();
                identifiedSum.parse(data);
                break;
            case TCP_UL:
                identifiedSum = new TCP_UL_SUM();
                identifiedSum.parse(data);
                break;
            case UDP_DL:
                identifiedSum = new UDP_DL_SUM();
                identifiedSum.parse(data);
                break;
            case UDP_UL:
                identifiedSum = new UDP_UL_SUM();
                identifiedSum.parse(data);
                break;
        }
        return identifiedSum;
    }

    public void parse(JSONObject data) throws JSONException {
        streams.parse(data.getJSONArray("streams"));
        sum = identifySum(data.getJSONObject("sum"));
        if(data.has("sum_bidir_reverse")){
            sumBidirReverse = identifySum(data.getJSONObject("sum_bidir_reverse"));
        }
    }

    public Streams getStreams() {
        return streams;
    }
    public Sum getSum() {
        return sum;
    }
    public Sum getSumBidirReverse() {
        return sumBidirReverse;
    }

    public String toString(){
        return new Gson().toJson(this);
    }
    public Interval(String line){
        this.streams = new Streams();
        Interval interval = new Gson().fromJson(line, Interval.class);
        this.sum = interval.sum;
        this.sumBidirReverse = interval.sumBidirReverse;
    }


}
