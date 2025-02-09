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

import java.util.concurrent.ExecutionException;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
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
    private int notificationID;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    public Iperf3ExecutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);
        int notificationNumber = getInputData().getInt(PingInput.NOTIFICATIONUMBER, 0);
        notificationID += notificationNumber;

        notificationBuilder = new NotificationCompat.Builder(context, channelId);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {

        return CallbackToFutureAdapter.getFuture(completer -> {

            Log.i(TAG, "Starting "+TAG);
            setForegroundAsync(createForegroundInfo(iperf3Input.getIperf3Parameter().getHost()+":"+iperf3Input.getIperf3Parameter().getPort()));

            int result =
                    iperf3Wrapper(iperf3Input.getIperf3Parameter().getInputAsCommand(), getApplicationContext().getApplicationInfo().nativeLibraryDir);
            Log.d(TAG, "doWork: " + result);


            Data.Builder output = new Data.Builder()
                    .putInt("result", result)
                    .putString("testUUID", iperf3Input.getTestUUID());
            if (result == 0) {
                return completer.set(Result.success(output.build()));
            }
            return completer.set(Result.failure(output
                    .build()));
        });
    }


    private native int iperf3Wrapper(String[] argv, String cache);

    private native int iperf3Stop();

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