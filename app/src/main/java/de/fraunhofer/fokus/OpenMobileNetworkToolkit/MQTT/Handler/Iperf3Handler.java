package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.multiprocess.RemoteCoroutineWorker;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;

import com.google.common.collect.Streams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Iperf3Parameter;

public class Iperf3Handler{
    private final String TAG = "Iperf3Handler";
    private ArrayList<Iperf3Input> iperf3Inputs = new ArrayList<>();
    private void parsePayload(String payload) throws JSONException {
        iperf3Inputs.clear();
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

            Iperf3Parameter iperf3Parameter = new Iperf3Parameter(params);
            if(iperf3Parameter == null) continue;
            Iperf3Input iperf3Input = new Iperf3Input(iperf3Parameter, testUUID, sequenceUUID, measurementUUUID,campaignUUID);
            iperf3Inputs.add(iperf3Input);
        }
    }

    public Iperf3Handler(String payload) {
        try {
            parsePayload(payload);
        } catch (JSONException e){
            Log.e(TAG, "Error parsing payload: " + e.getMessage());
            return;
        }
    }



    public void enableSequence(Context context){
        if(iperf3Inputs.isEmpty()) {
            Log.e(TAG, "No iperf3 tests to run");
            return;
        };

        WorkManager.getInstance(context);
        ArrayList<ArrayList<OneTimeWorkRequest>> workRequestss = new ArrayList<>();
        RemoteWorkManager remoteWorkManager = RemoteWorkManager.getInstance(context);
        for(Iperf3Input iperf3Input: iperf3Inputs){
            ArrayList<OneTimeWorkRequest> workRequests = new ArrayList<>();
            workRequests.add(iperf3Input.getWorkRequestExecutor(iperf3Inputs.indexOf(iperf3Input), context.getPackageName()));
//            workRequests.add(iperf3Input.getWorkRequestLineProtocol());
//            workRequests.add(iperf3Input.getWorkRequestUpload());
            workRequestss.add(workRequests);

        }

        ArrayList<RemoteWorkContinuation> continuations = new ArrayList<>();

        for(ArrayList<OneTimeWorkRequest> workRequests: workRequestss){
            RemoteWorkContinuation remoteWorkContinuation = remoteWorkManager.beginWith(workRequests.get(0));//.then(workRequests.get(1)).then(workRequests.get(2));
            continuations.add(remoteWorkContinuation);
        }
        RemoteWorkContinuation mainRemoteWorkContinuation = RemoteWorkContinuation.combine(continuations);
        mainRemoteWorkContinuation.enqueue();

    }

    public void disableSequence(Context context){
        RemoteWorkManager workManager = RemoteWorkManager.getInstance(context);
        for(Iperf3Input iperf3Input: iperf3Inputs){
            workManager.cancelAllWorkByTag(iperf3Input.getTestUUID());
        }
    }
}





