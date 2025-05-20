/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP.TCP_UL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP.UDP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ToLineProtocolWorker extends Worker {
    public static final String TAG = "Iperf3ToLineProtocolWorker";
    InfluxdbConnection influx;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private SharedPreferencesGrouper spg;
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;
    private Iperf3RunResultDao iperf3RunResultDao;
    private final DeviceInformation di = GlobalVars.getInstance().get_dp().getDeviceInformation();
    private int notificationID;
    private Iperf3Input iperf3Input;
    public Iperf3ToLineProtocolWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "OMNT_notification_channel");
        notificationID = 200+getInputData().getInt(Iperf3Input.NOTIFICATIONUMBER, 0);
        spg = SharedPreferencesGrouper.getInstance(getApplicationContext());
        setForegroundAsync(createForegroundInfo("Processing iPerf3 data"));
        iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(getApplicationContext()).iperf3RunResultDao();
    }
    private ForegroundInfo createForegroundInfo(String progress) {
        notification = notificationBuilder
                .setContentTitle("iPerf32LineProtocol")
                .setContentText(progress)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
                .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }


    @NonNull
    @Override
    public Result doWork() {
        Data output = new Data.Builder().putBoolean("iperf3_upload", false).build();

        Iperf3RunResult iperf3RunResult = iperf3RunResultDao.getRunResult(iperf3Input.getTestUUID());

        long timestamp = Integer.toUnsignedLong( iperf3RunResult.start.getTimestamp().getTimesecs())*1000;
        Log.d(TAG, "doWork: "+timestamp);

        String role = "server";
        if(iperf3RunResult.start.getConnecting_to() != null){
            role = "client";
        }

        LinkedList<Point> points = new LinkedList<Point>();
        ArrayList<Interval> intervals = iperf3RunResult.intervals.getIntervalArrayList();
        for (Interval interval: intervals) {
            long tmpTimestamp = timestamp + (long) (interval.getSum().getEnd() * 1000);
            int intervalIdx = intervals.indexOf(interval);
            for (Stream stream: interval.getStreams().getStreamArrayList()){
                Point point = new Point("Iperf3");

                point.addTag(Iperf3Input.TESTUUID, iperf3Input.getTestUUID());
                point.addTag(Iperf3Input.SEQUENCEUUID, iperf3Input.getSequenceUUID());
                point.addTag(Iperf3Input.MEASUREMENTUUID, iperf3Input.getMeasurementUUID());
                point.addTag(Iperf3Input.CAMPAIGNUUID, iperf3Input.getCampaignUUID());
                point.addTag(Iperf3Input.IPERF3UUID, iperf3Input.getParameter().getiPerf3UUID());




                point.addTag("bidir", String.valueOf(iperf3Input.getParameter().getBidir()));
                point.addTag("sender", String.valueOf(stream.getSender()));
                point.addTag("role", role);
                point.addTag("socket", String.valueOf(stream.getSocket()));
                point.addTag("protocol", iperf3RunResult.start.getTest_start().protocol);
                point.addTag("interval", String.valueOf(iperf3Input.getParameter().getInterval()));
                point.addTag("version", iperf3RunResult.start.getVersion());
                point.addTag("reversed", String.valueOf(iperf3Input.getParameter().getReverse()));
                point.addTag("oneOff", String.valueOf(iperf3Input.getParameter().getOneOff()));
                point.addTag("connectingToHost", iperf3RunResult
                    .start
                    .getConnecting_to()
                    .getHost());
                point.addTag("connectingToPort", String.valueOf(iperf3RunResult
                    .start
                    .getConnecting_to()
                    .getPort()));
                point.addTag("bandwidth", iperf3Input.getParameter().getBitrate());
                point.addTag("duration", String.valueOf(iperf3Input.getParameter().getTime()));
                point.addTag("bytesToTransmit", String.valueOf(iperf3Input.getParameter().getBytes()));
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
                setForegroundAsync(createForegroundInfo("Processing iPerf3 data: "+intervalIdx+"/"+intervals.size()));
            }
        }

        // is needed when only --udp is, otherwise no lostpackets/lostpercent parsed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (Point point:points) {
                point.addTags(GlobalVars.getInstance().get_dp().getTagsMap());
            }

        }
        File path = new File(Iperf3Parameter.lineProtocolDirPath);
        if(!path.exists()){
            path.mkdirs();
        }
        File iperf3File = new File(iperf3Input.getParameter().getLineProtocolFile());
        if (!iperf3File.exists()) {
            try {
                iperf3File.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "doWork: ", e);
            }
        }

        FileOutputStream iperf3Stream = null;
        try {
            iperf3Stream = new FileOutputStream(iperf3Input.getParameter().getLineProtocolFile(), true);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "logfile not created", Toast.LENGTH_SHORT).show();
            Log.d(TAG,e.toString());
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
