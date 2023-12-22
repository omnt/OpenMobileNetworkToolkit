/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Root;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream__1;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;

public class Iperf3UploadWorker extends Worker {
    private static final String TAG = "Iperf3UploadWorker";
    InfluxdbConnection influx;
    private SharedPreferences sp;
    private String logFilePath;
    private String measurementName;
    private String ip;

    private String port;
    private String bandwidth;
    private String duration;
    private String intervalIperf;
    private String bytes;
    private String protocol;

    private DeviceInformation di = new DeviceInformation();


    private boolean rev;
    private boolean biDir;
    private boolean oneOff;
    private boolean client;

    public Iperf3UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        logFilePath = getInputData().getString("logfilepath");

        ip = getInputData().getString("ip");
        measurementName = getInputData().getString("measurementName");

        port = getInputData().getString("port");
        if(port == null)
            port = "5201";
        protocol = getInputData().getString("protocol");
        bandwidth = getInputData().getString("bandwidth");

        if(bandwidth == null){
            if(protocol.equals("TCP")) {
                bandwidth = "unlimited";
            } else {
                bandwidth = "1000";
            }
        }

        duration = getInputData().getString("duration");
        if(duration == null)
            duration = "10";
        intervalIperf = getInputData().getString("interval");
        if(intervalIperf == null)
            intervalIperf = "1";
        bytes = getInputData().getString("bytes");
        if(bytes == null){
            if(protocol.equals("TCP")) {
                bytes = "8";
            } else {
                bytes = "1470";
            }
        }

        rev = getInputData().getBoolean("rev", false);
        biDir = getInputData().getBoolean("biDir",false);
        oneOff = getInputData().getBoolean("oneOff",false);
        client = getInputData().getBoolean("client",false);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    private void setup(){
        influx = InfluxdbConnections.getRicInstance(getApplicationContext());
    }


    public Map<String, String> getTagsMap() {
        String tags = sp.getString("tags", "").strip().replace(" ", "");
        Map<String, String> tags_map = Collections.emptyMap();
        if (!tags.isEmpty()) {
            try {
                tags_map = Splitter.on(',').withKeyValueSeparator('=').split(tags);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "cant parse tags, ignoring");
            }
        }
        Map<String, String> tags_map_modifiable = new HashMap<>(tags_map);
        tags_map_modifiable.put("measurement_name", sp.getString("measurement_name", "OMNT"));
        tags_map_modifiable.put("manufacturer", di.getManufacturer());
        tags_map_modifiable.put("model", di.getModel());
        tags_map_modifiable.put("sdk_version", String.valueOf(di.getAndroidSDK()));
        tags_map_modifiable.put("android_version", di.getAndroidRelease());
        tags_map_modifiable.put("secruity_patch", di.getSecurityPatchLevel());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tags_map_modifiable.put("soc_model", di.getSOCModel());
        }
        tags_map_modifiable.put("radio_version", Build.getRadioVersion());
        return tags_map_modifiable;
    }


    @NonNull
    @Override
    public Result doWork() {
        setup();
        Data output = new Data.Builder().putBoolean("iperf3_upload", false).build();
        if(!influx.ping()){
            return Result.failure();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(logFilePath));
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
            return Result.failure(output);
        }
        Root iperf3AsJson = new Gson().fromJson(br, Root.class);
        long timestamp = iperf3AsJson.start.timestamp.timesecs*1000;
        Log.d(TAG, "doWork: "+timestamp);

        String role = "server";
        if(iperf3AsJson.start.connectingTo != null){
            role = "client";
        }


        LinkedList<Point> points = new LinkedList<Point>();
        for (Interval interval: iperf3AsJson.intervals) {
            int idInterval = iperf3AsJson.intervals.indexOf(interval);
            long tmpTimestamp = timestamp + (long) (interval.sum.end * 1000);
            for (Stream stream: interval.streams){
                Point point = new Point(measurementName);
                point.addTag("bidir", String.valueOf(biDir));
                point.addTag("sender", String.valueOf(stream.sender));
                point.addTag("role", role);
                point.addTag("socket", String.valueOf(stream.socket));
                point.addTag("protocol", protocol);
                point.addTag("interval", intervalIperf);
                point.addTag("version", iperf3AsJson.start.version);
                point.addTag("reversed", String.valueOf(rev));
                point.addTag("oneOff", String.valueOf(oneOff));
                point.addTag("connectingToHost", iperf3AsJson.start.connectingTo.host);
                point.addTag("connectingToPort", String.valueOf(iperf3AsJson.start.connectingTo.port));
                point.addTag("bandwith", bandwidth);
                point.addTag("duration", duration);
                point.addTag("bytes", bytes);
                point.addTag("streams", String.valueOf(interval.streams.size()));
                point.addTag("streamIdx", String.valueOf(interval.streams.indexOf(stream)));


                point.addField("bits_per_second", stream.bitsPerSecond);
                point.addField("seconds", stream.seconds);
                point.addField("bytes", stream.bytes);
                point.addField("rtt", stream.rtt);
                point.addField("rttvar", stream.rttvar);
                point.addField("retransmits", stream.retransmits);
                point.addField("jitter_ms", stream.jitterMs);
                point.addField("lost_packets", stream.lostPackets);
                point.addField("lost_percent", stream.lostPercent);

                point.time(tmpTimestamp, WritePrecision.MS);

                points.add(point);
            }
        }

        if(iperf3AsJson.end.streams != null) {
            for (Stream__1 stream : iperf3AsJson.end.streams){
                Stream udp = stream.udp;
                if(udp == null){
                    continue;
                }
                Point point = new Point(measurementName);
                point.addTag("bidir", String.valueOf(biDir));

                point.addTag("sender", String.valueOf(udp.sender));
                point.addTag("role", role);
                point.addTag("socket", String.valueOf(udp.socket));
                point.addTag("protocol", protocol);
                point.addTag("interval", intervalIperf);
                point.addTag("version", iperf3AsJson.start.version);
                point.addTag("reversed", String.valueOf(rev));
                point.addTag("oneOff", String.valueOf(oneOff));
                point.addTag("connectingToHost", iperf3AsJson.start.connectingTo.host);
                point.addTag("connectingToPort", String.valueOf(iperf3AsJson.start.connectingTo.port));
                point.addTag("bandwith", bandwidth);
                point.addTag("duration", duration);
                point.addTag("bytes", bytes);

                point.addField("start", udp.start);
                point.addField("end", udp.end);
                point.addField("seconds", udp.seconds);
                point.addField("bytes", udp.bytes);
                point.addField("bits_per_second", udp.bitsPerSecond);
                point.addField("jitter_ms", udp.jitterMs);
                point.addField("lost_packets", udp.lostPackets);
                point.addField("packets", udp.packets);
                point.addField("lost_percent", udp.lostPercent);
                point.addField("out_of_order", udp.outOfOrder);
                points.add(point);
            }
        }
        // is needed when only --udp is, otherwise no lostpackets/lostpercent parsed
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                for (Point point:points) {
                    point.addTags(getTagsMap());
                }

            }
            influx.writePoints(points);
        } catch (IOException e) {
            return Result.failure(output);
        }


        influx.flush();

        output = new Data.Builder().putBoolean("iperf3_upload", true).build();
        return Result.success(output);
    }
}
