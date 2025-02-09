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
import android.content.Context;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class PingWorker extends Worker {

    public static final String PING = "ping";
    public static final String REASON = "reason";
    public static final String LINE = "line";
    public static final String TAG = "PingWorker";

    private int notificationID = 102;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private final Context ct;
    private NotificationManager notificationManager;
    private final String channelId = "OMNT_notification_channel";
    private NotificationCompat.Builder notificationBuilder;
    private float rtt; // round-trip time
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
        setForegroundAsync(createForegroundInfo(""));
    }

    private ForegroundInfo createForegroundInfo(String progress) {
        notification = notificationBuilder
                .setContentTitle("Ping")
                .setContentText(pingInput.getPingParameter().getDestination()+": "+progress)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
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
        try {
            String timeRegex = "time=(\\d+(?:\\.\\d+)?)\\s*ms";
            Pattern pattern = Pattern.compile(timeRegex);


            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(getApplicationContext().getFilesDir()+pingInput.getTestUUID()+".txt"));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                writer.write(line + "\n");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    try {
                        rtt = Float.parseFloat(Objects.requireNonNull(matcher.group(1)));
                        Log.d(TAG, "Updated RTT: " + rtt);
                        new Runnable() {
                            @Override
                            public void run() {
                                setForegroundAsync(createForegroundInfo( rtt + " ms"));
                            }
                        }.run();
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing RTT value: " + e.toString());
                    }
                }
                // Optionally, report progress with the output line.
                setProgressAsync(output.putString(LINE, line).build());
            }

            writer.close();
            reader.close();

            result = process.waitFor();
        } catch (IOException e) {
            Log.e(TAG, "Error while executing ping command: " + e.toString());
            return Result.failure(output.putString(REASON, "Error while executing ping command.").build());
        } catch (InterruptedException e) {
            Log.e(TAG, "Error while waiting for ping command: " + e.toString());
            return Result.failure(output.putString(REASON, "Error while waiting for ping command.").build());
        }

        if (result != 0) {
            Log.e(TAG, "Ping command failed with result: " + result);
            return Result.failure(output.putString(REASON, "Ping command failed with result: " + result).build());
        }

        return Result.success(output.putBoolean(PING, true).build());
    }
}
