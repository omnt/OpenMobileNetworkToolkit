package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkInfo;
import androidx.work.WorkQuery;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Parser;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3MonitorWorker extends Worker {
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
    private RemoteViews notificationLayout;
    public Iperf3MonitorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams){
        super(context, workerParams);
        Gson gson = new Gson();
        this.context = context;
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

    private Runnable read = () -> {
        Log.d(TAG, "startRemoteWork: starting reading thread...");
        Iperf3Parser iperf3Parser = new Iperf3Parser(iperf3Input.getParameter().getLogfile());
        MetricCalculator metricCalculatorUL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);
        MetricCalculator metricCalculatorDL = new MetricCalculator(METRIC_TYPE.THROUGHPUT);

        iperf3Parser.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "interval":
                    Interval interval = (Interval) evt.getNewValue();
                    Log.d(TAG, "Read Thread: interval: " + interval.toString());
                    if(interval.getSum().getSender()){
                        metricCalculatorUL.update(interval.getSum().getBits_per_second());
                    } else {
                        metricCalculatorDL.update(interval.getSum().getBits_per_second());
                    }


                    String megabitPerSecond = String.valueOf(interval.getSum().getBits_per_second() / 1e6);
                    setProgressAsync(new Data.Builder().putString("interval", interval.toString()).build());
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

                    setForegroundAsync(createForegroundInfo(notificationLayout));

                    Intervals _intervals = iperf3RunResultDao.getIntervals(iperf3Input.getTestUUID());
                    if(_intervals == null){
                        _intervals = new Intervals();
                    }
                    _intervals.addInterval(interval);
                    iperf3RunResultDao.updateIntervals(iperf3Input.getTestUUID(), _intervals);
                    break;
                case "end":
                    Log.d(TAG, "Read Thread: end: " + evt.getNewValue().toString());
                    setProgressAsync(new Data.Builder().putString("error", evt.getNewValue().toString()).build());
                    iperf3RunResultDao.updateEnd(iperf3Input.getTestUUID(), evt.getNewValue().toString());
                    iperf3Parser.close();
                    break;
                case "error":
                    Log.d(TAG, "Read Thread: error: " + evt.getNewValue());
                    setProgressAsync(new Data.Builder().putString("error", evt.getNewValue().toString()).build());

                    de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error error = (de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error) evt.getNewValue();
                    iperf3RunResultDao.updateResult(iperf3Input.getTestUUID(), 1);
                    iperf3RunResultDao.updateError(iperf3Input.getTestUUID(), error);
                    iperf3Parser.close();
                    break;
                case "start":
                    Log.d(TAG, "Read Thread: start: " + evt.getNewValue());
                    Start start = (Start) evt.getNewValue();
                    setProgressAsync(new Data.Builder().putString("start", start.toString()).build());
                    iperf3RunResultDao.updateStart(iperf3Input.getTestUUID(), evt.getNewValue().toString());
                    break;
                default:
                    Log.d(TAG, "Read Thread: unknown property: " + evt.getPropertyName());
                    iperf3Parser.close();
                    break;
            }
        });
        iperf3Parser.parse();
        metricCalculatorDL.calcAll();
        metricCalculatorUL.calcAll();
        iperf3RunResultDao.updateMetricDL(iperf3RunResult.uid, metricCalculatorDL);
        iperf3RunResultDao.updateMetricUL(iperf3RunResult.uid, metricCalculatorUL);
        try {
            iperf3Parser.getRunnable().wait(iperf3Input.getParameter().getTime());
        } catch (InterruptedException e) {
            Log.d(TAG, "startRemoteWork: "+e);
        }
        Log.d(TAG, "Read Thread: finished");
    };

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: starting "+this.getClass().getCanonicalName());
        executorService.schedule(read, (long) (iperf3Input.getParameter().getInterval()+1), TimeUnit.SECONDS);
        executorService.shutdown();
        long runTime = 10;
        if(iperf3Input.getParameter().getTime() != 0) runTime = iperf3Input.getParameter().getTime();
        runTime += 1;
        Log.d(TAG, "startRemoteWork: timeout: "+runTime);

        Log.d(TAG, "startRemoteWork: awating threads");
        try {
            boolean taskFinished =  executorService.awaitTermination(runTime, TimeUnit.SECONDS);
            Log.d(TAG, "doWork: task finished: "+taskFinished);
        } catch (Exception e) {
            Log.d(TAG, "doWork: "+e);
        }
        return Result.success();
    }
}
