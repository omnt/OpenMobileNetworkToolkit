package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
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
    private native int iperf3Stop();
    public iperf3Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cmd = getInputData().getStringArray("commands");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: called!");
        Log.d(TAG, "doWork: "+getId());
        if(isStopped()){
            return Result.failure();
        }
        int check = iperf3Wrapper(cmd, getApplicationContext().getApplicationInfo().nativeLibraryDir);
        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putInt("return", check);

        if (check == 0) {
            return Result.success(iperf3Data.build());
        }
        return Result.failure(iperf3Data.build());
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: called!");
        iperf3Stop();
    }
}
