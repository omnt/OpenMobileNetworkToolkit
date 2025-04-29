
/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.FileObserver;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkQuery;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3MonitorWorker extends RemoteListenableWorker {
    public static final String TAG = "Iperf3MonitorWorker";

    private final String channelId = "OMNT_notification_channel";
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private Iperf3Input iperf3Input;
    private int notificationID = 4000;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private Iperf3ResultsDataBase db;
    private Iperf3RunResultDao iperf3RunResultDao;
    private Iperf3RunResult iperf3RunResult;
    private Context context;
    private RemoteWorkManager remoteWorkManager;
    private WorkQuery workQuery;
    private ScheduledExecutorService executorService;
    private BufferedReader br = null;
    private String pathToFile;
    private File file;
    private boolean isStopped;

    private RemoteViews notificationLayout;
    private FileObserver fileObserver;

    MetricCalculator metricCalculatorUL;
    MetricCalculator metricCalculatorDL;

    public Iperf3MonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams){
        super(context, workerParams);
        Gson gson = new Gson();
        this.context = context;
        metricCalculatorUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        metricCalculatorDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);
        int notificationNumber = getInputData().getInt(Iperf3Input.NOTIFICATIONUMBER, 0);
        notificationID += notificationNumber;
        db = Iperf3ResultsDataBase.getDatabase(this.context);
        iperf3RunResultDao = db.iperf3RunResultDao();
        notificationBuilder = new NotificationCompat.Builder(this.context, channelId);
        iperf3RunResultDao.getRunResult(iperf3Input.getTestUUID());
        remoteWorkManager = RemoteWorkManager.getInstance(this.context);
        workQuery = WorkQuery.Builder
                .fromTags(Arrays.asList(iperf3Input.getTestUUID()))
                .build();

        executorService = Executors.newScheduledThreadPool(1);

        notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.iperf3_notification);
        notificationLayout.setTextViewText(R.id.notification_title, "Running iPerf3 test...");
        notificationLayout.setViewVisibility(R.id.notification_throughput, GONE);
        notificationLayout.setViewVisibility(R.id.notification_direction, GONE);
        setForegroundAsync(createForegroundInfo(notificationLayout));

        this.pathToFile = iperf3Input.getParameter().getLogfile();
        Log.d(TAG, "Iperf3MonitorWorker: pathToFile: "+this.pathToFile);
        this.file = new File(this.pathToFile);
    }


    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {


            setForegroundAsync(createForegroundInfo(notificationLayout));
            try {
                br = new BufferedReader(new FileReader(file));
                br.ready();
            } catch (IOException ex) {
                Log.d(TAG, "Iperf3MonitorWorker: file not found!" + ex);
            }
            Log.d(TAG, "doWork: starting "+this.getClass().getCanonicalName());
            Timestamp starTtime = new Timestamp(iperf3RunResultDao.getTimestampFromUid(iperf3Input.getTestUUID()));
            while(true){
                Timestamp newTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
                long deltaInMillis = Math.abs(newTimestamp.getTime() - starTtime.getTime());
                if(deltaInMillis > iperf3Input.getParameter().getTime()*1000){
                    break;
                }
                String line = null;
                try {
                    while ((line = br.readLine()) != null) {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(line);
                        } catch (JSONException e) {
                            Log.d(TAG, "run: parsing line as JSONObject failed");
                            break;
                        }
                        String event = null;
                        try {
                            event = obj.getString("event");
                            Log.d(TAG, "run: got event: " + event);
                        } catch (JSONException e) {
                            Log.d(TAG, "run: parsing event failed");
                            break;
                        }
                        switch (event) {
                            case "start":
                                Log.d(TAG, "parse: Start");
                                Start start = new Start();
                                JSONObject startData = null;
                                try {
                                    startData = obj.getJSONObject("data");
                                } catch (JSONException e) {
                                    Log.e(TAG, "parse: getting start failed");
                                    Log.d(TAG, "parse: " + e);

                                }
                                try {
                                    start.parseStart(startData);
                                } catch (JSONException e) {
                                    Log.e(TAG, "parse: parsing start failed");
                                    Log.d(TAG, "parse: " + e);

                                }
                                setProgressAsync(new Data.Builder().putString("start", start.toString()).build());
                                iperf3RunResultDao.updateStart(iperf3Input.getTestUUID(), start.toString());


                                break;
                            case "interval":
                                Log.d(TAG, "parse: Interval");
                                Interval interval = new Interval();
                                JSONObject intervalData = null;
                                try {
                                    intervalData = obj.getJSONObject("data");
                                } catch (JSONException e) {
                                    Log.e(TAG, "run: getting interval failed");
                                    Log.d(TAG, "parse: " + e);

                                }
                                try {
                                    interval.parse(intervalData);
                                } catch (JSONException e) {
                                    Log.e(TAG, "run: parsing interval failed");
                                    Log.d(TAG, "parse: " + e);

                                }


                                Log.d(TAG, "Read Thread: interval: " + interval.toString());
                                if (interval.getSum().getSender()) {
                                    metricCalculatorUL.update(interval.getSum().getBits_per_second());
                                } else {
                                    metricCalculatorDL.update(interval.getSum().getBits_per_second());
                                }

                                String megabitPerSecond = String.valueOf( Math.round(interval.getSum().getBits_per_second() / 1e6));
                                metricCalculatorUL.update(interval.getSum().getBits_per_second());
                                notificationLayout.setTextViewText(R.id.notification_title, String.format("iPerf3 %s:%s", iperf3Input.getParameter().getHost(), iperf3Input.getParameter().getPort()));
                                notificationLayout.setTextViewText(R.id.notification_throughput, String.format("Throughput: %s Mbit/s", megabitPerSecond));
                                notificationLayout.setTextViewText(R.id.notification_direction, String.format("Direction: %s", interval.getSum().getSumType()));
                                notificationLayout.setViewVisibility(R.id.notification_throughput, VISIBLE);
                                notificationLayout.setViewVisibility(R.id.notification_direction, VISIBLE);


                                if (interval.getSumBidirReverse() != null) {
                                    notificationLayout.setViewVisibility(R.id.notification_bidir_throughput, VISIBLE);
                                    notificationLayout.setViewVisibility(R.id.notification_bidir_direction, VISIBLE);
                                    notificationLayout.setTextViewText(R.id.notification_bidir_throughput, String.format("Throughput: %d Mbit/s", Math.round(interval.getSumBidirReverse().getBits_per_second() / 1e6)));
                                    notificationLayout.setTextViewText(R.id.notification_bidir_direction, String.format("Direction: %s", interval.getSumBidirReverse().getSumType()));
                                    metricCalculatorDL.update(interval.getSumBidirReverse().getBits_per_second());
                                }
                                metricCalculatorDL.calcAll();
                                metricCalculatorUL.calcAll();
                                iperf3RunResultDao.updateMetricDL(iperf3Input.getTestUUID(), metricCalculatorDL);
                                iperf3RunResultDao.updateMetricUL(iperf3Input.getTestUUID(), metricCalculatorUL);

                                setProgressAsync(new Data.Builder().putString("interval", interval.toString()).build());
                                setForegroundAsync(createForegroundInfo(notificationLayout));

                                Intervals _intervals = iperf3RunResultDao.getIntervals(iperf3Input.getTestUUID());
                                if (_intervals == null) {
                                    _intervals = new Intervals();
                                }
                                _intervals.addInterval(interval);
                                iperf3RunResultDao.updateIntervals(iperf3Input.getTestUUID(), _intervals);
                                break;
                            case "end":
                                Log.d(TAG, "parse: End");
                                //todo
                                //End end = new End();
                                //JSONObject endData = obj.getJSONObject("data");
                                //end.parseEnd(endData);
                                //support.firePropertyChange("interval", null, end);

                                break;
                            case "error":
                                Log.d(TAG, "parse: Error");
                                de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error error = new Error();
                                String errorString = null;
                                iperf3RunResultDao.updateResult(iperf3Input.getTestUUID(), -1);
                                try {
                                    errorString = obj.getString("data");
                                } catch (JSONException e) {
                                    Log.e(TAG, "run: getting error failed!");
                                    Log.d(TAG, "parse: " + e);
                                    break;
                                }
                                try {
                                    error.parse(errorString);
                                    Log.d(TAG, "startRemoteWork: got error!"+error);
                                    setProgressAsync(new Data.Builder().putString("error", error.toString()).build());
                                    iperf3RunResultDao.updateError(iperf3Input.getTestUUID(), error);
                                } catch (JSONException e) {
                                    Log.e(TAG, "run: parsing error failed!");
                                    Log.d(TAG, "parse: " + e);
                                    break;
                                }

                                return completer.set(Result.success());
                            default:
                                Log.d(TAG, "parse: Unknown event");
                                break;
                        }
                        Thread.sleep((long) (iperf3Input.getParameter().getInterval() * 1001));

                    }
                } catch (Exception e){
                    Log.d(TAG, "run: error reading file: "+e);

                }
            }







           return completer.set(Result.success());
        });
    }

    private ForegroundInfo createForegroundInfo(RemoteViews notificationLayout) {


        notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build();

        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }




}