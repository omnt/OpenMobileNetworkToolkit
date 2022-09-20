package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Iperf3MoveWorker extends Worker {

    private String logFilePath;
    private String measurementName;
    private String ip;

    private String port;
    private String bandwidth;
    private String duration;
    private String interval;
    private String bytes;

    private boolean rev;
    private boolean biDir;
    private boolean oneOff;
    private boolean client;


    public Iperf3MoveWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        logFilePath = getInputData().getString("logfilepath");

        ip = getInputData().getString("ip");
        measurementName = getInputData().getString("measurementName");

        port = getInputData().getString("port");
        bandwidth = getInputData().getString("bandwidth");
        duration = getInputData().getString("duration");
        interval = getInputData().getString("interval");
        bytes = getInputData().getString("bytes");


        rev = getInputData().getBoolean("rev", false);
        biDir = getInputData().getBoolean("biDir",false);
        oneOff = getInputData().getBoolean("oneOff",false);
        client = getInputData().getBoolean("client",false);
    }



    private static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        InputStream in = new FileInputStream(sourceLocation);

        OutputStream out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    @NonNull
    @Override
    public Result doWork() {


        Log.d("moveLogs", "Path: " + logFilePath);
        File iperf3Path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/iperf3_logs/");
        if (!iperf3Path.exists()) {
            iperf3Path.mkdir();
        }
        String[] pathSplitted = logFilePath.split("/");
        String fileName = pathSplitted[pathSplitted.length-1];
        File to = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/iperf3_logs/"+fileName);
        File from = new File(logFilePath);
        Data output = new Data.Builder().putBoolean("iperf3_move", false).build();
        try {
            copyDirectoryOneLocationToAnotherLocation(from, to);
            //from.delete();
        } catch (IOException e) {
            e.printStackTrace();
            Result.failure(output);
        }
        output = new Data.Builder().putBoolean("iperf3_move", true).build();
        return Result.success(output);
    }
}
