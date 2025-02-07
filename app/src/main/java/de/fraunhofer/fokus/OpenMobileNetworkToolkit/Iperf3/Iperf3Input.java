package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME;
import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME;

import android.content.ComponentName;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.multiprocess.RemoteWorkerService;


import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Iperf3ServiceWorkerFour;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Iperf3ServiceWorkerThree;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Iperf3ServiceWorkerTwo;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Service.Iperf3ServiceWorkerOne;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;


public class Iperf3Input implements Parcelable {
    public static final String[] EXCLUDED_FIELDS = {
            "measurementName", "rawFile", "logFileName", "command", "lineProtocolFile",
            "context", "timestamp", "uuid", "cardView", "main", "EXCLUDED_FIELDS"
    };


    public static final String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    public static final String jsonDirPath = rootPath+"/omnt/iperf3/json/";
    public static final String lineProtocolDirPath = rootPath+"/omnt/iperf3/lineprotocol/";
    private static final String TAG = "Iperf3Input";
    public static final String IPERF3INPUT = "iperf3input";
    public static final String TESTUUID = "testUUID";
    public static final String SEQUENCEUUID = "sequenceUUID";
    public static final String MEASUREMENTUUID = "measurementUUID";
    public static final String CAMPAIGNUUID = "campaignUUID";
    public static final String IPERF3UUID = "iPerf3UUID";
    protected Iperf3Input(Parcel in) {
        rawFile = in.readString();
        iperf3Parameter = in.readParcelable(Iperf3Parameter.class.getClassLoader());
        testUUID = in.readString();
        logFileName = in.readString();
        measurementName = in.readString();
        lineProtocolFile = in.readString();
        timestamp = (Timestamp) in.readSerializable();
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

        parcel.writeString(rawFile);
        parcel.writeParcelable(iperf3Parameter, i);
        parcel.writeString(testUUID);
        parcel.writeString(logFileName);
        parcel.writeString(measurementName);
        parcel.writeString(lineProtocolFile);
        parcel.writeSerializable(timestamp);
    }
    private String rawFile;
    private Iperf3Parameter iperf3Parameter;
    private String testUUID;
    private String logFileName;
    private String measurementName;
    private String lineProtocolFile;
    private Timestamp timestamp;
    private String campaignUUID;
    private String sequenceUUID;
    private String measurementUUID;
    public Iperf3Input(Iperf3Parameter iperf3Parameter,
                       String testUUID,
                       String sequenceUUID,
                       String measurementUUID,
                       String campaignUUID) {
        try {
            this.iperf3Parameter = iperf3Parameter;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        this.testUUID = testUUID;
        this.sequenceUUID = sequenceUUID;
        this.measurementUUID = measurementUUID;
        this.campaignUUID = campaignUUID;
        this.rawFile = jsonDirPath+this.testUUID +".json";
        this.lineProtocolFile = lineProtocolDirPath+this.testUUID +".txt";
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Iperf3Parameter getIperf3Parameter() {
        return iperf3Parameter;
    }

    public String getTestUUID() {
        return testUUID;
    }

    public String getSequenceUUID() {
        return sequenceUUID;
    }

    public String getCampaignUUID() {
        return campaignUUID;
    }

    public String getMeasurementUUID() {
        return measurementUUID;
    }

    public String getRawFile() {
        return rawFile;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getLineProtocolFile() {
        return lineProtocolFile;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Data.Builder getInputAsDataBuilder(int i, String packageName) {
        Data.Builder data = new Data.Builder();
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
            default:
                break;

        }
        ComponentName componentName = new ComponentName(packageName, serviceName);

        data.putString(ARGUMENT_PACKAGE_NAME, componentName.getPackageName());
        data.putString(ARGUMENT_CLASS_NAME, componentName.getClassName());
        data.putInt("notificationNumber", i);
        data.putString(IPERF3INPUT, new GsonBuilder().create().toJson(this, Iperf3Input.class));
        return data;
    }

    public OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(Iperf3ExecutorWorker.class)
                .addTag(testUUID)
                .addTag(iperf3Parameter.getiPerf3UUID())
                .addTag(measurementUUID)
                .addTag(sequenceUUID)
                .addTag(campaignUUID)
                .setInputData(getInputAsDataBuilder(i, packageName).build())
                .build();
        return workRequest;
    }

    public OneTimeWorkRequest getWorkRequestLineProtocol() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                .addTag(testUUID)
                .addTag(iperf3Parameter.getiPerf3UUID())
                .addTag(measurementUUID)
                .addTag(sequenceUUID)
                .addTag(campaignUUID)
               // .setInputData(getInputAsDataBuilder().build())
                .build();
        return workRequest;
    }

    public OneTimeWorkRequest getWorkRequestUpload() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class)
                .addTag(testUUID)
                .addTag(iperf3Parameter.getiPerf3UUID())
                .addTag(measurementUUID)
                .addTag(sequenceUUID)
                .addTag(campaignUUID)
                //.setInputData(getInputAsDataBuilder().build())
                .build();
        return workRequest;
    }


    public static byte[] convertToBytes(Iperf3Input object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }







}