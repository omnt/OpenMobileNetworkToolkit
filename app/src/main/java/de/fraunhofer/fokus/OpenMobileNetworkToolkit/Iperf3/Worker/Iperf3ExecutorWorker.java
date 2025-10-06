/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
import static android.view.View.GONE;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.common.reflect.Reflection.getPackageName;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.io.File;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ExecutorWorker extends RemoteListenableWorker {
    public static final String TAG = "Iperf3ExecutorWorker";

    static {
        Iperf3LibLoader.load();
    }

    private static final String CHANNEL_ID = "OMNT_notification_channel_iperf3_executor";
    private static final int BASE_NOTIFICATION_ID = 2000;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

    private Iperf3Input iperf3Input;
    private int notificationID;
    private NotificationCompat.Builder builder;
    private NotificationManager nm;

    private Iperf3ResultsDataBase db;
    private Iperf3RunResultDao iperf3RunResultDao;
    private Data.Builder dataBuilder = new Data.Builder();

    public Iperf3ExecutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "Iperf3ExecutorWorker: called!");

        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(Iperf3Input.INPUT);
        iperf3Input = gson.fromJson(iperf3InputString, Iperf3Input.class);

        int notificationNumber = getInputData().getInt(PingInput.NOTIFICATIONUMBER, 0);
        notificationID = BASE_NOTIFICATION_ID + notificationNumber;

        db = Iperf3ResultsDataBase.getDatabase(context);
        iperf3RunResultDao = db.iperf3RunResultDao();

        nm = context.getSystemService(NotificationManager.class);
        setupNotificationChannel();
        setupNotificationBuilder(context);
        dataBuilder.putString("testUUID", iperf3Input.getTestUUID())
                .putString("measurementUUID", iperf3Input.getMeasurementUUID())
                .putString("sequenceUUID", iperf3Input.getSequenceUUID())
                .putString("campaignUUID", iperf3Input.getCampaignUUID());
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "OMNT iPerf3 Worker",
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

    private ForegroundInfo createForegroundInfo(RemoteViews notificationLayout) {
        Notification notification = builder
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }

    private void updateNotification(RemoteViews notificationLayout) {
        builder.setCustomContentView(notificationLayout);
        nm.notify(notificationID, builder.build());
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Log.d(TAG, "startRemoteWork: tags: " + this.getTags());

            File logFile = new File(iperf3Input.getParameter().getRawLogFilePath());
            File rawPath = new File(iperf3Input.getParameter().getRawDirPath());
            if (!rawPath.exists()) rawPath.mkdirs();

            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "startRemoteWork: " + e);
                dataBuilder.putString("exception", "Logfile could not be created");
                return completer.set(Result.failure(dataBuilder.build()));
            }

            RemoteViews notificationLayout = new RemoteViews(
                    getApplicationContext().getPackageName(),
                    R.layout.iperf3_notification
            );
            notificationLayout.setTextViewText(R.id.notification_title, "Running iPerf3 test...");
            notificationLayout.setViewVisibility(R.id.notification_throughput, GONE);
            notificationLayout.setViewVisibility(R.id.notification_direction, GONE);

            // Set worker into foreground
            setForegroundAsync(createForegroundInfo(notificationLayout));

            // JNI execution
            int result = iperf3Wrapper(
                    iperf3Input.getParameter().getInputAsCommand(),
                    getApplicationContext().getApplicationInfo().nativeLibraryDir
            );
            Log.d(TAG, "startRemoteWork: JNI Thread: " + result);

            iperf3RunResultDao.updateResult(iperf3Input.getTestUUID(), result);

            dataBuilder.putInt("result", result);
            if (result == 0) {
                Log.d(TAG, "startRemoteWork: iperf3Wrapper: success");
                return completer.set(Result.success(dataBuilder.build()));
            } else {
                Log.d(TAG, "startRemoteWork: iperf3Wrapper: failure");
                return completer.set(Result.failure(dataBuilder.build()));
            }
        });
    }

    private native int iperf3Wrapper(String[] argv, String cache);
}
