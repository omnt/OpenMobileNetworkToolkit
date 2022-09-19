package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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


    @NonNull
    @Override
    public Result doWork() {
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
