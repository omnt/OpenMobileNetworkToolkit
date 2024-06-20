package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

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
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnections;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingService extends Service {
    private static final String TAG = "PingService";
    private FileOutputStream ping_stream;
    private Handler pingLogging;
    private WorkManager wm;
    private Context context;
    private ArrayList<OneTimeWorkRequest> pingWRs;
    private SharedPreferences pingSP;
    private SharedPreferences defaultSP;
    NotificationCompat.Builder builder;
    DataProvider dp;
    InfluxdbConnection influx;
    private static boolean isRunning = false;
    private String pingCommand;
    private PropertyChangeListener propertyChangeListener;
    HashMap<String, String> parsedCommand = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Stop logging service");
        stopPing();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start logging service");
        GlobalVars gv = GlobalVars.getInstance();
        // setup class variables
        dp = gv.get_dp();
        context = getApplicationContext();
        pingSP = context.getSharedPreferences("Ping", Context.MODE_PRIVATE);
        defaultSP = PreferenceManager.getDefaultSharedPreferences(context);
        if(defaultSP.getBoolean("enable_influx", false)) influx = InfluxdbConnections.getRicInstance(context);
        wm = WorkManager.getInstance(context);
        wm.cancelAllWorkByTag("Ping");
        if(intent == null) return START_NOT_STICKY;
        pingWRs = new ArrayList<>();

        setupPing();
        isRunning = true;
        return START_STICKY;
    }


    private void setupPing(){
        Log.d(TAG, "starting Ping Service...");

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/ping/";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            Toast.makeText(context, "could not create /omnt/ping Dir!", Toast.LENGTH_SHORT).show();
            pingSP.edit().putBoolean("ping", false).apply();
            Log.d(TAG, "setupPing: could not create /omnt/ping Dir!");
            return;
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
            Toast.makeText(context, "could not create logfile "+filename, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "setupPing: could not create logfile");
            pingSP.edit().putBoolean("ping", false).apply();
            return;
        }

        // get an output stream
        try {
            ping_stream = new FileOutputStream(logfile);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "could not create output stream", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "setupPing: could not create output stream");
            pingSP.edit().putBoolean("ping", false).apply();
            return;
        }
        pingSP.edit().putBoolean("ping", true).apply();
        pingLogging = new Handler(Objects.requireNonNull(Looper.myLooper()));
        PingParser pingParser = PingParser.getInstance(null);
        propertyChangeListener = pingParser.getListener();
        if(propertyChangeListener != null){
            pingParser.removePropertyChangeListener(propertyChangeListener);
        }
        propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PingInformation pi = (PingInformation) evt.getNewValue();

                Point point = pi.getPoint();
                point.addTags(dp.getTagsMap());
                Log.d(TAG, "propertyChange: "+point.toLineProtocol());
                try {
                    ping_stream.write((point.toLineProtocol() + "\n").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (defaultSP.getBoolean("enable_influx", false) && influx.getWriteApi() != null) {
                    try {
                        influx.writePoints(Arrays.asList(point));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        pingParser.addPropertyChangeListener(propertyChangeListener);
        pingParser.setListener(propertyChangeListener);
        pingLogging.post(pingUpdate);
    }
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

    private final Runnable pingUpdate = new Runnable() {
        @Override
        public void run() {
            Data data = new Data.Builder()
                .putString("input", pingSP.getString("ping_input", "8.8.8.8"))
                .build();
            OneTimeWorkRequest pingWR =
                new OneTimeWorkRequest.Builder(PingWorker.class)
                    .setInputData(data)
                    .addTag("Ping").build();
            pingWRs.add(pingWR);

            wm.beginWith(pingWR).enqueue();
            Observer observer = new Observer() {
                @Override
                public void onChanged(Object o) {
                    WorkInfo workInfo = (WorkInfo) o;
                    WorkInfo.State state = workInfo.getState();
                    switch (state){
                        case RUNNING:
                        case ENQUEUED:
                            return;
                        case CANCELLED:
                            try {
                                ping_stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pingLogging.removeCallbacks(pingUpdate);
                            return;
                    }

                    wm.getWorkInfoByIdLiveData(pingWR.getId()).removeObserver(this);
                    pingWRs.remove(pingWR);
                    pingLogging.postDelayed(pingUpdate, 200);
                }
            };
            wm.getWorkInfoByIdLiveData(pingWR.getId()).observeForever(observer);
        }
    };

    private void stopPing(){

        if (pingLogging != null )pingLogging.removeCallbacks(pingUpdate);
        try {
            if (ping_stream != null) ping_stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (OneTimeWorkRequest wr : pingWRs){
            wm.cancelWorkById(wr.getId());

        }

        pingSP.edit().putBoolean("ping", false).apply();
        pingWRs = new ArrayList<>();
    }
    public static boolean isRunning() {
        return isRunning;
    }


}

