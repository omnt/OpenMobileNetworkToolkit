/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs;

import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME;
import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME;

import android.content.ComponentName;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;


import com.google.gson.GsonBuilder;


import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Monitor.Iperf3MonitorServiceWorkerFour;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Monitor.Iperf3MonitorServiceWorkerOne;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Monitor.Iperf3MonitorServiceWorkerThree;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Monitor.Iperf3MonitorServiceWorkerTwo;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3MonitorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Executor.Iperf3ServiceWorkerFour;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Executor.Iperf3ServiceWorkerThree;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Executor.Iperf3ServiceWorkerTwo;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Executor.Iperf3ServiceWorkerOne;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;


public class Iperf3Input extends Inputs {

    private static final String TAG = "Iperf3Input";
    public static final String IPERF3UUID = "iPerf3UUID";

    private Iperf3Parameter iperf3Parameter;
    protected Iperf3Input(Parcel in) {
        super(in);
        iperf3Parameter = in.readParcelable(Iperf3Parameter.class.getClassLoader());
    }


    public static final Creator<Iperf3Input> CREATOR = new Creator<>() {
        @Override
        public Iperf3Input createFromParcel(Parcel in) {
            return new Iperf3Input(in);
        }

        @Override
        public Iperf3Input[] newArray(int size) {
            return new Iperf3Input[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
//        parcel.writeParcelable(iperf3Parameter, i);
    }
    public Iperf3Input(Iperf3Parameter iperf3Parameter, String testUUID) {
        this(iperf3Parameter, testUUID, "", "", "");
    }
    public Iperf3Input(Iperf3Parameter iperf3Parameter,
                       String testUUID,
                       String sequenceUUID,
                       String measurementUUID,
                       String campaignUUID) {
        super(testUUID, sequenceUUID, measurementUUID, campaignUUID, iperf3Parameter);
        this.iperf3Parameter = iperf3Parameter;
    }

    @Override
    public Iperf3Parameter getParameter() {
        return this.iperf3Parameter;
    }


    public Data.Builder getInputAsDataBuilder(int workerProcess, String packageName, String serviceName) {
        Data.Builder data = new Data.Builder();
        ComponentName componentName = new ComponentName(packageName, serviceName);
        data.putString(ARGUMENT_PACKAGE_NAME, componentName.getPackageName());
        data.putString(ARGUMENT_CLASS_NAME, componentName.getClassName());
        data.putInt(NOTIFICATIONUMBER, workerProcess);
        data.putString(Inputs.INPUT, new GsonBuilder().create().toJson(this, Iperf3Input.class));
        Log.d(TAG, "getInputAsDataBuilder: ARGUMENT_PACKAGE_NAME: " + componentName.getPackageName());
        Log.d(TAG, "getInputAsDataBuilder: ARGUMENT_CLASS_NAME: " + componentName.getClassName());
        Log.d(TAG, "getInputAsDataBuilder: workerProcess: " + workerProcess);
        Log.d(TAG, "getInputAsDataBuilder: testUUID: " + getTestUUID());
        return data;
    }
    
    

    public Data.Builder getInputAsDataBuilder(int i) {
        Data.Builder data = new Data.Builder();
        data.putInt(NOTIFICATIONUMBER, i);
        data.putString(Inputs.INPUT, new GsonBuilder().create().toJson(this, Iperf3Input.class));
        return data;
    }

    @Override
    public OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName) {

        String serviceName = "";
        switch (i){
            case 0:
                serviceName = Iperf3ServiceWorkerOne.class.getName();
                break;
            case 1:
                serviceName = Iperf3ServiceWorkerTwo.class.getName();
                break;
            case 2:
                serviceName = Iperf3ServiceWorkerThree.class.getName();
                break;
            case 3:
                serviceName = Iperf3ServiceWorkerFour.class.getName();
                break;
        }

        return new OneTimeWorkRequest.Builder(Iperf3ExecutorWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(Iperf3ExecutorWorker.TAG)
                .addTag(getParameter().getiPerf3UUID())
                .setInputData(getInputAsDataBuilder(i, packageName, serviceName).build())
                .build();
    }

    @Override
    public OneTimeWorkRequest getWorkRequestMonitor(int i, String packageName) {
        String serviceName = "";
        switch (i){
            case 0:
                serviceName = Iperf3MonitorServiceWorkerOne.class.getName();
                break;
            case 1:
                serviceName = Iperf3MonitorServiceWorkerTwo.class.getName();
                break;
            case 2:
                serviceName = Iperf3MonitorServiceWorkerThree.class.getName();
                break;
            case 3:
                serviceName = Iperf3MonitorServiceWorkerFour.class.getName();
                break;
        }
        return new OneTimeWorkRequest.Builder(Iperf3MonitorWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(Iperf3MonitorWorker.TAG)
                .addTag(getParameter().getiPerf3UUID())
                .setInputData(getInputAsDataBuilder(i, packageName, serviceName).build())
                .build();
    }

    @Override
    public OneTimeWorkRequest getWorkRequestLineProtocol(int i, String packageName) {
        return new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(Iperf3ToLineProtocolWorker.TAG)
                .addTag(getParameter().getiPerf3UUID())
                .setInputData(getInputAsDataBuilder(i).build())
                .build();
    }

    @Override
    public OneTimeWorkRequest getWorkRequestUpload(int i, String packageName) {
        return new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .addTag(Iperf3UploadWorker.TAG)
                .addTag(getParameter().getiPerf3UUID())
                .setInputData(getInputAsDataBuilder(i).build())
                .build();
    }
}