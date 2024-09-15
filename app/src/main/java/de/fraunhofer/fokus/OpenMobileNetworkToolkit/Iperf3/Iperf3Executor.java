package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ExecutorWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3ToLineProtocolWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Worker.Iperf3UploadWorker;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class Iperf3Executor {
    private static final String TAG = "Iperf3Executor";
    private WorkManager workManager;
    private Context context;
    private Iperf3Input iperf3Input;
    private Data.Builder dataBuilder;
    private Iperf3RunResultDao iperf3RunResultDao;
    private SharedPreferencesGrouper spg;
    private OneTimeWorkRequest iperf3WR;
    private OneTimeWorkRequest iperf3LP;
    private OneTimeWorkRequest iperf3UP;


    public Iperf3Executor(Context context, Iperf3Input iperf3Input) {
        this.context = context;
        spg = SharedPreferencesGrouper.getInstance(context);
        workManager = WorkManager.getInstance(context);
        iperf3RunResultDao = Iperf3ResultsDataBase.getDatabase(context).iperf3RunResultDao();
        this.iperf3Input = iperf3Input;
        this.dataBuilder = this.iperf3Input.getInputAsDataBuilder();


        iperf3WR = new OneTimeWorkRequest
                        .Builder(Iperf3ExecutorWorker.class)
                        .setInputData(this.dataBuilder.build())
                        .addTag("iperf3Run")
                        .addTag(iperf3Input.getUuid())
                        .build();
        iperf3LP = new OneTimeWorkRequest
                        .Builder(Iperf3ToLineProtocolWorker.class)
                        .setInputData(this.dataBuilder.build())
                        .build();
        iperf3UP = new OneTimeWorkRequest
                        .Builder(Iperf3UploadWorker.class)
                        .setInputData(this.dataBuilder.build())
                        .addTag("iperf3")
                        .build();
        iperf3RunResultDao.insert(new Iperf3RunResult(this.iperf3Input.getUuid(),
                -100,
                false,
                this.iperf3Input,
                this.iperf3Input.getTimestamp()));




        workManager.getWorkInfoByIdLiveData(iperf3WR.getId()).observeForever(workInfo -> {
            int iperf3_result;
            iperf3_result = workInfo.getOutputData().getInt("iperf3_result", -100);
            if (workInfo.getState().equals(WorkInfo.State.CANCELLED)) {
                iperf3_result = -1;
            }
            iperf3RunResultDao.updateResult(this.iperf3Input.getUuid(), iperf3_result);
            Log.d(TAG, "onChanged: iperf3_result: " + iperf3_result);
            /*if (iperf3_result == -100) {
                progressIndicator.setVisibility(LinearProgressIndicator.VISIBLE);
                if (!isModeSpinnerClient()) {
                    progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);
                }
            } else if (iperf3_result != 0) {
                progressIndicator.setIndicatorColor(failedColors);
                progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);

            } else {
                progressIndicator.setIndicatorColor(succesColors);
                progressbarHandler.postDelayed(progressbarUpdate, SHOW_PROGRESSBAR);
            }*/
        });

        workManager.getWorkInfoByIdLiveData(iperf3UP.getId()).observeForever(workInfo -> {
            boolean iperf3_upload;
            iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
            Log.d(TAG, "onChanged: iperf3_upload: " + iperf3_upload);
            iperf3RunResultDao.updateUpload(this.iperf3Input.getUuid(), iperf3_upload);
        });

    }

    public void start() {
        if (spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)) {
            workManager.beginWith(iperf3WR).then(iperf3LP).then(iperf3UP).enqueue();
        } else {
            workManager.beginWith(iperf3WR).then(iperf3LP).enqueue();
        }
    }

    public void stop(){
        workManager.cancelAllWorkByTag(this.iperf3Input.getUuid());
    }

}
