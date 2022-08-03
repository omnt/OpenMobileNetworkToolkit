package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Intent;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;

public class Iperf3OverView {
    private LinkedList<iperf3Runner> iperf3Runners;

    private ThreadGroup iperf3TG;

    public Iperf3OverView(ThreadGroup iperf3TG) {
        this.iperf3Runners = new LinkedList<>();
        this.iperf3TG = iperf3TG;
    }

    public boolean existRunner(iperf3Runner iperf3R){
        return this.iperf3Runners.contains(iperf3R);
    }

    public String getRunnersAsString(){
        JSONArray test = new JSONArray();
        int i = 0;
        for (iperf3Runner iperf3R:this.iperf3Runners) {
            try {
                test.put(i, iperf3R.getCommand());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            i++;
        }

        return test.toString();
    }

    public boolean addRunner(iperf3Runner iperf3R){
         assert !existRunner(iperf3R) : "Runner "+iperf3R.getId()+" already exists!";
         return this.iperf3Runners.add(iperf3R);
    }
}
