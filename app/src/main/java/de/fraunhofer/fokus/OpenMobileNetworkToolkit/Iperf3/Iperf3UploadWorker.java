package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.Gson;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Root;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream__1;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

public class Iperf3UploadWorker extends Worker {
    private static final String TAG = "Iperf3UploadWorker";
    InfluxdbConnection influx;
    private final SharedPreferences sp;
    private final String logFilePath;
    private final String measurementName;
    private final String ip;

    private String port;
    private String bandwidth;
    private String duration;
    private String intervalIperf;
    private String bytes;
    private final String protocol;

    private final boolean rev;
    private final boolean biDir;
    private final boolean oneOff;
    private final boolean client;

    public Iperf3UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        logFilePath = getInputData().getString("logfilepath");

        ip = getInputData().getString("ip");
        measurementName = getInputData().getString("measurementName");

        port = getInputData().getString("port");
        if (port == null) {
            port = "5201";
        }
        protocol = getInputData().getString("protocol");
        bandwidth = getInputData().getString("bandwidth");

        if (bandwidth == null) {
            if (protocol.equals("TCP")) {
                bandwidth = "unlimited";
            } else {
                bandwidth = "1000";
            }
        }

        duration = getInputData().getString("duration");
        if (duration == null) {
            duration = "10";
        }
        intervalIperf = getInputData().getString("interval");
        if (intervalIperf == null) {
            intervalIperf = "1";
        }
        bytes = getInputData().getString("bytes");
        if (bytes == null) {
            if (protocol.equals("TCP")) {
                bytes = "8";
            } else {
                bytes = "1470";
            }
        }

        rev = getInputData().getBoolean("rev", false);
        biDir = getInputData().getBoolean("biDir", false);
        oneOff = getInputData().getBoolean("oneOff", false);
        client = getInputData().getBoolean("client", false);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void setup() {
        influx = InfluxdbConnections.getRicInstance(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        setup();
        Data output = new Data.Builder().putBoolean("iperf3_upload", false).build();
        if (!influx.ping()) {
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
        long timestamp = iperf3AsJson.start.timestamp.timesecs.longValue() * 1000;
        Log.d(TAG, "doWork: " + timestamp);

        String role = "server";
        if (iperf3AsJson.start.connectingTo != null) {
            role = "client";
        }


        LinkedList<Point> points = new LinkedList<Point>();
        for (Interval interval : iperf3AsJson.intervals) {
            for (Stream stream : interval.streams) {
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
                point.addTag("connectingToPort",
                    String.valueOf(iperf3AsJson.start.connectingTo.port));
                point.addTag("bandwith", bandwidth);
                point.addTag("duration", duration);
                point.addTag("bytes", bytes);


                point.addField("bits_per_second", stream.bitsPerSecond);
                point.addField("seconds", stream.seconds);
                point.addField("bytes", stream.bytes);
                point.addField("rtt", stream.rtt);
                point.addField("rttvar", stream.rttvar);
                point.addField("retransmits", stream.retransmits);
                point.addField("jitter_ms", stream.jitterMs);
                point.addField("lost_packets", stream.lostPackets);
                point.addField("lost_percent", stream.lostPercent);

                long tmpTimestamp = timestamp + (long) (stream.end * 1000);
                point.time(tmpTimestamp, WritePrecision.MS);

                points.add(point);
            }
        }
        DataProvider dp = new DataProvider(getApplicationContext());

        // is needed when only --udp is, otherwise no lostpackets/lostpercent parsed
        for (Stream__1 stream : iperf3AsJson.end.streams) {
            Stream udp = stream.udp;
            if (udp == null) {
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
        for (Point point : points) {
            point.addTags(dp.getTagsMap());
            if (!influx.writePoint(point)) {
                return Result.failure(output);
            }
        }


        influx.flush();

        output = new Data.Builder().putBoolean("iperf3_upload", true).build();
        return Result.success(output);
    }
}
