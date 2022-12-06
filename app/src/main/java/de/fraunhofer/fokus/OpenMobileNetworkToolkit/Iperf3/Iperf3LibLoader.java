package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.util.Log;

import java.util.ArrayList;

public class Iperf3LibLoader {


    private static boolean done = false;
    private static String TAG = "Iperf3LibLoader";

    protected static synchronized void load() {
        if (done)
            return;
        ArrayList<Integer> loadedLibs = new ArrayList<>();
        try {
            System.loadLibrary("iperf3.10.1");
            Log.i(TAG, "iperf3.10.1 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {}

        try {
            System.loadLibrary("iperf3.11");
            Log.i(TAG, "iperf3.11 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {

        }

        try {
            System.loadLibrary("iperf3.12");
            Log.i(TAG, "iperf3.12 loaded!");
            loadedLibs.add(1);
        } catch (UnsatisfiedLinkError ignored) {
            return;
        }

        if(loadedLibs.size() > 1){
            Log.d(TAG, "static initializer: multiple libiperfs loaded!");
        }
        done = true;
    }

}
