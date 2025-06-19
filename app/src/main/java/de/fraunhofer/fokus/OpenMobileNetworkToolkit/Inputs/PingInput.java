/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs;

import android.os.Parcel;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.google.gson.Gson;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker.InfluxDB2xUploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker.PingToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker.PingWorker;


public class PingInput extends Inputs {
    private PingParameter pingParameter;

    protected PingInput(Parcel in) {
        super(in);

    }
    public PingInput(PingParameter pingParameter, String testUUID){
        super(testUUID, "","","", pingParameter);
        this.pingParameter = pingParameter;
    }
    public PingInput(PingParameter pingParameter,
                       String testUUID,
                       String sequenceUUID,
                       String measurementUUID,
                       String campaignUUID) {
        super(testUUID, sequenceUUID, measurementUUID, campaignUUID, pingParameter);
        this.pingParameter = pingParameter;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public Data.Builder getInputAsDataBuilder(int i, String packageName) {
        Data.Builder data = new Data.Builder();
        Gson gson = new Gson();
        data.putInt(NOTIFICATIONUMBER, i);
        data.putString(INPUT, gson.toJson(this));
        return data;
    }

    public PingParameter getPingParameter() {
        return pingParameter;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PingInput> CREATOR = new Creator<PingInput>() {
        @Override
        public PingInput createFromParcel(Parcel in) {
            return new PingInput(in);
        }

        @Override
        public PingInput[] newArray(int size) {
            return new PingInput[size];
        }
    };


    @Override
    public OneTimeWorkRequest getWorkRequestLineProtocol(int i, String packageName) {
        // TODO FIX
        return new OneTimeWorkRequest.Builder(PingToLineProtocolWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(PingToLineProtocolWorker.TAG) // TODO FIX
                .setInputData(getInputAsDataBuilder(i,  packageName).build())
                .build();
    }

    @Override
    public OneTimeWorkRequest getWorkRequestUpload(int i, String packageName) {
        return new OneTimeWorkRequest.Builder(InfluxDB2xUploadWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(InfluxDB2xUploadWorker.TAG)
                .setInputData(getInputAsDataBuilder(i, packageName).build())
                .build();
    }

    @Override
    public OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName) {
        return new OneTimeWorkRequest.Builder(PingWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(PingWorker.TAG)
                .setInputData(getInputAsDataBuilder(i,  packageName).build())
                .build();
    }
}
