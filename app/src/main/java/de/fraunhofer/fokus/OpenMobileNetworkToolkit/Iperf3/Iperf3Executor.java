package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;

import androidx.work.Data;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;

public class Iperf3Executor {
    private static final String TAG = "Iperf3Executor";
    private Iperf3ExecutorWorker iperf3ExecutorWorker;
    private Iperf3ToLineProtocolWorker iperf3ToLineProtocolWorker;
    private Iperf3UploadWorker iperf3UploadWorker;
    private Context context;
    private Iperf3Input iperf3Input;
    private Data.Builder dataBuilder;

    public Iperf3Executor(Context context, WorkerParameters workerParameters,
                          Iperf3Input iperf3Input) {
        this.iperf3ExecutorWorker = new Iperf3ExecutorWorker(context, workerParameters);
        this.iperf3ToLineProtocolWorker = new Iperf3ToLineProtocolWorker(context, workerParameters);
        this.iperf3UploadWorker = new Iperf3UploadWorker(context, workerParameters);
        this.context = context;
        this.iperf3Input = iperf3Input;
        this.dataBuilder = this.iperf3Input.getInputAsDataBuilder();


        this.iperf3ExecutorWorker
    }

    public void startIperf3ExecuterWorker() {
        iperf3ExecutorWorker.startIperf3ExecuterWorker();
    }

    public void startIperf3ToLineProtocolWorker() {
        iperf3ToLineProtocolWorker.startIperf3ToLineProtocolWorker();
    }

    public void startIperf3UploadWorker() {
        iperf3UploadWorker.startIperf3UploadWorker();
    }

    public void stopIperf3ExecuterWorker() {
        iperf3ExecutorWorker.stopIperf3ExecuterWorker();
    }


}
