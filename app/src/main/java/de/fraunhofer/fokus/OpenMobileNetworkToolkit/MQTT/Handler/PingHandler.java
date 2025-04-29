/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;

public class PingHandler extends Handler {

    public static final String TAG = "PingHandler";
    private ArrayList<PingInput> pingInputs = new ArrayList<>();
    private boolean isEnable = false;
    private RemoteWorkContinuation workContinuation;
    @Override
    public void parsePayload(String payload) throws JSONException {
        pingInputs.clear();
        JSONArray tests = new JSONArray(payload);
        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            String testUUID = test.getString("testUUID");
            String measurementUUUID = test.getString("measurementUUID");
            String sequenceUUID = test.getString("sequenceUUID");
            String campaignUUID = test.getString("campaignUUID");
            String device = test.getString("device");
            String testType = test.getString("type");
            if(!testType.equals("ping")) continue;
            JSONObject params = test.getJSONObject("params");

            PingParameter pingParameter = new PingParameter(params, testUUID);
            if(pingParameter == null) continue;
            PingInput pingInput = new PingInput(pingParameter, testUUID, sequenceUUID, measurementUUUID,campaignUUID);
            File logFile = new File(pingInput.getParameter().getLogfile());
            if(logFile.exists()) {
                logFile.delete();
            }
            pingInputs.add(pingInput);

        }

    }

    public ArrayList<String> getTestUUIDs() {
        ArrayList<String> testUUIDs = new ArrayList<>();
        for(PingInput pingInput : pingInputs) {
            testUUIDs.add(pingInput.getTestUUID());
        }
        return testUUIDs;
    }

    @Override
    public ArrayList<OneTimeWorkRequest> getExecutorWorkRequests(Context context) {
        ArrayList<OneTimeWorkRequest> executorWorkRequests = new ArrayList<>();
        for(PingInput pingInput : pingInputs) {
            executorWorkRequests.add(pingInput.getWorkRequestExecutor(pingInputs.indexOf(pingInput), context.getPackageName()));
        }
        return executorWorkRequests;
    }

    @Override
    public ArrayList<OneTimeWorkRequest> getMonitorWorkRequests(Context context) {
        return null;
    }

    @Override
    public ArrayList<OneTimeWorkRequest> getToLineProtocolWorkRequests(Context context) {
        ArrayList<OneTimeWorkRequest> toLineProtocolWorkRequests = new ArrayList<>();
        for(PingInput pingInput : pingInputs) {
            toLineProtocolWorkRequests.add(pingInput.getWorkRequestLineProtocol(pingInputs.indexOf(pingInput), context.getPackageName()));
        }
        return toLineProtocolWorkRequests;
    }
    @Override
    public ArrayList<OneTimeWorkRequest> getUploadWorkRequests(Context context) {
        ArrayList<OneTimeWorkRequest> uploadWorkRequests = new ArrayList<>();
        for(PingInput pingInput : pingInputs) {
            uploadWorkRequests.add(pingInput.getWorkRequestUpload(pingInputs.indexOf(pingInput), context.getPackageName()));
        }
        return uploadWorkRequests;
    }
    @Override
    public void preperareSequence(Context context) {
        workContinuation = RemoteWorkManager.getInstance(context).beginWith(getExecutorWorkRequests(context)).then(getToLineProtocolWorkRequests(context)).then(getUploadWorkRequests(context));
    }
    @Override
    public void enableSequence() {
        if(workContinuation == null){
            return;
        }
        workContinuation.enqueue();
    }

    @Override
    public void disableSequence(Context context) {


    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
