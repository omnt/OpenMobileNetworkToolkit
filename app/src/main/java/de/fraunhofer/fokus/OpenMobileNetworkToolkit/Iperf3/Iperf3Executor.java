/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker.InfluxDB2xUploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Executor.Iperf3ServiceWorkerOne;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Monitor.Iperf3MonitorServiceWorkerOne;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3MonitorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class Iperf3Executor {
    private static final String TAG = "Iperf3Executor";
    private RemoteWorkManager remoteWorkManager;
    private WorkManager workManager;
    private Context context;
    private SharedPreferencesGrouper spg;
    private RemoteWorkContinuation remoteWorkContinuation;
    private WorkContinuation localWorkContinuation;
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
        this.workManager = WorkManager.getInstance(this.context);
        OneTimeWorkRequest iperf3ExecutorWorker = new OneTimeWorkRequest.Builder(Iperf3ExecutorWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0, context.getPackageName(), Iperf3ServiceWorkerOne.class.getName()).build())
                .addTag(iperf3Input.getTestUUID())
                .build();
        OneTimeWorkRequest iperf3MonitorWorker = new OneTimeWorkRequest.Builder(Iperf3MonitorWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0, context.getPackageName(), Iperf3MonitorServiceWorkerOne.class.getName()).build())
                .addTag(iperf3Input.getTestUUID())
                .build();
        OneTimeWorkRequest iPerf3ToLineProtocolWorker = new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0).build())
                .addTag(iperf3Input.getTestUUID())
                .build();
        OneTimeWorkRequest influxDB2xUploadWorker = new OneTimeWorkRequest.Builder(InfluxDB2xUploadWorker.class)
                .setInputData(iperf3Input.getInputAsDataBuilder(0).build())
                .addTag(iperf3Input.getTestUUID())
                .build();


        this.remoteWorkContinuation = this.remoteWorkManager.beginWith(Arrays.asList(iperf3ExecutorWorker, iperf3MonitorWorker)).then(iPerf3ToLineProtocolWorker);
        if(spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)){
            this.remoteWorkContinuation = remoteWorkContinuation.then(influxDB2xUploadWorker);
        }
    }

    public void execute(){
        this.remoteWorkContinuation.enqueue();

    }

}
