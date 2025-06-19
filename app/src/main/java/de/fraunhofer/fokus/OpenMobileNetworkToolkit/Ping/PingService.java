/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import java.util.UUID;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker.InfluxDB2xUploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker.PingToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker.PingWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class PingService extends Service {
    private static final String TAG = "PingService";
    public static final String PING_INTENT_COMMAND = "ping_intent_command";
    public static final String PING_INTENT_ENABLE = "ping_intent_enable";
    public static final String PING_LAST_UUID = "ping_last_uuid";
    private SharedPreferencesGrouper spg;
    private WorkManager workManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Stop logging service");
        spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
        stopWorker();
    }


    private void startWorker(String command) {
        String uuid = UUID.randomUUID().toString();

        PingParameter pingParameter = new PingParameter(command, uuid);
        PingInput pingInput = new PingInput(pingParameter, uuid);
        String gson = new Gson().toJson(pingInput, PingInput.class);
        Data data = new Data.Builder()
                .putString(PingInput.INPUT, gson)
                .build();
        OneTimeWorkRequest pingWR = new OneTimeWorkRequest.Builder(PingWorker.class)
                .setInputData(data)
                .addTag(uuid)
                .addTag(PingWorker.TAG)
                .build();
        OneTimeWorkRequest pingToLineProtocolWR = new OneTimeWorkRequest.Builder(PingToLineProtocolWorker.class)
                .setInputData(data)
                .addTag(uuid)
                .addTag(PingToLineProtocolWorker.TAG)
                .build();

        spg.getSharedPreference(SPType.ping_sp).edit().putString(PING_LAST_UUID, pingWR.getId().toString()).apply();
        registerObserver(command);
        WorkContinuation workContinuation = workManager.beginWith(pingWR).then(pingToLineProtocolWR);
        if(spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)){
            OneTimeWorkRequest influxDB2xUploadWorker = new OneTimeWorkRequest.Builder(InfluxDB2xUploadWorker.class)
                    .setInputData(data)
                    .addTag(uuid)
                    .addTag(InfluxDB2xUploadWorker.TAG)
                    .build();
            workContinuation = workContinuation.then(influxDB2xUploadWorker);
        }
        workContinuation.enqueue();

    }
    private void stopWorker(){
        String lastUUID = spg.getSharedPreference(SPType.ping_sp).getString(PING_LAST_UUID, "");
        if(lastUUID.equals("")) {
            Log.d(TAG, "stopWorker: no worker to stop!");
            return;
        }
        workManager.cancelWorkById(UUID.fromString(lastUUID));
    }


    private void registerObserver(String command){
        String lastUUID = spg.getSharedPreference(SPType.ping_sp).getString(PING_LAST_UUID, null);
        if(lastUUID != null) {
            UUID lastUUIDUUID = UUID.fromString(lastUUID);
            LiveData<WorkInfo> liveData =  workManager.getWorkInfoByIdLiveData(lastUUIDUUID);
            Observer<WorkInfo> observer = workInfo -> {
                if(workInfo == null) return;
                Log.d(TAG, "registerObserver: workInfo-State: "+workInfo.getState());
                switch (workInfo.getState()){
                    case SUCCEEDED:
                    case FAILED:
                        if(spg.getSharedPreference(SPType.ping_sp).getBoolean("repeat_ping", false)){
                            Log.d(TAG, "registerObserver: Repeat ping");
                            startWorker(command);
                        } else {
                            Log.d(TAG, "registerObserver: Stop ping service");
                            stopSelf();
                        }
//                        liveData.removeObserver(observer);
//                        liveData.removeObservers();

                        break;
                    case CANCELLED:
                    case ENQUEUED:
                    case BLOCKED:
                    case RUNNING:
                        break;
                    default:
                        break;
                }
            };
            liveData.observeForever(observer);
        }

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start ping service");
        spg = SharedPreferencesGrouper.getInstance(getApplicationContext());
        workManager = WorkManager.getInstance(getApplicationContext());
        if(intent == null){
            Log.d(TAG, "onStartCommand: Intent is null, stopping service");
            workManager.cancelAllWorkByTag(PingWorker.TAG);
            stopSelf();
            spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
            return START_NOT_STICKY;
        }
        String command = intent.getStringExtra(PING_INTENT_COMMAND);
        boolean isEnabled = intent.getBooleanExtra(PING_INTENT_ENABLE, false);
        if(!isEnabled){
            stopWorker();
            stopSelf();
            spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();
            return START_NOT_STICKY;
        }
        String lastUUID = spg.getSharedPreference(SPType.ping_sp).getString(PING_LAST_UUID, "");
        if(!lastUUID.isEmpty()){
            stopWorker();
        }

        startWorker(command);


        return START_STICKY;
    }



}

