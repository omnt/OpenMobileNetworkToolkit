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
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthLte;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.influxdb.client.write.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;

public class LoggingService extends Service {
    private static final String TAG = "Logging_Service";
    public TelephonyManager tm;
    public PackageManager pm;
    public boolean feature_telephony = false;
    public boolean cp = false;
    public NotificationManager nm;
    NotificationCompat.Builder builder;
    private Handler notificationHandler;
    private Handler influxHandler;
    InfluxdbConnection ic;
    DataProvider dc;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

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
        influxHandler = new Handler(Looper.myLooper());
        influxHandler.post(influxUpdate);
    }

    private void stopInfluxDB() {
        Log.d(TAG, "stopInfluxDB");
        influxHandler.removeCallbacks(influxUpdate);
        ic.disconnect();
    }

    private Runnable notification_updater = new Runnable() {
        @Override
        public void run() {
            List<CellInfo> cil = dc.getCellInfo();
            String OperatorName = "OMNT";
            String PCI = "1337";
            String CI = "2342";
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

    private Runnable influxUpdate = new Runnable() {
        @Override
        public void run() {
            if (ic != null){

                // write network information
                if (sp.getBoolean("influx_network_data", false)) {
                    Point point = new Point("NetworkInformation");
                    NetworkInformation ni = dc.GetNetworkInformation();
                    point.addField("NetworkOperatorName", ni.getNetworkOperatorName());
                    point.addField("NetworkSpecifier",ni.getNetworkSpecifier());
                    point.addField("SimOperatorName", ni.getSimOperatorName());
                    point.addField("DataState", ni.getDataState());
                    point.addField("PhoneType", ni.getPhoneType());
                    point.addField("PreferredOpportunisticDataSubscriptionId", ni.getPreferredOpportunisticDataSubscriptionId());
                    ic.writePoint(point);
                }
                // write signal strength information
                if (sp.getBoolean("influx_signal_data", false)) { // user settings here
                    Point point = new Point("SignalStrength");
                    SignalStrength ss = dc.getSignalStrength();

                    try {
                        point.addField("Level", ss.getLevel());
                        ic.writePoint(point);
                    } catch (NullPointerException ignored) {
                    }
                }

                // write throughput data
                if(sp.getBoolean("influx_throughput_data", false)) {
                    ConnectivityManager cm = dc.getCm();
                    NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                    int upSpeed = nc.getLinkUpstreamBandwidthKbps();
                    int signalStrength = nc.getSignalStrength();
                    Point point = new Point(sp.getString("measurement_name", "iperf3_test"));
                    point.addField("downSpeed_kbps", downSpeed);
                    point.addField("upSpeed_kbps", upSpeed);
                    point.addField("signalStrength", signalStrength);
                    ic.writePoint(point);
                }

                // write cell information
                if (sp.getBoolean("influx_cell_data", false)) {
                    Point point = new Point("CellInformation");
                    List<CellInfo> cil = dc.getCellInfo();
                    for (CellInfo ci:cil) {
                        point.addField("OperatorAlphaLong", (String) ci.getCellIdentity().getOperatorAlphaLong());
                        if (ci instanceof CellInfoNr) {
                            point.addField("CellType", "NR");
                        }
                        if (ci instanceof CellInfoLte) {
                            CellInfoLte ciLTE = (CellInfoLte) ci;
                            CellIdentityLte ciLTEId= ciLTE.getCellIdentity();
                            point.addField("CellType", "LTE");
                            point.addField("Bands", ciLTEId.getBands().toString());
                            point.addField("Bandwidth", ciLTEId.getBandwidth());
                            point.addField("CI",ciLTEId.getCi());
                            point.addField("EARFCN", ciLTEId.getEarfcn());
                            point.addField("MNC", ciLTEId.getMncString());
                            point.addField("PCI", ciLTEId.getPci());
                            point.addField("TAC", ciLTEId.getTac());
                            CellSignalStrengthLte ssLTE = ciLTE.getCellSignalStrength();
                            point.addField("CQI", ssLTE.getCqi());
                            point.addField("RSRP", ssLTE.getRsrp());
                            point.addField("RSRQ", ssLTE.getRsrq());
                            point.addField("RSSI", ssLTE.getRssi());
                            point.addField("RSSNR", ssLTE.getRssnr());
                        }
                        if (ci instanceof CellInfoCdma) {
                            point.addField("CellType", "CDMA");
                        }
                        if (ci instanceof CellInfoGsm) {
                            point.addField("CellType", "GSM");
                        }
                    }
                    ic.writePoint(point);
                }
                Point point = new Point("Location");
                Location loc = dc.getLocation();

                try {
                    point.addField("longitude", loc.getLongitude());
                    point.addField("latitude", loc.getLatitude());
                    point.addField("altitude", loc.getAltitude());
                    point.addField("speed", loc.getSpeed());
                    ic.writePoint(point);
                }
                catch (NullPointerException e) {
                    Log.d(TAG, "run: no location found");
                }
                
                influxHandler.postDelayed(this,1000);
            } else {
                Log.d(TAG, "influx not initialized");
            }
        }
    };

    private Runnable influxIperf3Update = new Runnable() {
        @Override
        public void run() {
            if (ic != null){
                String path = getApplicationContext().getFilesDir().toString();
                Log.d("influxIperf3Update", "Path: " + path);
                File directory = new File(path);
                FilenameFilter filter = (f, name) -> name.endsWith(".log");

                File[] files = directory.listFiles(filter);
/*                for (File from: files) {
                    Log.d(TAG, "influxIperf3Update: "+from.getName());
                    try {

                        Point point = new Point("Iperf3");

                        for (Iperf3JsonAsClass.Interval interval:data.intervals) {
                            point.addField("Timestamp", interval.streams.get(0).end);
                            point.addField("bits_per_second", interval.streams.get(0).bits_per_second);
                        }
                        ic.writePoint(point);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
*/


                influxHandler.postDelayed(this,1000);
            } else {
                Log.d(TAG, "influx not initialized");
            }
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

        if (sp.getBoolean("enable_iperf3_update", false)) {
            stopInfluxDB();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
