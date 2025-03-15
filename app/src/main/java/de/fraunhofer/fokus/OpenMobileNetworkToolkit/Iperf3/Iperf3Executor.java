package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;


import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker.InfluxDB2xUploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class Iperf3Executor {
    private static final String TAG = "Iperf3Executor";
    private RemoteWorkManager remoteWorkManager;
    private Context context;
    private SharedPreferencesGrouper spg;
    private RemoteWorkContinuation workContinuation;
    @SuppressLint("EnqueueWork")
    public Iperf3Executor(Iperf3Input iperf3Input, Context context){

        if(iperf3Input == null){
            throw new IllegalArgumentException("Iperf3Input cannot be null");
        }
        if(context == null){
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.spg = SharedPreferencesGrouper.getInstance(this.context);
        this.remoteWorkManager = RemoteWorkManager.getInstance(this.context);
        OneTimeWorkRequest iperf3ExecutorWorker = new OneTimeWorkRequest.Builder(Iperf3ExecutorWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0, context.getPackageName()).build())
                .addTag(iperf3Input.getTestUUID())
                .build();
        OneTimeWorkRequest iPerf3ToLineProtocolWorker = new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0, context.getPackageName()).build())
                .addTag(iperf3Input.getTestUUID())
                .build();
        OneTimeWorkRequest influxDB2xUploadWorker = new OneTimeWorkRequest.Builder(InfluxDB2xUploadWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0, context.getPackageName()).build())
                .addTag(iperf3Input.getTestUUID())
                .build();

        this.remoteWorkManager.beginWith(iperf3ExecutorWorker).enqueue();
        //this.workContinuation = this.remoteWorkManager.beginWith(iperf3ExecutorWorker).then(iPerf3ToLineProtocolWorker);
        //if(spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)){
        //    this.workContinuation = workContinuation.then(influxDB2xUploadWorker);
        //}
    }

    public void execute(){
        //this.workContinuation.enqueue();
    }

}
