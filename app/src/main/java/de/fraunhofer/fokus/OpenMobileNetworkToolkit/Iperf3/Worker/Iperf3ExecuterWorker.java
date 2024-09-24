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
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3LibLoader;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3ExecuterWorker extends Worker {
    private static final String TAG = "iperf3Worker";

    static {
        Iperf3LibLoader.load();
    }

    private final String[] cmd;
    private final String uuid;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private final int notificationID = 1002;
    private String ip;
    private String port;
    private String protocol;
    private Iperf3Input.Iperf3Mode mode;
    public Iperf3ExecuterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("command");
        uuid = getInputData().getString("uuid");
        ip = getInputData().getString("ip");
        port = getInputData().getString("port");
        protocol = getInputData().getString("protocol");
        mode = Iperf3Input.Iperf3Mode.valueOf(getInputData().getString("mode"));

    }

    private native int iperf3Wrapper(String[] argv, String cache);

    private native int iperf3Stop();

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {

        Context context = getApplicationContext();
        String id = "OMNT_notification_channel";
        PendingIntent intent = WorkManager.getInstance(context)
            .createCancelPendingIntent(getId());
        Notification notification = new NotificationCompat.Builder(context, id)
            //.setContentTitle("iPerf3 "+ client.substring(0, 1).toUpperCase() + client.substring(1).toLowerCase())
            .setContentText(progress)
            .setOngoing(true)
            .setColor(Color.WHITE)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
            .addAction(R.drawable.ic_close, "Cancel", intent)
            .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped: called!");
        iperf3Stop();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: called!");
        if (port == null) port = "5201";
        String progress = String.format("Connecting to %s:%s with %s", ip, port, protocol);
        if (mode.equals(Iperf3Input.Iperf3Mode.SERVER)) {
            progress = String.format("Running on %s:%s", ip, port);
        }

        setForegroundAsync(createForegroundInfo(progress));

        int result =
            iperf3Wrapper(cmd, getApplicationContext().getApplicationInfo().nativeLibraryDir);
        Log.d(TAG, "doWork: " + result);


        Data.Builder output = new Data.Builder()
            .putInt("iperf3_result", result)
            .putString("iperf3WorkerID", uuid);
        if (result == 0) {
            return Result.success(output.build());
        }
        return Result.failure(output
            .build());
    }
}
