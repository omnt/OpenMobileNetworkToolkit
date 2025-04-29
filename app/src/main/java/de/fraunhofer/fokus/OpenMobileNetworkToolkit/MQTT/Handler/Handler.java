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
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.multiprocess.RemoteWorkContinuation;
import androidx.work.multiprocess.RemoteWorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;

abstract public class Handler {
    private final String TAG = "Handler";
    abstract public void parsePayload(String payload) throws JSONException;

    public Handler() {
    }
    abstract public ArrayList<OneTimeWorkRequest> getExecutorWorkRequests(Context context);

    abstract public ArrayList<OneTimeWorkRequest> getMonitorWorkRequests(Context context);

    abstract public ArrayList<OneTimeWorkRequest> getToLineProtocolWorkRequests(Context context);

    abstract public ArrayList<OneTimeWorkRequest> getUploadWorkRequests(Context context);

    abstract public void preperareSequence(Context context);

    abstract public void enableSequence();

    abstract public void disableSequence(Context context);
}
