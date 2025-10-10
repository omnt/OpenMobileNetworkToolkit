/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import java.sql.Timestamp;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Parameter;

public class Inputs {


    public static final String INPUT = "input";
    public static final String TESTUUID = "testUUID";
    public static final String SEQUENCEUUID = "sequenceUUID";
    public static final String MEASUREMENTUUID = "measurementUUID";
    public static final String CAMPAIGNUUID = "campaignUUID";
    public static final String DEVICE = "device";
    public static final String TYPE = "type";
    public static final String PARAMS = "params";
    public static final String NOTIFICATIONUMBER = "notificationNumber";
    private String testUUID;

    private Timestamp timestamp;
    private String campaignUUID;
    private String sequenceUUID;
    private String measurementUUID;
    private Parameter parameter;


    public String getTestUUID() {
        return testUUID;
    }

    public void setTestUUID(String testUUID) {
        this.testUUID = testUUID;
    }


    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getCampaignUUID() {
        return campaignUUID;
    }

    public String getSequenceUUID() {
        return sequenceUUID;
    }

    public String getMeasurementUUID() {
        return measurementUUID;
    }


    public Inputs(String testUUID, String sequenceUUID, String measurementUUID, String campaignUUID, Parameter parameter) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.campaignUUID = campaignUUID;
        this.sequenceUUID = sequenceUUID;
        this.measurementUUID = measurementUUID;
        this.testUUID = testUUID;
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Data.Builder getInputAsDataBuilder(int i, String packageName) {
        return null;
    }

    public OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName) {
        return null;
    }

    public OneTimeWorkRequest getWorkRequestMonitor(int i, String packageName) {
        return null;
    }

    public OneTimeWorkRequest getWorkRequestLineProtocol(int i, String packageName) {
        return null;
    }

    public OneTimeWorkRequest getWorkRequestUpload(int i, String packageName) {
        return null;
    }

}
