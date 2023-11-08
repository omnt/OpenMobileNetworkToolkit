package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
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
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Root;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Stream__1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class Iperf3ToLineProtocolWorker extends Worker {
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

    private DeviceInformation di = GlobalVars.getInstance().get_dp().getDeviceInformation();

    private boolean rev;
    private boolean biDir;
    private boolean oneOff;
    private boolean client;

    private String runID;
    public Iperf3ToLineProtocolWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
        runID = getInputData().getString("iperf3runID");
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
                Point point = new Point("Iperf3");
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
                Point point = new Point("Iperf3");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (Point point:points) {
                point.addTags(getTagsMap());
            }

        }


        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/iperf3LP/";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create the log file
        String filename = path + runID + ".txt";
        Log.d(TAG, "logfile: " + filename);
        File logfile = new File(filename);
        try {
            logfile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileOutputStream iperf3Stream = null;
        // get an output stream
        try {
            iperf3Stream = new FileOutputStream(logfile, true);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "logfile not created", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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
