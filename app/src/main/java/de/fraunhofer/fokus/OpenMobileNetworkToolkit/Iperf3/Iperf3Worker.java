/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Worker extends Worker {
    private static final String TAG = "iperf3Worker";

    static {
        Iperf3LibLoader.load();
    }

    private final String[] cmd;
    private final String iperf3WorkerID;
    private final String measurementName;
    private final String timestamp;
    private final int notificationID;

    public Iperf3Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("commands");
        measurementName = getInputData().getString("measurementName");
        iperf3WorkerID = getInputData().getString("iperf3WorkerID");
        timestamp = getInputData().getString("timestamp");
        notificationID = getInputData().getInt("notificationID", 100);
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    private native int iperf3Stop();

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {

        Context context = getApplicationContext();
        String id = "OMNT_notification_channel";
        PendingIntent intent = WorkManager.getInstance(context)
            .createCancelPendingIntent(getId());

        Notification notification = new NotificationCompat.Builder(context, id)
            .setContentTitle(progress)
            .setOngoing(true)
            .setColor(Color.WHITE)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
            .addAction(R.drawable.ic_close, "Cancel", intent)
            .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped: called!");
        iperf3Stop();
    }

    @NonNull
    @Override
    public Result doWork() {
        String progress = "Running " + measurementName + " test " + timestamp;
        setForegroundAsync(createForegroundInfo(progress));

        int result =
            iperf3Wrapper(cmd, getApplicationContext().getApplicationInfo().nativeLibraryDir);
        Log.d(TAG, "doWork: " + result);


        Data.Builder output = new Data.Builder()
            .putInt("iperf3_result", result)
            .putString("iperf3WorkerID", iperf3WorkerID);
        if (result == 0) {
            return Result.success(output.build());
        }
        return Result.failure(output
            .build());
    }
}
