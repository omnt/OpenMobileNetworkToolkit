package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

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

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Worker extends Worker {
    private static final String TAG = "iperf3Worker";
    private final String[] cmd;
    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    public Iperf3Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("commands");
    }

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String id = "OMNT_notification_channel";
        String title = "Iperf3 Test is running...";
        // This PendingIntent can be used to cancel the worker

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setOngoing(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        // Add the cancel action to the notification which can
                // be used to cancel the worker
                .build();
        return new ForegroundInfo(100, notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        String progress = "Starting Iperf3 test";
        setForegroundAsync(createForegroundInfo(progress));

        int result = iperf3Wrapper(cmd, getApplicationContext().getApplicationInfo().nativeLibraryDir);
        Log.d(TAG, "doWork: "+result);


        Data.Builder output = new Data.Builder()
                .putInt("iperf3_result", result);
        if (result == 0)
            return Result.success(output.build());
        return Result.failure(output
                .putBoolean("iperf3_upload", true)
                .putBoolean("iperf3_move", true)
                .build());
    }
}
