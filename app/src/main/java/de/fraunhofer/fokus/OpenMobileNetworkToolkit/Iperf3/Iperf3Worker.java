package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.UUID;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Worker extends Worker {
    private static final String TAG = "iperf3Worker";
    private final String[] cmd;
    private final String iperf3WorkerID;
    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    public Iperf3Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("commands");
        iperf3WorkerID = UUID.randomUUID().toString();
    }

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {

        Context context = getApplicationContext();
        String id = "OMNT_notification_channel";
        String title = "Iperf3 Test is running...";

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setOngoing(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
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
                .putInt("iperf3_result", result)
                .putString("iperf3WorkerID", iperf3WorkerID);
        if (result == 0)
            return Result.success(output.build());
        return Result.failure(output
                .putBoolean("iperf3_upload", true)
                .putBoolean("iperf3_move", true)
                .build());
    }
}
