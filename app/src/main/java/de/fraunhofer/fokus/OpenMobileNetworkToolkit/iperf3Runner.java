package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;


import java.util.Arrays;

public class iperf3Runner {
    private String[] command;
    private Context context;
    private ThreadGroup iperf3TG;
    private static final String TAG = "iperf3Runner";

    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }


    public iperf3Runner(String[] command, Context context) {
        super();
        this.iperf3TG = new ThreadGroup("iperf3ThreadGroup");
        this.command = command;
        this.context = context;
    }

    private native int iperf3Wrapper(String[] argv, String cache);


    private Runnable createRunable() {
        return new Runnable() {
            public void run() {
                iperf3Wrapper(command, context.getApplicationInfo().nativeLibraryDir);
            }
        };
    }


    public int start(){
        Thread iperf3Thread = new Thread(this.iperf3TG, createRunable());
        iperf3Thread.setPriority(Thread.MAX_PRIORITY);
        iperf3Thread.start();
        return 0;
    }

    @Override
    public String toString() {
        return "iperf3Runner{" +
                "command=" + Arrays.toString(command) +
                ", context=" + context.toString() +
                ", iperf3TG=" + iperf3TG.toString() +
                '}';
    }

    public ThreadGroup getIperf3TG() {
        return iperf3TG;
    }
}
