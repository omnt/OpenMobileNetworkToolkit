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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingWorker extends Worker {

    private static final String TAG = "PingWorker";
    Runtime runtime;
    private ArrayList<String> lines;
    private Process pingProcess;
    private final int notificationID = 102;
    HashMap<String, String> parsedCommand = new HashMap<>();
    private String pingCommand;
    private final int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    private final Context ct;
    private final String channelId = "OMNT_notification_channel";
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private final String timeRegex = "\\btime=([0-9]+\\.[0-9]+)\\s+ms\\b";
    private double rtt;
    private NotificationManager notificationManager;
    private SharedPreferencesGrouper spg;


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
        spg = SharedPreferencesGrouper.getInstance(ct);
    }


    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped: worker stopped!");
        if(pingProcess.isAlive()) pingProcess.destroy();
        spg.getSharedPreference(SPType.ping_sp).edit().putBoolean("ping_running", false).apply();

    }

    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Create an Intent to launch the main activity
        Intent launchIntent = new Intent(ct, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Add a Bundle to the Intent to indicate that the PingFragment should be opened
        Bundle bundle = new Bundle();
        bundle.putString("openFragment", "PingFragment");
        launchIntent.putExtras(bundle);

        // Create a PendingIntent with the Intent
        PendingIntent pendingIntent = PendingIntent.getActivity(ct, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent cancelIntent = WorkManager.getInstance(ct)
                .createCancelPendingIntent(getId());

        notification = notificationBuilder
                .setContentTitle("Ping " + parsedCommand.get("target"))
                .setContentText(progress)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.WHITE)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFAULT)
                .addAction(R.drawable.ic_close, "Cancel", cancelIntent)
                .setContentIntent(pendingIntent) // Set the content intent
                .build();
        return new ForegroundInfo(notificationID, notification, FOREGROUND_SERVICE_TYPE);
    }

    Runnable updateNotification = new Runnable() {
        @Override
        public void run() {
            if(notification != null){
                notificationBuilder.setContentText(rtt+" ms");
                notificationManager.notify(notificationID, notificationBuilder.build());
            } else {
                setForegroundAsync(createForegroundInfo(rtt+" ms"));
            }
        }
    };

    @NonNull
    @Override
    public Result doWork() {
        lines = new ArrayList<>();
        Data data = null;
        notificationBuilder = new NotificationCompat.Builder(ct, channelId);
        notificationManager = ct.getSystemService(NotificationManager.class);
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



            PingParser pingParser = PingParser.getInstance(outputReader);

            pingParser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    PingInformation pi = (PingInformation) evt.getNewValue();
                    switch(pi.getLineType()){
                        case PACKET_LOSS:
                            break;
                        case RTT:
                            rtt = ((RTTLine)pi).getRtt();
                            break;
                    }
                    updateNotification.run();
                }
            });
            pingParser.parse();
            int result = pingProcess.waitFor();

            if(isStopped()){
                Log.d(TAG, "doWork: got cancelled because Worker got stopped!");

                return Result.success();
            }


            Log.d(TAG, "doWork: result " + result);
            if (result != 0) {
                return Result.failure();
            }
        } catch (IOException e) {
            Log.d(TAG,e.toString());
;
            System.out.printf(e.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Result.success();
    }
}
