package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class iperf3Worker extends Worker {
    private static final String TAG = "iperf3Worker";
    private final String[] cmd;
    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    public iperf3Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("commands");
    }

    @NonNull
    @Override
    public Result doWork() {
        iperf3Wrapper(cmd, getApplicationContext().getApplicationInfo().nativeLibraryDir);
        return Result.success();
    }
}
