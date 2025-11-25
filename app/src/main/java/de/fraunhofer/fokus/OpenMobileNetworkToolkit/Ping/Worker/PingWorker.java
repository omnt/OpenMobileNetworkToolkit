/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PacketLossLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingParser;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class PingWorker extends Worker {

    public static final String PING = "ping";
    public static final String REASON = "reason";
    public static final String LINE = "line";
    public static final String RTT = "rtt";
    public static final String PACKET_LOSS = "packetLoss";
    public static final String UNREACHABLE = "unreachable";
    public static final String TIMEOUT = "timeout";

    public static final String TAG = "PingWorker";
    private int notificationID = 102;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private final Context ct;
    private NotificationManager notificationManager;
    private final String channelId = "OMNT_notification_channel";
    private NotificationCompat.Builder notificationBuilder;
    private double rtt; // round-trip time
    private PingInput pingInput;
    private Notification notification;
    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        ct = context;

        // Retrieve the PingInput from the Worker's input data.
        String pingInputString = getInputData().getString(PingInput.INPUT);
        pingInput = new Gson().fromJson(pingInputString, PingInput.class);

        int notificationNumber = getInputData().getInt(PingInput.NOTIFICATIONUMBER, 0);
        notificationID += notificationNumber;

        notificationManager = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(ct, channelId);

        File rawPath = new File(pingInput.getParameter().getRawDirPath());
        if(!rawPath.exists()){
            if(!rawPath.mkdirs()){
                Log.e(TAG, "Error creating rawDirPath directory: " + pingInput.getParameter().getRawDirPath());
            }
        }



        setForegroundAsync(createForegroundInfo(""));
    }

    private ForegroundInfo createForegroundInfo(String progress) {
        Intent intent = new Intent(this.ct, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("navigateToFragment", "PingFragment"); // Identifier for your fragment

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.ct,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = notificationBuilder
                .setContentTitle("Ping")
                .setContentText(progress)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
                .setContentIntent(pendingIntent) // 👈 attach pending intent here
                .build();

        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }



    @NonNull
    @Override
    public Result doWork() {
        Data.Builder output = new Data.Builder().putBoolean(PING, false);

        if (pingInput == null) {
            Log.e(TAG, "PingInput is null");
            return Result.failure(output.putString(REASON, "PingInput is null").build());
        }
        PingParameter pingParameter = pingInput.getPingParameter();
        if (pingParameter == null) {
            Log.e(TAG, "PingParameter is null");
            return Result.failure(output.putString(REASON, "PingParameter is null").build());
        }
        String[] command = pingParameter.getInputAsCommand();
        if (command == null) {
            Log.e(TAG, "Command is null");
            return Result.failure(output.putString(REASON, "Command is null").build());
        }

        Log.d(TAG, "doWork: executing " + String.join(" ", command));
        int result = -1;
        String error = "";
        try {


            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            FileOutputStream pingStream = new FileOutputStream(pingInput.getPingParameter().getRawLogFilePath(), true);
            PingParser pingParser = new PingParser();
            String line;

            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "doWork: "+line);
                pingStream.write((line + "\n").getBytes());
                pingParser.addLine(line);
                PingInformation pingInformation = pingParser.getLastPingInformation();
                Data.Builder progressOutput = new Data.Builder();
                if(line == null || pingInformation == null){
                    Log.w(TAG, "doWork: Line or PingInformation is null, skipping line");
                    continue;
                }

                switch (pingInformation.getLineType()){
                    case RTT:
                        rtt = ((RTTLine)pingInformation).getRtt();
                        progressOutput.putString(RTT, new Gson().toJson((RTTLine)pingInformation));
//                        setProgressAsync(progressOutput.build());
                        setForegroundAsync(createForegroundInfo(((RTTLine) pingInformation).getHost()+": " + rtt + " ms"));
                        Log.d(TAG, "doWork: RTT: " + rtt);
                        break;
                    case UNREACHABLE:
                        Log.e(TAG, "doWork: Unreachable destination");
                        return Result.failure(progressOutput.putString(REASON, "Unreachable destination").build());
                    case TIMEOUT:
                        Log.w(TAG, "doWork: Request timeout");
                        progressOutput.putString(REASON, "Request timeout");
                        break;
                    case PACKET_LOSS:
                        PacketLossLine packetLoss = ((PacketLossLine)pingInformation);
                        progressOutput.putString(PACKET_LOSS, new Gson().toJson(packetLoss));
//                        setProgressAsync(progressOutput.build());
                        Log.d(TAG, "doWork: Packet Loss: " + packetLoss.toString());
                        break;
                    case UNKNOWN:
                        Log.w(TAG, "doWork: Unknown line type");
                        break;
                }
                setProgressAsync(progressOutput.build());

                if(this.isStopped()){
                    break;
                }


            }
            error = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            process.destroy();
            pingStream.close();
            reader.close();

            result = process.waitFor();



            Thread.sleep(200); // Sleep so that the Observer can process the last lines
        } catch (IOException e) {
            Log.e(TAG, "Error while executing ping command: " + e.toString());
            return Result.failure(output.putString(REASON, "Error while executing ping command.").build());
        } catch (InterruptedException e) {
            Log.e(TAG, "Error while waiting for ping command: " + e.toString());
            return Result.failure(output.putString(REASON, "Error while waiting for ping command.").build());
        }

        if (result != 0) {;
            Log.e(TAG, "Ping command failed with result: " + result);
            Log.e(TAG, "Ping command failed because: "+error);
            return Result.failure(output.putString(REASON, "Ping command failed: " + error).build());
        }

        return Result.success(output.putBoolean(PING, true).build());
    }
}
