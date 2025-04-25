/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Inputs;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;

public class InfluxDB2xUploadWorker extends Worker {
    public static final String TAG = "InfDB2xUploadWorker";
    InfluxdbConnection influx;
    private Inputs input;
    public static final String UPLOAD = "influxdb2x_upload";

    public InfluxDB2xUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String inputString = getInputData().getString(Inputs.INPUT);
        input = gson.fromJson(inputString, Inputs.class);
    }
    private void setup(){
        influx = InfluxdbConnections.getRicInstance(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        setup();
        Data output = new Data.Builder().putBoolean(UPLOAD, false).build();
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
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(input.getParameter().getLineProtocolFile()));
        } catch (FileNotFoundException | NullPointerException e) {
            Log.d(TAG,e.toString());
            return Result.failure(output);
        }
        List<String> points = br.lines().collect(Collectors.toList());
        try {
            Log.d(TAG, String.format("doWork: uploading %s", input.getParameter().getLineProtocolFile()));
            influx.writeRecords(points);
        } catch (IOException e) {
            Log.d(TAG, String.format("doWork: upload of %s failed!", input.getParameter().getLineProtocolFile()));
            return Result.failure(output);
        }


        influx.flush();

        output = new Data.Builder().putBoolean(UPLOAD, true).build();
        return Result.success(output);
    }
}
