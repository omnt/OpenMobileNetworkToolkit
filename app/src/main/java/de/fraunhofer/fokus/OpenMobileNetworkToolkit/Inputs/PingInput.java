package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs;

import android.os.Parcel;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.google.gson.Gson;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.Worker.InfluxDB2xUploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingWorker;


public class PingInput extends Inputs {
    PingParameter pingParameter;
    public static final String rawDirPath = rootPath+"/omnt/ping/raw/";
    public static final String lineProtocolDirPath = rootPath+"/omnt/ping/lineprotocol/";
    protected PingInput(Parcel in) {
        super(in);

    }
    public PingInput(PingParameter pingParameter,
                       String testUUID,
                       String sequenceUUID,
                       String measurementUUID,
                       String campaignUUID) {
        super(testUUID, sequenceUUID, measurementUUID, campaignUUID);
        super.setRawFile(rawDirPath +testUUID+".json");
        super.setLineProtocolFile(lineProtocolDirPath+testUUID+".txt");
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
        data.putString(INPUT, gson.toJson(pingParameter));
        return data;
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
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(Iperf3ToLineProtocolWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .setInputData(getInputAsDataBuilder(i,  packageName).build())
                .build();
        return workRequest;
    }

    @Override
    public OneTimeWorkRequest getWorkRequestUpload(int i, String packageName) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(InfluxDB2xUploadWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .setInputData(getInputAsDataBuilder(i, packageName).build())
                .build();
        return workRequest;
    }

    @Override
    public OneTimeWorkRequest getWorkRequestExecutor(int i, String packageName) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(PingWorker.class)
                .addTag(super.getTestUUID())
                .addTag(super.getMeasurementUUID())
                .addTag(super.getSequenceUUID())
                .addTag(super.getCampaignUUID())
                .setInputData(getInputAsDataBuilder(i,  packageName).build())
                .build();
        return workRequest;
    }
}
