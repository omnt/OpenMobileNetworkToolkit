package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import java.sql.Timestamp;

public abstract class Inputs implements Parcelable {

    public static final String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    public static final String INPUT = "input";
    public static final String TESTUUID = "testUUID";
    public static final String SEQUENCEUUID = "sequenceUUID";
    public static final String MEASUREMENTUUID = "measurementUUID";
    public static final String CAMPAIGNUUID = "campaignUUID";
    private String rawFile;
    private String testUUID;
    private String logFileName;
    private String measurementName;
    private String lineProtocolFile;
    private Timestamp timestamp;
    private String campaignUUID;
    private String sequenceUUID;
    private String measurementUUID;

    public String getRawFile() {
        return rawFile;
    }

    public void setRawFile(String rawFile) {
        this.rawFile = rawFile;
    }

    public String getTestUUID() {
        return testUUID;
    }

    public void setTestUUID(String testUUID) {
        this.testUUID = testUUID;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    public String getLineProtocolFile() {
        return lineProtocolFile;
    }

    public void setLineProtocolFile(String lineProtocolFile) {
        this.lineProtocolFile = lineProtocolFile;
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
    protected Inputs(Parcel in) {
        rawFile = in.readString();
        logFileName = in.readString();
        measurementName = in.readString();
        lineProtocolFile = in.readString();
        timestamp = (Timestamp) in.readSerializable();
        campaignUUID = in.readString();
        sequenceUUID = in.readString();
        measurementUUID = in.readString();
        testUUID = in.readString();
    }
    public Inputs(String rawFile, String logFileName, String measurementName, String lineProtocolFile, String campaignUUID, String sequenceUUID, String measurementUUID, String testUUID) {

        this.rawFile = rawFile;
        this.logFileName = logFileName;
        this.measurementName = measurementName;
        this.lineProtocolFile = lineProtocolFile;
        this.timestamp = new Timestamp(System.currentTimeMillis());

        this.campaignUUID = campaignUUID;
        this.sequenceUUID = sequenceUUID;
        this.measurementUUID = measurementUUID;
        this.testUUID = testUUID;
    }

    public Inputs(String campaignUUID, String sequenceUUID, String measurementUUID, String testUUID) {
        this.rawFile = "";
        this.logFileName = "";
        this.measurementName = "";
        this.lineProtocolFile = "";
        this.timestamp = new Timestamp(System.currentTimeMillis());

        this.campaignUUID = campaignUUID;
        this.sequenceUUID = sequenceUUID;
        this.measurementUUID = measurementUUID;
        this.testUUID = testUUID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(rawFile);
        dest.writeString(logFileName);
        dest.writeString(measurementName);
        dest.writeString(lineProtocolFile);
        dest.writeSerializable(timestamp);
        dest.writeString(campaignUUID);
        dest.writeString(sequenceUUID);
        dest.writeString(measurementUUID);
        dest.writeString(testUUID);
    }
    public abstract Data.Builder getInputAsDataBuilder(int i, String packageName);
    public abstract OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName);
    public abstract OneTimeWorkRequest getWorkRequestLineProtocol(int i, String packageName);
    public abstract OneTimeWorkRequest getWorkRequestUpload(int i, String packageName);

}
