package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Service extends Service {
    public final static int FOREGROUND_SERVICE_TYPE = FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
    public final static int NOTIFICATION_ID = 1002;
    private static final String CHANNEL_ID = "Iperf3ServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

            Files.createDirectories(Paths.get(iperf3Input.getRawFile()));
            Files.createDirectories(Paths.get(iperf3Input.getLineProtocolFile()));
        } catch (IOException e){
            Toast.makeText(getApplicationContext(),"Could not create Dir files!", Toast.LENGTH_SHORT).show();
        }

        Iperf3Executor iperf3Executor = new Iperf3Executor(this, iperf3Input);
        iperf3Executor.start();
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