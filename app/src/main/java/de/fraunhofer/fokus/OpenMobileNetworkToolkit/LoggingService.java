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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.influxdb.client.write.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoggingService extends Service {
    private static final String TAG = "Logging_Service";
    public TelephonyManager tm;
    public PackageManager pm;
    public boolean feature_telephony = false;
    public boolean cp = false;
    public NotificationManager nm;
    NotificationCompat.Builder builder;
    private Handler notificationHandler;
    private Handler loggingHandler;
    InfluxdbConnection ic;
    DataProvider dc;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    LocalLogDatabase db;
    List<InfluxPointEntry> points;
    PointDao pointDao;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Logging service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start logging service.");
        dc = new DataProvider(this);
        pm = getPackageManager();
        nm = getSystemService(NotificationManager.class);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
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
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setColor(Color.WHITE)
                        .setContentIntent(pendingIntent)
                        // prevent to swipe the notification away
                        .setOngoing(true)
                        // don't wait 10 seconds to show the notification
                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);

        // create preferences listener
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Objects.equals(key, "enable_influx")) {
                    if (prefs.getBoolean(key,false)) {
                        setupInfluxDB();
                    } else {
                        stopInfluxDB();
                    }
                } else if (Objects.equals(key, "enable_notification_update")) {
                    if (prefs.getBoolean(key, false)) {
                        setupNotificationUpdate();
                    } else {
                        stopNotificationUpdate();
                    }
                }
            }
        };
        sp.registerOnSharedPreferenceChangeListener(listener);



        // Start foreground service.
        startForeground(1, builder.build());

        if (sp.getBoolean("enable_notification_update", false)) {
            setupNotificationUpdate();
        }

        if (sp.getBoolean("enable_influx", false)) {
            setupInfluxDB();
        }


        return START_STICKY;
    }

    private void setupNotificationUpdate() {
        Log.d(TAG, "setupNotificationUpdate");
        notificationHandler = new Handler(Looper.myLooper());
        notificationHandler.post(notification_updater);
    }

    private void stopNotificationUpdate() {
        Log.d(TAG, "stopNotificationUpdate");
        notificationHandler.removeCallbacks(notification_updater);
        builder.setContentText(null);
        nm.notify(1, builder.build());
    }

    private void setupInfluxDB() {
        Log.d(TAG, "setupInfluxDB");
        String url = sp.getString("influx_URL", null);
        String org = sp.getString("influx_org", null);
        String bucket = sp.getString("influx_bucket", null);
        String token = sp.getString("influx_token", null);

        ic = new InfluxdbConnection(url, token, org, bucket);
        ic.connect();
        loggingHandler = new Handler(Looper.myLooper());
        loggingHandler.post(loggingUpdate);
    }

    private void stopInfluxDB() {
        Log.d(TAG, "stopInfluxDB");

        loggingHandler.removeCallbacks(loggingUpdate);
        ic.disconnect();
    }

    private void setupLocalLog() {
        db = Room.databaseBuilder(getApplicationContext(),
                LocalLogDatabase.class, "OMNT-Log").build();
        //pointDao = db.PointDao();
    }

    private void stopLocalLog(){

    }

    private Runnable localLogUpdate = new Runnable() {
        @Override
        public void run() {
            long ts = System.currentTimeMillis();
            // write network information
            if (sp.getBoolean("influx_network_data", false)) {
                //pointDao.insertAll(new InfluxPointEntry(ts, dc.getNetworkInformationPoint()));
            }
            // write signal strength information
            if (sp.getBoolean("influx_signal_data", false)) { // user settings here
                ic.writePoint(dc.getSignalStrengthPoint());
            }
            // write cell information
            if (sp.getBoolean("influx_cell_data", false)) {
                ic.writePoint(dc.getCellInfoPoint());
            }
            // always add location information
            ic.writePoint(dc.getLocationPoint());

            loggingHandler.postDelayed(this,1000);

        }
    };

    private Runnable notification_updater = new Runnable() {
        @Override
        public void run() {
            List<CellInfo> cil = dc.getCellInfo();
            String OperatorName = "Not registered";
            String PCI = "";
            String CI = "";
            for (CellInfo ci:cil) {
                if (ci.isRegistered()) { //we only care for the serving cell
                    OperatorName = (String) ci.getCellIdentity().getOperatorAlphaLong();
                    CellInfoLte ciLTE = (CellInfoLte) ci;
                    PCI = String.valueOf(ciLTE.getCellIdentity().getPci());
                    CI = String.valueOf(ciLTE.getCellIdentity().getCi());

                }
            }

            builder.setContentText(new StringBuilder().append(OperatorName).append(" PCI: ").append(PCI).append(" CI: ").append(CI));
            nm.notify(1, builder.build());
            // disabled for now as it wakes the screen
            notificationHandler.postDelayed(this,100000);
        }
    };

    private Runnable loggingUpdate = new Runnable() {
        @Override
        public void run() {
            List<Point> points = new ArrayList<Point>();
            if (sp.getBoolean("influx_network_data", false)) {
                points.add(dc.getNetworkInformationPoint());
            }

            if(sp.getBoolean("influx_throughput_data", false)) {
                points.add(dc.getNetworkCapabilitiesPoint(sp.getString("measurement_name", "iperf3_test")));
            }

            if (sp.getBoolean("influx_signal_data", false)) {
                points.add(dc.getSignalStrengthPoint());
            }
            if (sp.getBoolean("influx_cell_data", false)) {
                points.add(dc.getCellInfoPoint());
            }
            points.add(dc.getLocationPoint());

            if (sp.getBoolean("enable_influx", false)) {
                for (Point point:points) {
                    ic.writePoint(point);
                }
            }


            ic.sendAll();


            if (sp.getBoolean("enable_local_log", false)) {
                long ts = System.currentTimeMillis();
                for (Point point:points) {

                    // as we can't simply get the timestamp we use a new one. This should be handled nicer
                    //pointDao.insertAll(new InfluxPointEntry(ts, point));
                }
            }



            loggingHandler.postDelayed(this,1000);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stop logging service.");


        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
        if (sp.getBoolean("enable_influx", false)) {
            stopInfluxDB();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}