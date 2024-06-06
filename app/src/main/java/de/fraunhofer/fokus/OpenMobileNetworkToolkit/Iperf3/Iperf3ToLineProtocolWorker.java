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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.TCP.TCP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.TCP.TCP_UL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.UDP.UDP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.UDP.UDP_UL_STREAM;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;

public class Iperf3ToLineProtocolWorker extends Worker {
    private static final String TAG = "Iperf3UploadWorker";
    InfluxdbConnection influx;
    private SharedPreferences sp;
    private String rawIperf3file;
    private String measurementName;
    private String ip;

    private String port;
    private String bandwidth;
    private String duration;
    private String intervalIperf;
    private String bytes;
    private String protocol;
    private String iperf3LineProtocolFile;

    private DeviceInformation di = GlobalVars.getInstance().get_dp().getDeviceInformation();

    private boolean rev;
    private boolean biDir;
    private boolean oneOff;
    private boolean client;

    private String runID;
    public Iperf3ToLineProtocolWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        rawIperf3file = getInputData().getString("rawIperf3file");

        ip = getInputData().getString("ip");
        measurementName = getInputData().getString("measurementName");
        iperf3LineProtocolFile = getInputData().getString("iperf3LineProtocolFile");
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
        runID = getInputData().getString("iperf3WorkerID");
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
                Log.d(TAG, "can't parse tags, ignoring");
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

        Iperf3Parser iperf3Parser = new Iperf3Parser(rawIperf3file);
        iperf3Parser.parse();


        long timestamp = Integer.toUnsignedLong( iperf3Parser.getStart().getTimestamp().getTimesecs())*1000;
        Log.d(TAG, "doWork: "+timestamp);

        String role = "server";
        if(iperf3Parser.getStart().getConnecting_to() != null){
            role = "client";
        }

        LinkedList<Point> points = new LinkedList<Point>();
        for (Interval interval: iperf3Parser.getIntervals().getIntervalArrayList()) {
            long tmpTimestamp = timestamp + (long) (interval.getSum().getEnd() * 1000);
            int intervalIdx = iperf3Parser.getIntervals().getIntervalArrayList().indexOf(interval);
            for (Stream stream: interval.getStreams().getStreamArrayList()){
                Point point = new Point("Iperf3");
                point.addTag("run_uid", runID);
                point.addTag("bidir", String.valueOf(biDir));
                point.addTag("sender", String.valueOf(stream.getSender()));
                point.addTag("role", role);
                point.addTag("socket", String.valueOf(stream.getSocket()));
                point.addTag("protocol", protocol);
                point.addTag("interval", intervalIperf);
                point.addTag("version", iperf3Parser.getStart().getVersion());
                point.addTag("reversed", String.valueOf(rev));
                point.addTag("oneOff", String.valueOf(oneOff));
                point.addTag("connectingToHost", iperf3Parser
                    .getStart()
                    .getConnecting_to()
                    .getHost());
                point.addTag("connectingToPort", String.valueOf(iperf3Parser
                    .getStart()
                    .getConnecting_to()
                    .getPort()));
                point.addTag("bandwidth", bandwidth);
                point.addTag("duration", duration);
                point.addTag("bytesToTransmit", bytes);
                point.addTag("streams", String.valueOf(interval.getStreams().size()));
                point.addTag("streamIdx", String.valueOf(interval.getStreams().getStreamArrayList().indexOf(stream)));
                point.addTag("intervalIdx", String.valueOf(intervalIdx));

                point.addField("bits_per_second", stream.getBits_per_second());
                point.addField("seconds", stream.getSeconds());
                point.addField("bytes", stream.getBytes());


                switch (stream.getStreamType()){
                    case TCP_DL:
                        break;
                    case TCP_UL:
                        TCP_UL_STREAM tcp_ul_stream = (TCP_UL_STREAM) stream;
                        point.addField("snd_cwnd", tcp_ul_stream.getSnd_cwnd());
                        point.addField("retransmits", tcp_ul_stream.getRetransmits());
                        point.addField("snd_wnd", tcp_ul_stream.getSnd_wnd());
                        point.addField("rtt", tcp_ul_stream.getRtt());
                        point.addField("rttvar", tcp_ul_stream.getRttvar());
                        point.addField("pmtu", tcp_ul_stream.getPmtu());
                        break;
                    case UDP_DL:
                        UDP_DL_STREAM udp_dl_stream = (UDP_DL_STREAM) stream;
                        point.addField("jitter_ms", udp_dl_stream.getJitter_ms());
                        point.addField("lost_packets", udp_dl_stream.getLost_packets());
                        point.addField("packets", udp_dl_stream.getPackets());
                        point.addField("lost_percent", udp_dl_stream.getLost_percent());
                        break;
                    case UDP_UL:
                        break;
                    case UNKNOWN:
                        break;
                }

                point.time(tmpTimestamp, WritePrecision.MS);

                points.add(point);
            }
        }

        // is needed when only --udp is, otherwise no lostpackets/lostpercent parsed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (Point point:points) {
                point.addTags(getTagsMap());
            }

        }


        FileOutputStream iperf3Stream = null;
        try {
            iperf3Stream = new FileOutputStream(iperf3LineProtocolFile, true);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "logfile not created", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if(iperf3Stream == null){
            Toast.makeText(getApplicationContext(),"FileOutputStream is not created for LP file", Toast.LENGTH_SHORT).show();
            return Result.failure();
        }

        try {
            for (Point point: points){
                iperf3Stream.write((point.toLineProtocol() + "\n").getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "doWork: ", e);
        }


        output = new Data.Builder().putBoolean("iperf3_to_lp", true).build();
        return Result.success(output);
    }

}
