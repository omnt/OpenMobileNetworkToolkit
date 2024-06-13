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
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    private String pingInput;
    private WorkManager wm;
    private Context context;
    private ArrayList<OneTimeWorkRequest> pingWRs;
    private SharedPreferences pingSP;
    private SharedPreferences defaultSP;
    NotificationCompat.Builder builder;
    DataProvider dp;
    InfluxdbConnection influx;
    private static boolean isRunning = false;
    private static PingParser pingParser;
    private Process pingProcess;
    private String pingCommand;
    private Runtime runtime = Runtime.getRuntime();

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
        if(intent == null) return START_NOT_STICKY;
        pingInput = intent.getStringExtra("input");
        pingWRs = new ArrayList<>();
        setupPing();
        isRunning = true;
        return START_STICKY;
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
        pingSP.edit().putBoolean("ping", true).apply();
        pingLogging = new Handler(Objects.requireNonNull(Looper.myLooper()));
        PingParser pingParser = PingParser.getInstance(null);
        pingParser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PingInformation pi = (PingInformation) evt.getNewValue();
                Log.d(TAG, "propertyChange: "+pi.toString());
                Point point = Point.measurement("Ping")
                    .time(pi.getUnixTimestamp(), WritePrecision.MS)
                    .addTags(dp.getTagsMap())
                    .addTag("toHost", pi.getHost())
                    .addField("icmp_seq", pi.getIcmpSeq())
                    .addField("ttl", pi.getTtl())
                    .addField("rtt", pi.getRtt());
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

                Log.d(TAG, "doWork: Point:"+point.toLineProtocol());
            }
        });
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
                    Log.d(TAG, "onChanged: "+state.toString());
                    switch (state){
                        case RUNNING:
                        case ENQUEUED:
                            return;
                        case FAILED:
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

