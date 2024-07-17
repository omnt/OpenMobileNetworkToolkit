/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;

public class Iperf3UploadWorker extends Worker {
    private static final String TAG = "Iperf3UploadWorker";
    InfluxdbConnection influx;
    private final SharedPreferences sp;
    private final String iperf3LineProtocolFile;


    public Iperf3UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        iperf3LineProtocolFile = getInputData().getString("iperf3LineProtocolFile");
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    private void setup(){
        influx = InfluxdbConnections.getRicInstance(getApplicationContext());
    }



    @NonNull
    @Override
    public Result doWork() {
        setup();
        Data output = new Data.Builder().putBoolean("iperf3_upload", false).build();
        if(influx == null){
            return Result.failure(output);
        }
        if(influx.getWriteApi() == null){
            influx.open_write_api();
            if(influx.getWriteApi() == null)
                return Result.failure(output);
        }

        if(!influx.ping()){
            return Result.failure(output);
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(iperf3LineProtocolFile));
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
            return Result.failure(output);
        }
        List<String> points = br.lines().collect(Collectors.toList());
        try {
            Log.d(TAG, String.format("doWork: uploading %s", iperf3LineProtocolFile));
            influx.writeRecords(points);
        } catch (IOException e) {
            Log.d(TAG, String.format("doWork: upload of %s failed!", iperf3LineProtocolFile));
            return Result.failure(output);
        }


        influx.flush();

        output = new Data.Builder().putBoolean("iperf3_upload", true).build();
        return Result.success(output);
    }
}
