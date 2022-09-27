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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Root;

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
    private String interval;
    private String bytes;

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
        bandwidth = getInputData().getString("bandwidth");
        duration = getInputData().getString("duration");
        interval = getInputData().getString("interval");
        bytes = getInputData().getString("bytes");


        rev = getInputData().getBoolean("rev", false);
        biDir = getInputData().getBoolean("biDir",false);
        oneOff = getInputData().getBoolean("oneOff",false);
        client = getInputData().getBoolean("client",false);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    private void setup(){
        String url = sp.getString("influx_URL", null);
        String org = sp.getString("influx_org", null);
        String bucket = sp.getString("influx_bucket", null);
        String token = sp.getString("influx_token", null);
        influx = new InfluxdbConnection(url, token, org, bucket, getApplicationContext());
    }
    @NonNull
    @Override
    public Result doWork() {
        setup();
        Data output = new Data.Builder().putBoolean("iperf3_upload", false).build();
        if(!influx.connect()){
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
        long timestamp = iperf3AsJson.start.timestamp.timesecs;
        for (Interval interval: iperf3AsJson.intervals) {
            Point point = new Point("Iperf3");
            point.addTag("IP", ip);
            point.addTag("port", port);
            point.addTag("duration", duration);

            point.addField("rtt", interval.streams.get(0).rtt);
            point.addField("bytes", interval.streams.get(0).bytes);
            point.addField("rttvar", interval.streams.get(0).rttvar);
            long tmpTimestamp = timestamp+Math.round(interval.streams.get(0).end);
            point.time(tmpTimestamp, WritePrecision.S);
            point.addField("bits_per_second", interval.streams.get(0).bitsPerSecond);


            Log.d(TAG, "doWork: "+point.toLineProtocol());
            if(!influx.writePoint(point)){
                return Result.failure(output);
            }
        }

        influx.sendAll();
        influx.disconnect();

        output = new Data.Builder().putBoolean("iperf3_upload", true).build();
        return Result.success(output);
    }
}