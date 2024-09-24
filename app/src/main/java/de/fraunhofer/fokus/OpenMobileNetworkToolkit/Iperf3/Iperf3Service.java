package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.WorkManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Service extends Service {
    public final static int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    public final static int NOTIFICATION_ID = 1002;
    private static final String CHANNEL_ID = "Iperf3ServiceChannel";
    private static final String TAG = "Iperf3Service";



    private HashMap<String, Iperf3Parser> iperfParser = new HashMap<>();
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            Toast.makeText(getApplicationContext(),"No intent!", Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }
        Iperf3Input iperf3Input = intent.getParcelableExtra("input");

        if(iperf3Input == null){
            Toast.makeText(getApplicationContext(),"No input data!", Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("iPerf3 Service")
                .setContentText("no active test")
                .setSmallIcon(R.drawable.outline_speed_24)
                .build();


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }
        try{

            Files.createDirectories(Paths.get(Iperf3Input.jsonDirPath));
            Files.createDirectories(Paths.get(Iperf3Input.lineProtocolDirPath));
        } catch (IOException e){
            Toast.makeText(getApplicationContext(),"Could not create Dir files!", Toast.LENGTH_SHORT).show();
        }

        Iperf3Executor iperf3Executor = new Iperf3Executor(getApplicationContext(), iperf3Input);
        iperf3Executor.start();

        iperfParser.put(iperf3Input.getUuid(), new Iperf3Parser(getApplicationContext(), iperf3Input.getRawFile(), iperf3Input));
        Objects.requireNonNull(iperfParser.get(iperf3Input.getUuid())).addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "interval":
                    Intent intent1 = new Intent();
                    Interval interval = (Interval) evt.getNewValue();
                    intent1.setAction(getApplicationContext().getApplicationInfo().packageName + ".broadcast.iperf3.INTERVAL");
                    intent1.putExtra("interval", interval);
                    intent1.putExtra("uuid", iperf3Input.getUuid());
                    Log.d(TAG, "onStartCommand: sending intent!");
                    sendBroadcast(intent1);
                    break;
                case "error":
                    // Handle error case if needed
                    break;
            }
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    File file = new File(iperf3Input.getRawFile());
                    if(file.exists()) iperfParser.get(iperf3Input.getUuid()).parse();
                }
            }
        };
        Future future = executorService.submit(runnable);


        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.getWorkInfoByIdLiveData(iperf3Executor.getUuid()).observeForever(workInfo -> {



            switch (workInfo.getState()){
                case ENQUEUED:
                    break;
                case RUNNING:
                   break;
                case SUCCEEDED:
                    break;
                case FAILED:
                case CANCELLED:
                case BLOCKED:
                    future.cancel(true);
                    break;

            }
        });



        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Iperf3 Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}