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

import com.influxdb.client.write.Point;

import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;

public class LoggingService extends Service {
    private static final String TAG_LOGGING_SERVICE = "Logging_Service";
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



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG_LOGGING_SERVICE, "i");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOGGING_SERVICE, "Start logging service.");
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
                        .setContentText("No Cell Information available")
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setColor(Color.WHITE)
                        .setContentIntent(pendingIntent)
                        // prevent to swipe the notification away
                        .setOngoing(true)
                        // don't wait 10 seconds to show the notification
                        .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);

        // Start foreground service.
        startForeground(1, builder.build());

        // todo add setting to enable / disable notification update
        notificationHandler = new Handler(Looper.myLooper());
        notificationHandler.postDelayed(notification_updater, 1000);

        // todo add setting to enable / disable influx output
        String url = sp.getString("influx_URL", null);
        String org = sp.getString("influx_org", null);
        String bucket = sp.getString("influx_bucket", null);
        String token = sp.getString("influx_token", null);

        ic = new InfluxdbConnection(url, token, org, bucket);
        ic.connect();
        influxHandler = new Handler(Looper.myLooper());
        influxHandler.postDelayed(influxUpdate, 1000);

        return START_STICKY;
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
                    point.addField("Level", ss.getLevel());
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
                point.addField("longitude", loc.getLongitude());
                point.addField("latitude", loc.getLatitude());
                point.addField("altitude", loc.getAltitude());
                point.addField("speed", loc.getSpeed());
                ic.writePoint(point);

                influxHandler.postDelayed(this,1000);
            } else {
                Log.d(TAG_LOGGING_SERVICE, "influx not initialized");
            }
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
        influxHandler.removeCallbacks(influxUpdate);
        ic.disconnect();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
