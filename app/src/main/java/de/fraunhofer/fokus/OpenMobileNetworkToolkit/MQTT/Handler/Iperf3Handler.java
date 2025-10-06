/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3ResultsDataBase;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResultDao;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;

public class Iperf3Handler extends Handler {
    private final String TAG = "Iperf3Handler";
    private ArrayList<Iperf3Input> iperf3Inputs;
    private boolean isEnable = false;
    private ArrayList<RemoteWorkContinuation> continuations;
    private Iperf3RunResultDao iperf3RunResultDao;
    private ArrayList<OneTimeWorkRequest> executorWorkRequests;
    private ArrayList<OneTimeWorkRequest> monitorWorkRequests;
    private ArrayList<OneTimeWorkRequest> lineProtocolWorkRequests;
    private ArrayList<OneTimeWorkRequest> uploadWorkRequests;


    @Override
    public void parsePayload(String payload) throws JSONException {
        iperf3Inputs = new ArrayList<>();
        JSONArray tests = new JSONArray(payload);
        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            String testUUID = test.getString("testUUID");
            String measurementUUUID = test.getString("measurementUUID");
            String sequenceUUID = test.getString("sequenceUUID");
            String campaignUUID = test.getString("campaignUUID");
            String device = test.getString("device");
            String testType = test.getString("type");
            if(!testType.equals("iperf3")) continue;
            JSONObject params = test.getJSONObject("params");

            Iperf3Parameter iperf3Parameter = new Iperf3Parameter(super.getRootFilePath(), params, testUUID);
            if(iperf3Parameter == null) continue;
            Iperf3Input iperf3Input = new Iperf3Input(iperf3Parameter, testUUID, sequenceUUID, measurementUUUID,campaignUUID);
            iperf3Input.setTimestamp(new Timestamp(System.currentTimeMillis()));
            iperf3Inputs.add(iperf3Input);

            Iperf3RunResult iperf3RunResult = new Iperf3RunResult(iperf3Input.getTestUUID(), -100, false, iperf3Input, new java.sql.Timestamp(System.currentTimeMillis()));
            iperf3RunResultDao.insert(iperf3RunResult);
            File logFile = new File(iperf3Input.getParameter().getRawLogFilePath());
            if(logFile.exists()) {
                logFile.delete();
            }

        }
    }

    public ArrayList<String> getTestUUIDs() {
        ArrayList<String> testUUIDs = new ArrayList<>();
        for(Iperf3Input iperf3Input : iperf3Inputs) {
            testUUIDs.add(iperf3Input.getTestUUID());
        }
        return testUUIDs;
    }
    public Iperf3Handler(Context context) {
        super(context);
        iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(context).iperf3RunResultDao();
    }
    @Override
    public ArrayList<OneTimeWorkRequest> getExecutorWorkRequests(Context context) {
        if(executorWorkRequests != null && !executorWorkRequests.isEmpty()) return executorWorkRequests;
        executorWorkRequests = new ArrayList<>();
        for(Iperf3Input iperf3Input : iperf3Inputs) {
            executorWorkRequests.add(iperf3Input.getWorkRequestExecutor(iperf3Inputs.indexOf(iperf3Input), context.getPackageName()));
        }
        return executorWorkRequests;
    }
    @Override
    public ArrayList<OneTimeWorkRequest> getMonitorWorkRequests(Context context) {
        if(monitorWorkRequests != null && !monitorWorkRequests.isEmpty()) return monitorWorkRequests;
        monitorWorkRequests = new ArrayList<>();
        for(Iperf3Input iperf3Input : iperf3Inputs) {
            monitorWorkRequests.add(iperf3Input.getWorkRequestMonitor(iperf3Inputs.indexOf(iperf3Input), context.getPackageName()));
        }
        return monitorWorkRequests;
    }
    @Override
    public ArrayList<OneTimeWorkRequest> getToLineProtocolWorkRequests(Context context) {
        if(lineProtocolWorkRequests != null && !lineProtocolWorkRequests.isEmpty()) return lineProtocolWorkRequests;
        lineProtocolWorkRequests = new ArrayList<>();
        for(Iperf3Input iperf3Input : iperf3Inputs) {
            lineProtocolWorkRequests.add(iperf3Input.getWorkRequestLineProtocol(iperf3Inputs.indexOf(iperf3Input), context.getPackageName()));
        }
        return lineProtocolWorkRequests;
    }
    @Override
    public ArrayList<OneTimeWorkRequest> getUploadWorkRequests(Context context) {
        if(uploadWorkRequests != null && !uploadWorkRequests.isEmpty()) return uploadWorkRequests;
        uploadWorkRequests = new ArrayList<>();
        for(Iperf3Input iperf3Input : iperf3Inputs) {
            uploadWorkRequests.add(iperf3Input.getWorkRequestUpload(iperf3Inputs.indexOf(iperf3Input), context.getPackageName()));
        }
        return uploadWorkRequests;
    }

    @Override
    public void preperareSequence(Context context){
        continuations = new ArrayList<>();
        Log.d(TAG, "enableSequence: called!");
        if(iperf3Inputs.isEmpty()) {
            Log.e(TAG, "No iperf3 tests to run");
            return;
        };

        ArrayList<ArrayList<OneTimeWorkRequest>> workRequestss = new ArrayList<>();
        RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(context);
        for(Iperf3Input iperf3Input: iperf3Inputs){
            remoteWorkManager.cancelAllWorkByTag(iperf3Input.getTestUUID());
            ArrayList<OneTimeWorkRequest> workRequests = new ArrayList<>();
            int i = iperf3Inputs.indexOf(iperf3Input);
            workRequests.add(iperf3Input.getWorkRequestExecutor(i, context.getPackageName()));
            workRequests.add(iperf3Input.getWorkRequestMonitor(i, context.getPackageName()));
            workRequests.add(iperf3Input.getWorkRequestLineProtocol(i, context.getPackageName()));
            workRequests.add(iperf3Input.getWorkRequestUpload(i, context.getPackageName()));
            workRequestss.add(workRequests);
            Log.d(TAG, "enableSequence: workRequests size: "+workRequests.size());
        }


        Log.d(TAG, "enableSequence: workRequestss size: "+workRequestss.size());
        for(ArrayList<OneTimeWorkRequest> workRequests: workRequestss){
            RemoteWorkContinuation remoteWorkContinuation = remoteWorkManager.beginWith(workRequests.subList(0, 2)).then(workRequests.get(2)).then(workRequests.get(3));
            continuations.add(remoteWorkContinuation);
        }
        Log.d(TAG, "enableSequence: continuations size: "+continuations.size());
    }

    @Override
    public void enableSequence(){

        for(RemoteWorkContinuation continuation: continuations){
            continuation.enqueue();
        }
    }

    @Override
    public void disableSequence(Context context){
        RemoteWorkManager workManager = RemoteWorkManager.getInstance(context);
        for(Iperf3Input iperf3Input: iperf3Inputs){
            workManager.cancelAllWorkByTag(iperf3Input.getTestUUID());
        }
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}





