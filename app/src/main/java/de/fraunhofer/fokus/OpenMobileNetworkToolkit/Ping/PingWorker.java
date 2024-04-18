/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingWorker extends Worker {

    private static final String TAG = "PingWorker";
    String host;
    Runtime runtime;
    private ArrayList<String> lines;
    private Process pingProcess;
    private final int notificationID = 102;
    HashMap<String, String> parsedCommand = new HashMap<>();
    private String pingCommand;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private Context ct;
    private String channelId = "OMNT_notification_channel";
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private String timeRegex = "\\btime=([0-9]+\\.[0-9]+)\\s+ms\\b";
    private Pattern pattern = Pattern.compile(timeRegex);
    private String line = null;

    public void parsePingCommand() {

        String[] commandParts = pingCommand.split("\\s+");

        String previousPart = null;
        for (String part : commandParts) {
            switch (part) {
                case "ping":
                    parsedCommand.put("command", part);
                    break;
                case "-w":
                    if (previousPart != null) {
                        parsedCommand.put("timeout", previousPart);
                    }
                    break;
                default:
                    parsedCommand.put("target", part);
                    break;
            }
            previousPart = part;
        }
    }

    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        runtime = Runtime.getRuntime();
        ct = context;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: worker stopped!");
        if(pingProcess.isAlive()) pingProcess.destroy();

    }

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {

        PendingIntent intent = WorkManager.getInstance(ct)
            .createCancelPendingIntent(getId());



        notification = notificationBuilder
            .setContentTitle("Ping "+ parsedCommand.get("target"))
            .setContentText(progress)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(Color.WHITE)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
            .addAction(R.drawable.ic_close, "Cancel", intent)
            .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }


    Runnable updateNotification = new Runnable() {
        @Override
        public void run() {
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()){
                double rtt = Double.parseDouble(matcher.group(1));
                setForegroundAsync(createForegroundInfo(rtt+" ms"));
            }
        }
    };
    Runnable sendBroadcast = new Runnable() {
        @Override
        public void run() {
            setProgressAsync(new Data.Builder().putString("ping_line", line).build());
        }
    };

    @NonNull
    @Override
    public Result doWork() {
        lines = new ArrayList<>();
        Data data = null;
        notificationBuilder = new NotificationCompat.Builder(ct, channelId);
        try {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/system/bin/ping ")
                .append("-D ")
                .append(getInputData().getString("input"));

            pingCommand = stringBuilder.toString();
            parsePingCommand();
            pingProcess = runtime.exec(stringBuilder.toString());

            BufferedReader outputReader =
                new BufferedReader(new InputStreamReader(pingProcess.getInputStream()));


            while((line = outputReader.readLine()) != null){
                lines.add(line);
                updateNotification.run();
                sendBroadcast.run();
            }

            data = new Data.Builder().putStringArray("output", lines.toArray(new String[0]))
                .build();

            if(isStopped()){
                Log.d(TAG, "doWork: got cancelled because Worker got stopped!");
                return Result.success(data);
            }


            int result = pingProcess.waitFor();
            Log.d(TAG, "doWork: result " + result);
            if (result != 0) {
                return Result.failure();
            }
            data = new Data.Builder().putStringArray("output", lines.toArray(new String[0]))
                .build();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf(e.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Result.success(data);
    }
}
