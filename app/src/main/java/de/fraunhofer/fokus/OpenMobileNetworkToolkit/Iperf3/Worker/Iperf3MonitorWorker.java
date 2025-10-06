
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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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

    private static final String CHANNEL_ID = "OMNT_notification_channel_iperf3_monitor";
    private static final int BASE_NOTIFICATION_ID = 4000;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

    private Iperf3Input iperf3Input;
    private int notificationID;
    private NotificationCompat.Builder builder;
    private NotificationManager nm;

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

    private MetricCalculator metricCalculatorUL;
    private MetricCalculator metricCalculatorDL;

    private Data.Builder dataBuilder = new Data.Builder();


    public Iperf3MonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

        Gson gson = new Gson();
        metricCalculatorUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        metricCalculatorDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);

        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);

        int notificationNumber = getInputData().getInt(Iperf3Input.NOTIFICATIONUMBER, 0);
        notificationID = BASE_NOTIFICATION_ID + notificationNumber;

        db = Iperf3ResultsDataBase.getDatabase(this.context);
        iperf3RunResultDao = db.iperf3RunResultDao();
        iperf3RunResultDao.getRunResult(iperf3Input.getTestUUID());

        remoteWorkManager = RemoteWorkManager.getInstance(this.context);
        workQuery = WorkQuery.Builder.fromTags(Arrays.asList(iperf3Input.getTestUUID())).build();
        executorService = Executors.newScheduledThreadPool(1);

        nm = context.getSystemService(NotificationManager.class);
        setupNotificationChannel();
        setupNotificationBuilder(context);

        // Initial notification layout
        notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.iperf3_notification);
        notificationLayout.setTextViewText(R.id.notification_title, "Running iPerf3 test...");
        notificationLayout.setViewVisibility(R.id.notification_throughput, GONE);
        notificationLayout.setViewVisibility(R.id.notification_direction, GONE);

        setForegroundAsync(createForegroundInfo(notificationLayout));
        this.pathToFile = iperf3Input.getParameter().getRawLogFilePath();
        this.file = new File(this.pathToFile);
        Log.d(TAG, "Iperf3MonitorWorker: pathToFile: " + this.pathToFile);
        dataBuilder.putString("testUUID", iperf3Input.getTestUUID())
                .putString("measurementUUID", iperf3Input.getMeasurementUUID())
                .putString("sequenceUUID", iperf3Input.getSequenceUUID())
                .putString("campaignUUID", iperf3Input.getCampaignUUID());
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "OMNT iPerf3 Monitor",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }
    }

    private void setupNotificationBuilder(Context context) {
        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
    }

    private ForegroundInfo createForegroundInfo(RemoteViews layout) {
        Notification notification = builder
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(layout)
                .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }
    private void cleaupNotification() {
        nm.cancel(notificationID);
    }
    private void updateNotification(RemoteViews layout) {
        builder.setCustomContentView(layout);
        nm.notify(notificationID, builder.build());
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            try {
                br = new BufferedReader(new FileReader(file));
                br.ready();
            } catch (IOException ex) {
                Log.d(TAG, "Iperf3MonitorWorker: file not found!" + ex);
            }

            Log.d(TAG, "doWork: starting " + this.getClass().getCanonicalName());
            Timestamp starTtime = new Timestamp(iperf3RunResultDao.getTimestampFromUid(iperf3Input.getTestUUID()));

            while (true) {
                Timestamp newTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
                long deltaInMillis = Math.abs(newTimestamp.getTime() - starTtime.getTime());
                if (deltaInMillis > iperf3Input.getParameter().getTime() * 1000) {
                    break;
                }

                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        JSONObject obj;
                        try {
                            obj = new JSONObject(line);
                        } catch (JSONException e) {
                            Log.d(TAG, "run: parsing line as JSONObject failed");
                            break;
                        }

                        String event;
                        try {
                            event = obj.getString("event");
                            Log.d(TAG, "run: got event: " + event);
                        } catch (JSONException e) {
                            Log.d(TAG, "run: parsing event failed");
                            break;
                        }
                        Log.d(TAG, "startRemoteWork: following event: "+event);
                        switch (event) {
                            case "interval":
                                handleIntervalEvent(obj);
                                break;
                            case "start":
                                handleStartEvent(obj);
                                break;
                            case "error":
                                handleErrorEvent(obj);
                                dataBuilder.putString("error", iperf3RunResultDao.getError(iperf3Input.getTestUUID()).toString());

                                return completer.set(Result.failure(dataBuilder.build()));
                            case "end":
                                Log.d(TAG, "parse: End");
                                break;
                            default:
                                Log.d(TAG, "parse: Unknown event");
                                break;
                        }

                        Thread.sleep((long) (iperf3Input.getParameter().getInterval() * 1001));
                    }
                } catch (Exception e) {
                    Log.d(TAG, "run: error reading file: " + e);
                    cleaupNotification();
                    dataBuilder.putString("exception", e.toString());
                    return completer.set(Result.failure(dataBuilder.build()));
                }
            }
            cleaupNotification();
            return completer.set(Result.success(dataBuilder.build()));
        });
    }

    private void handleStartEvent(JSONObject obj) {
        try {
            Start start = new Start();
            start.parseStart(obj.getJSONObject("data"));
            setProgressAsync(dataBuilder.putString("start", start.toString()).build());
            iperf3RunResultDao.updateStart(iperf3Input.getTestUUID(), start.toString());
        } catch (Exception e) {
            Log.e(TAG, "parse: Start event failed", e);
        }
    }

    private void handleIntervalEvent(JSONObject obj) {
        try {
            Interval interval = new Interval();
            interval.parse(obj.getJSONObject("data"));
            Log.d(TAG, "Read Thread: interval: " + interval);

            if (interval.getSum().getSender()) {
                metricCalculatorUL.update(interval.getSum().getBits_per_second());
            } else {
                metricCalculatorDL.update(interval.getSum().getBits_per_second());
            }

            String mbit = String.valueOf(Math.round(interval.getSum().getBits_per_second() / 1e6));
            notificationLayout.setTextViewText(R.id.notification_title,
                    String.format("iPerf3 %s:%s", iperf3Input.getParameter().getHost(), iperf3Input.getParameter().getPort()));
            notificationLayout.setTextViewText(R.id.notification_throughput, "Throughput: " + mbit + " Mbit/s");
            notificationLayout.setTextViewText(R.id.notification_direction, "Direction: " + interval.getSum().getSumType());
            notificationLayout.setViewVisibility(R.id.notification_throughput, VISIBLE);
            notificationLayout.setViewVisibility(R.id.notification_direction, VISIBLE);

            if (interval.getSumBidirReverse() != null) {
                notificationLayout.setViewVisibility(R.id.notification_bidir_throughput, VISIBLE);
                notificationLayout.setViewVisibility(R.id.notification_bidir_direction, VISIBLE);
                notificationLayout.setTextViewText(R.id.notification_bidir_throughput,
                        String.format("Throughput: %d Mbit/s", Math.round(interval.getSumBidirReverse().getBits_per_second() / 1e6)));
                notificationLayout.setTextViewText(R.id.notification_bidir_direction,
                        "Direction: " + interval.getSumBidirReverse().getSumType());
                metricCalculatorDL.update(interval.getSumBidirReverse().getBits_per_second());
            }

            metricCalculatorDL.calcAll();
            metricCalculatorUL.calcAll();
            iperf3RunResultDao.updateMetricDL(iperf3Input.getTestUUID(), metricCalculatorDL);
            iperf3RunResultDao.updateMetricUL(iperf3Input.getTestUUID(), metricCalculatorUL);

            setProgressAsync(dataBuilder.putString("interval", interval.toString()).build());
            updateNotification(notificationLayout);

            Intervals intervals = iperf3RunResultDao.getIntervals(iperf3Input.getTestUUID());
            if (intervals == null) intervals = new Intervals();
            intervals.addInterval(interval);
            iperf3RunResultDao.updateIntervals(iperf3Input.getTestUUID(), intervals);

        } catch (Exception e) {
            Log.e(TAG, "handleIntervalEvent failed", e);
        }
    }

    private void handleErrorEvent(JSONObject obj) {
        try {
            Error error = new Error();
            String errorString = obj.getString("data");
            error.parse(errorString);
            Log.d(TAG, "startRemoteWork: got error! " + error);
            setProgressAsync(dataBuilder.putString("error", error.toString()).build());
            iperf3RunResultDao.updateError(iperf3Input.getTestUUID(), error);
            iperf3RunResultDao.updateResult(iperf3Input.getTestUUID(), -1);
        } catch (Exception e) {
            Log.e(TAG, "handleErrorEvent failed", e);
        }
    }
}
