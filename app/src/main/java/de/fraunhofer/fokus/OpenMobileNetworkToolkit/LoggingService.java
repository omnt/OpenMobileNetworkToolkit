/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
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
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingWorker;

public class LoggingService extends Service {
    private static final String TAG = "Logging_Service";
    public TelephonyManager tm;
    public PackageManager pm;
    public boolean feature_telephony = false;
    public boolean cp = false;
    public NotificationManager nm;
    NotificationCompat.Builder builder;
    InfluxdbConnection ic; // remote influxDB
    InfluxdbConnection lic; // local influxDB
    DataProvider dp;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    //private MainActivity ma;
    private Handler notificationHandler;
    private Handler remoteInfluxHandler;
    private Handler localInfluxHandler;
    private Handler localFileHandler;
    private List<Point> logFilePoints;
    private FileOutputStream stream;
    private FileOutputStream ping_stream;
    private int interval;
    private Context context;
    private Handler pingLogging;
    private String pingInput;
    private WorkManager wm;
    private ArrayList<OneTimeWorkRequest> pingWRs;
    // Handle local on-device logging to logfile
    private final Runnable localFileUpdate = new Runnable() {
        @Override
        public void run() {
            logFilePoints.addAll(getPoints());
            if (logFilePoints.size() >= 100) {
                for (Point point : logFilePoints) {
                    try {
                        stream.write((point.toLineProtocol() + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logFilePoints.clear();
                try {
                    stream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            localFileHandler.postDelayed(this, interval);
        }
    };
    // Handle notification bar update
    private final Runnable notification_updater = new Runnable() {
        @Override
        public void run() {
            List<CellInformation> cil = dp.getRegisteredCells();
            StringBuilder s = new StringBuilder();
            for (CellInformation ci : cil) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !ci.getAlphaLong().isEmpty()) {
                    s.append(ci.getAlphaLong());
                } else {
                    s.append(" PLMN: ");
                    s.append(ci.getMcc());
                    s.append(ci.getMnc());
                }
                s.append(" Type: ");
                s.append(ci.getCellType());
                switch (ci.getCellType()) {
                    case "LTE":
                        s.append(" PCI: ");
                        s.append(ci.getPci());
                        s.append(" RSRQ: ");
                        s.append(ci.getRsrp());
                        s.append(" RSRP: ");
                        s.append(ci.getRsrq());
                        break;
                    case "NR":
                        s.append(" PCI: ");
                        s.append(ci.getPci());
                        s.append(" SSRSRQ: ");
                        s.append(ci.getSsrsrq());
                        s.append(" SSRSRP: ");
                        s.append(ci.getSsrsrp());
                        s.append(" SSSINR: ");
                        s.append(ci.getSssinr());
                        break;
                    case "GSM":
                        s.append(" CI: ");
                        s.append(ci.getCi());
                        s.append(" Level: ");
                        s.append(ci.getLevel());
                        s.append(" DBM: ");
                        s.append(ci.getDbm());
                }
            }
            builder.setContentText(s);
            nm.notify(1, builder.build());
            notificationHandler.postDelayed(this, interval);
        }
    };

    // Handle local on-device influxDB
    private final Runnable localInfluxUpdate = new Runnable() {
        @Override
        public void run() {
            long ts = System.currentTimeMillis();
            // write network information
            if (sp.getBoolean("influx_network_data", false)) {

            }
            // write signal strength information
            if (sp.getBoolean("influx_signal_data", false)) { // user settings here

            }
            // write cell information
            if (sp.getBoolean("influx_cell_data", false)) {

            }
            // always add location information
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ic.writePoints(new ArrayList<>(Collections.singleton(dp.getLocationPoint())));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            remoteInfluxHandler.postDelayed(this, interval);
        }
    };

    // Handle remote on-server influxdb update
    private final Runnable RemoteInfluxUpdate = new Runnable() {
        @Override
        public void run() {
            try {
                ic.writePoints(getPoints());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ic.flush();
            remoteInfluxHandler.postDelayed(this, interval);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Logging service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start logging service");
        GlobalVars gv = GlobalVars.getInstance();
        // setup class variables
        dp = gv.get_dp();
        pm = getPackageManager();
        nm = getSystemService(NotificationManager.class);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        context = this;
        interval = Integer.parseInt(sp.getString("logging_interval", "1000"));
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (feature_telephony) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            cp = tm.hasCarrierPrivileges();
        }
        wm = WorkManager.getInstance(context);
        if(intent != null) {
            if (intent.getBooleanExtra("ping", false)) {
                if (intent.getBooleanExtra("ping_stop", false)) {
                    stopPing();
                } else {
                    pingInput = intent.getStringExtra("input");
                    pingWRs = new ArrayList<>();
                    setupPing();
                }
                return START_STICKY;
            }
        }
        // create intent for notifications
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // create notification
            builder = new NotificationCompat.Builder(this, "OMNT_notification_channel").setContentTitle(getText(R.string.loggin_notifaction)).setSmallIcon(R.mipmap.ic_launcher_foreground).setColor(Color.WHITE).setContentIntent(pendingIntent)
                    // prevent to swipe the notification away
                    .setOngoing(true)
                    // don't wait 10 seconds to show the notification
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        } else {
            // create notification
            builder = new NotificationCompat.Builder(this, "OMNT_notification_channel").setContentTitle(getText(R.string.loggin_notifaction)).setSmallIcon(R.mipmap.ic_launcher_foreground).setColor(Color.WHITE).setContentIntent(pendingIntent)
                    // prevent to swipe the notification away
                    .setOngoing(true);
        }

        // create preferences listener
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Objects.equals(key, "enable_influx")) {
                    if (prefs.getBoolean(key, false)) {
                        if (prefs.getString("influx_URL", "").isEmpty() || prefs.getString("influx_org", "").isEmpty() || prefs.getString("influx_token", "").isEmpty() || prefs.getString("influx_bucket", "").isEmpty()) {
                            Log.i(TAG, "Not all influx settings are present in preferences");
                            Toast.makeText(getApplicationContext(), "Please fill all Influx Settings", Toast.LENGTH_LONG).show();
                            prefs.edit().putBoolean("enable_influx", false).apply();
                        } else {
                            setupRemoteInfluxDB();
                        }
                    } else {
                        stopRemoteInfluxDB();
                    }
                } else if (Objects.equals(key, "enable_notification_update")) {
                    if (prefs.getBoolean(key, false)) {
                        setupNotificationUpdate();
                    } else {
                        stopNotificationUpdate();
                    }
                } else if (Objects.equals(key, "enable_local_file_log")) {
                    if (prefs.getBoolean(key, false)) {
                        setupLocalFile();
                    } else {
                        stopLocalFile();
                    }
                } else if (Objects.equals(key, "enable_local_influx_log")) {
                    if (prefs.getBoolean(key, false)) {
                        setupLocalInfluxDB();
                    } else {
                        stopLocalInfluxDB();
                    }
                } else if (Objects.equals(key, "logging_interval")) {
                    interval = Integer.parseInt(sp.getString("logging_interval", "1000"));
                }
            }
        };
        sp.registerOnSharedPreferenceChangeListener(listener);

        // Start foreground service and setup logging targets
        startForeground(1, builder.build());

        if (sp.getBoolean("enable_notification_update", false)) {
            setupNotificationUpdate();
        }

        if (sp.getBoolean("enable_influx", false)) {
            setupRemoteInfluxDB();
        }

        if (sp.getBoolean("enable_local_file_log", false)) {
            setupLocalFile();
        }

        if (sp.getBoolean("enable_local_influx_log", false)) {
            setupLocalFile();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Stop logging service");
        if (sp.getBoolean("enable_influx", false)) {
            stopRemoteInfluxDB();
        }
        if (sp.getBoolean("enable_local_file_log", false)) {
            stopLocalFile();
        }
        if (sp.getBoolean("enable_local_influx_log", false)) {
            stopLocalInfluxDB();
        }

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    private ArrayList<Point> getPoints() {
        long time = System.currentTimeMillis();
        Map<String, String> tags_map = dp.getTagsMap();
        ArrayList<Point> logPoints = new ArrayList<Point>();
        if (sp.getBoolean("influx_network_data", false)) {
            Point p = dp.getNetworkInformationPoint();
            if (p.hasFields()) {
                p.time(time, WritePrecision.MS);
                p.addTags(tags_map);
                logPoints.add(p);
            } else {
                Log.w(TAG, "Point without fields from getNetworkInformationPoint");
            }
        }

        if (sp.getBoolean("influx_throughput_data", false)) {
            Point p = dp.getNetworkCapabilitiesPoint();
            if (p.hasFields()) {
                p.time(time, WritePrecision.MS);
                p.addTags(tags_map);
                logPoints.add(p);
            } else {
                Log.w(TAG, "Point without fields from getNetworkCapabilitiesPoint");
            }
        }

        if (sp.getBoolean("log_signal_data", false)) {
            Point p = dp.getSignalStrengthPoint();
            if (p.hasFields()) {
                p.time(time, WritePrecision.MS);
                p.addTags(tags_map);
                logPoints.add(p);
            } else {
                Log.w(TAG, "Point without fields from getSignalStrengthPoint");
            }
        }

        if (sp.getBoolean("influx_cell_data", false)) {
            List<Point> ps = dp.getCellInformationPoint();
            for (Point p : ps) {
                if (p.hasFields()) {
                    p.time(time, WritePrecision.MS);
                    p.addTags(tags_map);
                } else {
                    Log.w(TAG, "Point without fields getCellInfoPoint");
                }
            }
            logPoints.addAll(ps);
        }
        if (sp.getBoolean("influx_ip_address_data", false)) {
            List<Point> ps = dp.getNetworkInterfaceInformationPoints();
            for (Point p : ps) {
                if (p.hasFields()) {
                    p.time(time, WritePrecision.MS);
                    p.addTags(tags_map);
                } else {
                    Log.w(TAG, "Point without fields getCellInfoPoint");
                }
            }
            logPoints.addAll(ps);
        }

        if (sp.getBoolean("influx_battery_data", false)) {
            Point bp = dp.getBatteryInformationPoint();
            bp.time(time, WritePrecision.MS);
            logPoints.add(bp);
        }

        Point p = dp.getLocationPoint();
        p.time(time, WritePrecision.MS);
        p.addTags(tags_map);
        logPoints.add(p);
        return logPoints;
    }

    private void setupLocalFile() {
        Log.d(TAG, "setupLocalFile");
        logFilePoints = new ArrayList<Point>();

        // build log file path
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/log/";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create the log file
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String filename = path + formatter.format(now) + ".txt";
        Log.d(TAG, "logfile: " + filename);
        File logfile = new File(filename);
        try {
            logfile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // get an output stream
        try {
            stream = new FileOutputStream(logfile);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "logfile not created", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        localFileHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        localFileHandler.post(localFileUpdate);
    }

    private void stopLocalFile() {
        Log.d(TAG, "stopLocalFile");
        if (localFileHandler != null) {
            try {
                stream.close();
                localFileHandler.removeCallbacks(localFileUpdate);
            } catch (java.lang.NullPointerException e) {
                Log.d(TAG, "trying to stop local file service while it was not running");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupNotificationUpdate() {
        Log.d(TAG, "setupNotificationUpdate");
        notificationHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        notificationHandler.post(notification_updater);
    }

    private void stopNotificationUpdate() {
        Log.d(TAG, "stopNotificationUpdate");
        notificationHandler.removeCallbacks(notification_updater);
        builder.setContentText(null);
        nm.notify(1, builder.build());
    }

    private void setupLocalInfluxDB() {
        Log.d(TAG, "setupLocalInfluxDB");
        lic = InfluxdbConnections.getLicInstance(getApplicationContext());
        Objects.requireNonNull(lic).open_write_api();
        localInfluxHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        localInfluxHandler.post(localInfluxUpdate);
    }

    private void stopLocalInfluxDB() {
        Log.d(TAG, "stopLocalInfluxDB");
        if (localInfluxHandler != null) {
            try {
                localInfluxHandler.removeCallbacks(RemoteInfluxUpdate);
            } catch (java.lang.NullPointerException e) {
                Log.d(TAG, "trying to stop local influx service while it was not running");
            }
        }
        if (lic != null) {
            lic.disconnect();
        }
    }

    private void setupRemoteInfluxDB() {
        Log.d(TAG, "setupRemoteInfluxDB");
        ic = InfluxdbConnections.getRicInstance(getApplicationContext());
        if(ic == null){
            stopRemoteInfluxDB();
            return;
        }
        Objects.requireNonNull(ic).open_write_api();
        remoteInfluxHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        remoteInfluxHandler.post(RemoteInfluxUpdate);
    }

    private void stopRemoteInfluxDB() {
        Log.d(TAG, "stopRemoteInfluxDB");
        if (remoteInfluxHandler != null) {
            try {
                remoteInfluxHandler.removeCallbacks(RemoteInfluxUpdate);
            } catch (java.lang.NullPointerException e) {
                Log.d(TAG, "trying to stop remote influx service while it was not running");
            }
        }
        if (ic != null) {
            ic.disconnect();
        }
    }
    private long unixTimestampWithMicrosToMillis(double timestampWithMicros) {
        long seconds = (long) timestampWithMicros;
        long microseconds = (long) ((timestampWithMicros - seconds) * 1e6);
        return seconds * 1000 + microseconds / 1000;
    }

    private void setupPing(){
        Log.d(TAG, "setupLocalFile");

        // build log file path
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/ping/";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create the log file
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String filename = path + formatter.format(now) + ".txt";
        Log.d(TAG, "logfile: " + filename);
        File logfile = new File(filename);
        try {
            logfile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // get an output stream
        try {
            ping_stream = new FileOutputStream(logfile);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "logfile not created", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        pingLogging = new Handler(Objects.requireNonNull(Looper.myLooper()));
        pingLogging.post(pingUpdate);
    }    private final Runnable pingUpdate = new Runnable() {
        @Override
        public void run() {

            Data data = new Data.Builder()
                .putString("input", pingInput)
                .build();
            OneTimeWorkRequest pingWR =
                new OneTimeWorkRequest.Builder(PingWorker.class)
                    .setInputData(data)
                    .addTag("Ping").build();
            pingWRs.add(pingWR);

            wm.beginWith(pingWR).enqueue();
            Observer observer = new Observer() {
                @RequiresApi(api = Build.VERSION_CODES.S)
                @Override
                public void onChanged(Object o) {
                    WorkInfo workInfo = (WorkInfo) o;
                    WorkInfo.State state = workInfo.getState();
                    Log.d(TAG, "onChanged: "+state.toString());
                    if(state == WorkInfo.State.RUNNING) {
                        String line = workInfo.getProgress().getString("ping_line");
                        if(line == null) return;
                        Pattern pattern = Pattern.compile("\\[(\\d+\\.\\d+)\\] (\\d+ bytes from (\\S+|\\d+\\.\\d+\\.\\d+\\.\\d+)): icmp_seq=(\\d+) ttl=(\\d+) time=([\\d.]+) ms");
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()){
                            long unixTimestamp = unixTimestampWithMicrosToMillis(Double.parseDouble(matcher.group(1)));
                            int icmpSeq = Integer.parseInt(matcher.group(4));
                            int ttl = Integer.parseInt(matcher.group(5));
                            String host = matcher.group(3);
                            double rtt = Double.parseDouble(matcher.group(6));
                            Intent intent = new Intent("ping_rtt");
                            intent.putExtra("ping_running", true);
                            intent.putExtra("ping_rtt", rtt);
                            sendBroadcast(intent);

                            // Create an InfluxDB point with the Unix timestamp
                            Point point = Point.measurement("Ping")
                                .time(unixTimestamp, WritePrecision.MS)
                                .addTags(dp.getTagsMap())
                                .addTag("toHost", host)
                                .addField("icmp_seq", icmpSeq)
                                .addField("ttl", ttl)
                                .addField("rtt", rtt);
                            try {
                                ping_stream.write((point.toLineProtocol() + "\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            if (sp.getBoolean("enable_influx", false)) {
                                try {
                                    ic.writePoints(Arrays.asList(point));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Log.d(TAG, "doWork: Point:"+point.toLineProtocol());

                        }
                        return;
                    }
                    if(state == WorkInfo.State.ENQUEUED) return;
                    if(state == WorkInfo.State.CANCELLED) {
                        try {
                            ping_stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    };
                    if(state == WorkInfo.State.FAILED) {
                        pingLogging.postDelayed(pingUpdate, 1000);
                        return;
                    }

                    wm.getWorkInfoByIdLiveData(pingWR.getId()).removeObserver(this);
                    pingLogging.postDelayed(pingUpdate, 1000);
                }
            };
            wm.getWorkInfoByIdLiveData(pingWR.getId()).observeForever(observer);
        }
    };

    private void stopPing(){
        if(pingLogging != null)  pingLogging.removeCallbacks(pingUpdate);
        try {
            if (ping_stream != null) ping_stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        wm.cancelAllWorkByTag("Ping");
        Intent intent = new Intent("ping_rtt");
        intent.putExtra("ping_rtt", 0);
        intent.putExtra("ping_running", false);
        sendBroadcast(intent);
        pingWRs = new ArrayList<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}