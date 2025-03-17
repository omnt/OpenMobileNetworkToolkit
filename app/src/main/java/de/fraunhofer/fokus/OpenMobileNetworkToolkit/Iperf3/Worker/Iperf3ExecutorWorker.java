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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteCoroutineWorker;
import androidx.work.multiprocess.RemoteListenableWorker;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Parser;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import kotlin.coroutines.Continuation;

public class Iperf3ExecutorWorker extends RemoteListenableWorker {
    public static final String TAG = "Iperf3ExecutorWorker";

    static {
        Iperf3LibLoader.load();
    }
    private final String channelId = "OMNT_notification_channel";
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private Iperf3Input iperf3Input;
    private int notificationID = 2000;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private Iperf3ResultsDataBase db;
    private Iperf3RunResultDao iperf3RunResultDao;
    public Iperf3ExecutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);
        int notificationNumber = getInputData().getInt(PingInput.NOTIFICATIONUMBER, 0);
        notificationID += notificationNumber;
        db = Iperf3ResultsDataBase.getDatabase(context);
        iperf3RunResultDao = db.iperf3RunResultDao();

        notificationBuilder = new NotificationCompat.Builder(context, channelId);
    }


    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {


            Log.d(TAG, "startRemoteWork: tags: "+this.getTags());
            File logFile = new File(iperf3Input.getParameter().getLogfile());
            File rawPath = new File(Iperf3Parameter.rawDirPath);
            if(logFile.exists() && !logFile.isDirectory()) {
                //weird hack otherwise the worker gets enqueued two times
                return completer.set(Result.success(new Data.Builder().putString("testUUID", iperf3Input.getTestUUID()).build()));
            }
            if(!rawPath.exists()) {
                rawPath.mkdirs();
            }
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.d(TAG, "startRemoteWork: "+e);
                return completer.set(Result.failure());
            }
            Log.i(TAG, "Starting "+TAG);
            setForegroundAsync(createForegroundInfo(iperf3Input.getParameter().getHost()+":"+iperf3Input.getParameter().getPort()));

            final int[] result = {-1};

            Log.d(TAG, "startRemoteWork: running thread");


            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
            Runnable iperf3  = () -> {
                result[0] = iperf3Wrapper(iperf3Input.getParameter().getInputAsCommand(), getApplicationContext().getApplicationInfo().nativeLibraryDir);
                Log.d(TAG, "JNI Thread: " + result[0]);
            };

            Runnable read = () -> {

                Iperf3Parser iperf3Parser = new Iperf3Parser(iperf3Input.getParameter().getLogfile());
                iperf3Parser.addPropertyChangeListener(evt -> {
                    switch (evt.getPropertyName()) {
                        case "interval":
                            Log.d(TAG, "Read Thread: interval: " + evt.getNewValue());
                            setProgressAsync(new Data.Builder().putString("interval", evt.getNewValue().toString()).build());
                            List<String> _intervals = iperf3RunResultDao.getIntervals(iperf3Input.getTestUUID());
                            _intervals.add(evt.getNewValue().toString());
                            iperf3RunResultDao.updateIntervals(iperf3Input.getTestUUID(), _intervals);
                            break;
                        case "end":
                            Log.d(TAG, "Read Thread: end: " + evt.getNewValue().toString());
                            setProgressAsync(new Data.Builder().putString("error", evt.getNewValue().toString()).build());
                            iperf3RunResultDao.updateEnd(iperf3Input.getTestUUID(), evt.getNewValue().toString());
                            break;
                        case "start":
                            Log.d(TAG, "Read Thread: start: " + evt.getNewValue());
                            setProgressAsync(new Data.Builder().putString("result", evt.getNewValue().toString()).build());
                            iperf3RunResultDao.updateStart(iperf3Input.getTestUUID(), evt.getNewValue().toString());
                            break;
                        default:
                            Log.d(TAG, "Read Thread: unknown property: " + evt.getPropertyName());
                            break;
                    }
                });
                iperf3Parser.parse();


            /*
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d(TAG, "Read Thread: line: " + line);
                        Data data = new Data.Builder().putString("line",line).build();
                        setProgressAsync(data);
                        setForegroundAsync(createForegroundInfo(line));
                        try {
                            Thread.sleep((long) (iperf3Input.getParameter().getInterval() * 1000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            Log.e(TAG, "Read Thread: Reading thread interrupted", e);
                        }
                    }
                    Log.d(TAG, "Read Thread: finished reading");
                } catch (IOException e) {e.printStackTrace();}
                */
            };

            Iperf3RunResult iperf3RunResult = new Iperf3RunResult(iperf3Input.getTestUUID(), iperf3Input.getParameter().getLogfile());



            Log.d(TAG, "startRemoteWork: schedule threads");
            executorService.execute(iperf3);
            executorService.schedule(read, (long) (iperf3Input.getParameter().getInterval()+1), TimeUnit.SECONDS);
            executorService.shutdown();
            int runTime = 10;
            if(iperf3Input.getParameter().getTime() != 0) runTime = iperf3Input.getParameter().getTime();
            runTime += 4;
            Log.d(TAG, "startRemoteWork: timeout: "+runTime);

            Log.d(TAG, "startRemoteWork: awating threads");
            boolean taskFinished =  executorService.awaitTermination(runTime, TimeUnit.SECONDS);
            Log.d(TAG, "startRemoteWork: threads awaited");
            Log.d(TAG, "startRemoteWork: threads timeout: "+!taskFinished);
            Data.Builder output = new Data.Builder()
                    .putInt("result", result[0])
                    .putString("testUUID", iperf3Input.getTestUUID());
            if (result[0] == 0) {
                return completer.set(Result.success(output.build()));
            }
            return completer.set(Result.failure(output
                    .build()));
        });


    }

    @NonNull
    @Override
    public ListenableFuture<Void> setProgressAsync(@NonNull Data data) {
        return super.setProgressAsync(data);
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    private ForegroundInfo createForegroundInfo(String progress) {
        notification = notificationBuilder
                .setContentTitle("iPerf3")
                .setContentText(progress)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }
}