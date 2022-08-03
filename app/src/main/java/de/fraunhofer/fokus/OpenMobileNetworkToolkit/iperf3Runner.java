package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;


import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class iperf3Runner {
    private String[] command;
    private Context context;
    private ThreadGroup iperf3TG;
    private String id;
    private Thread iperf3Thread;
    private static final String TAG = "iperf3Runner";

    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }


    public iperf3Runner(String[] command, Context context, ThreadGroup iperf3TG) {
        super();
        this.iperf3TG = iperf3TG;
        this.command = command;
        this.context = context;
        this.id = UUID.randomUUID().toString();
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    public String getCommand() {
        return String.join(" ", command);
    }

    private Runnable createRunable() {
        return () -> iperf3Wrapper(command, context.getApplicationInfo().nativeLibraryDir);
    }


    public int start(){
        iperf3Thread = new Thread(this.iperf3TG, createRunable(), this.id);
        iperf3Thread.setPriority(Thread.MAX_PRIORITY);
        iperf3Thread.start();
        return 0;
    }

    public String getThreadState(){
        return this.iperf3Thread.getState().toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "iperf3Runner{" +
                "command=" + Arrays.toString(command) +
                ", context=" + context.toString() +
                ", iperf3TG=" + iperf3TG.toString() +
                '}';
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof iperf3Runner)) return false;
        iperf3Runner that = (iperf3Runner) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
