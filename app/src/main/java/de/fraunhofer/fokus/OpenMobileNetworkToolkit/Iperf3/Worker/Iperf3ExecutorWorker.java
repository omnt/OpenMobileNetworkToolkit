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

import static com.google.common.reflect.Reflection.getPackageName;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
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
    private final String channelId = "OMNT_notification_channel";
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private Iperf3Input iperf3Input;
    private int notificationID = 2000;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private Iperf3ResultsDataBase db;
    private Iperf3RunResultDao iperf3RunResultDao;
    private Iperf3RunResult iperf3RunResult;
    public Iperf3ExecutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "Iperf3ExecutorWorker: called!");
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

            if(!rawPath.exists()) {
                rawPath.mkdirs();
            }
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.d(TAG, "startRemoteWork: "+e);
                return completer.set(Result.failure());
            }
            Log.i(TAG, "startRemoteWork: "+TAG);

            RemoteViews notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.iperf3_notification);
            notificationLayout.setTextViewText(R.id.notification_title, "Running iPerf3 test...");
            notificationLayout.setViewVisibility(R.id.notification_throughput, GONE);
            notificationLayout.setViewVisibility(R.id.notification_direction, GONE);


            setForegroundAsync(createForegroundInfo(notificationLayout));

            int result = -1;

            Log.d(TAG, "startRemoteWork: about to call iPerf3 JNI!");
            result = iperf3Wrapper(iperf3Input.getParameter().getInputAsCommand(), getApplicationContext().getApplicationInfo().nativeLibraryDir);
            Log.d(TAG, "startRemoteWork: JNI Thread: " + result);



            iperf3RunResultDao.updateResult(iperf3Input.getTestUUID(), result);
            Data.Builder output = new Data.Builder()
                    .putInt("result", result)
                    .putString("testUUID", iperf3Input.getTestUUID());


            if (result == 0) {
                Log.d(TAG, "startRemoteWork: iperf3Wrapper: success");
                return completer.set(Result.success(output.build()));
            }
            Log.d(TAG, "startRemoteWork: iperf3Wrapper: failure");
            return completer.set(Result.failure(output.build()));
        });


    }


    @NonNull
    @Override
    public ListenableFuture<Void> setProgressAsync(@NonNull Data data) {
        return super.setProgressAsync(data);
    }

    private native int iperf3Wrapper(String[] argv, String cache);


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