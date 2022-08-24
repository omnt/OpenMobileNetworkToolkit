/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.os.HandlerCompat;

public class LoggingService extends Service {
    private static final String TAG_LOGGING_SERVICE = "Logging_Service";
    public TelephonyManager tm;
    public PackageManager pm;
    public boolean feature_telephony = false;
    public boolean cp = false;
    public NotificationManager nm;
    NotificationCompat.Builder builder;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG_LOGGING_SERVICE, "i");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOGGING_SERVICE, "Start logging service.");

        pm = getPackageManager();
        nm = getSystemService(NotificationManager.class);
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (feature_telephony) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            cp = tm.hasCarrierPrivileges();
        }

        // create intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        // create notification
        builder =
                new NotificationCompat.Builder(this, "OMNT_notification_channel")
                        .setContentTitle(getText(R.string.loggin_notifaction))
                        .setContentText("5G, >9000dbm, 1337 Gbit")
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setColor(Color.WHITE)
                        .setContentIntent(pendingIntent)
                        .setTicker("tick tick tick")
                        // prevent to swipe the notification away
                        .setOngoing(true)
                        // don't wait 10 seconds to show the notification
                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);

        // Start foreground service.
        startForeground(1, builder.build());

        handler = new Handler(Looper.myLooper());
        handler.postDelayed(notification_updater, 1000);

        return START_STICKY;
    }

    private Runnable notification_updater = new Runnable() {
        @Override
        public void run() {
            builder.setContentText(tm.getSignalStrength().toString());
            nm.notify(1, builder.build());
            handler.postDelayed(this,1000);
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG_LOGGING_SERVICE, "Stop logging service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
