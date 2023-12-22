/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PingWorker extends Worker {

    private static final String TAG = "PingWorker";
    String host;
    Runtime runtime;
    private ArrayList<String> lines;
    private Process pingProcess;
    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        runtime = Runtime.getRuntime();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: worker stopped!");
        if(pingProcess.isAlive()) pingProcess.destroy();

    }

    @NonNull
    @Override
    public Result doWork() {
        lines = new ArrayList<>();
        Data data = null;
        try {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/system/bin/ping ")
                .append("-D ")
                .append(getInputData().getString("input"));

            pingProcess = runtime.exec(stringBuilder.toString());

            BufferedReader outputReader =
                new BufferedReader(new InputStreamReader(pingProcess.getInputStream()));
            String line = null;
            while((line = outputReader.readLine()) != null){
                lines.add(line);
                setProgressAsync(new Data.Builder().putString("ping_line", line).build());
            }

            data = new Data.Builder().putStringArray("output", lines.toArray(new String[0]))
                .build();

            if(isStopped()){
                Log.d(TAG, "doWork: got cancelled because Worker got stopped!");
                return Result.success(data);
            }


            int result = pingProcess.waitFor();
            Log.d(TAG, "doWork: result " + result);
            if (result != 0) {
                return Result.failure();
            }
            data = new Data.Builder().putStringArray("output", lines.toArray(new String[0]))
                .build();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf(e.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Result.success(data);
    }
}
