package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;

public class PingHandler extends Handler {

    private ArrayList<PingInput> pingInputs = new ArrayList<>();
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

            PingParameter pingParameter = new PingParameter(params);
            if(pingParameter == null) continue;
            PingInput pingInput = new PingInput(pingParameter, testUUID, sequenceUUID, measurementUUUID,campaignUUID);
            pingInputs.add(pingInput);
        }
    }

    @Override
    public void enableSequence(Context context) {

        WorkManager workManager = WorkManager.getInstance(context);
        ArrayList<OneTimeWorkRequest> executorWorkRequests = new ArrayList<>();
        ArrayList<OneTimeWorkRequest> toLineProtocolWorkRequests = new ArrayList<>();
        ArrayList<OneTimeWorkRequest> uploadWorkRequests = new ArrayList<>();
        for(PingInput pingInput : pingInputs) {
            executorWorkRequests.add(pingInput.getWorkRequestExecutor(pingInputs.indexOf(pingInput), context.getPackageName()));
            toLineProtocolWorkRequests.add(pingInput.getWorkRequestLineProtocol(pingInputs.indexOf(pingInput), context.getPackageName()));
            uploadWorkRequests.add(pingInput.getWorkRequestUpload(pingInputs.indexOf(pingInput), context.getPackageName()));
        }

        workManager.beginWith(executorWorkRequests)
                .then(toLineProtocolWorkRequests)
                .then(uploadWorkRequests)
                .enqueue();
    }

    @Override
    public void disableSequence(Context context) {

    }
}
