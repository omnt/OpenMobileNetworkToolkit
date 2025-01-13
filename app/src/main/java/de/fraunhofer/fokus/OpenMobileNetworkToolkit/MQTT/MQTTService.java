package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MQTTService extends Service {
    private static final String TAG = "MQTTService";
    private Context context;
    private SharedPreferences mqttSP;
    private NotificationCompat.Builder builder;
    public NotificationManager nm;
    private Handler notificationHandler;
    private Mqtt5AsyncClient client;
    private SharedPreferencesGrouper spg;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupSharedPreferences(){
        spg = SharedPreferencesGrouper.getInstance(context);
        mqttSP = spg.getSharedPreference(SPType.mqtt_sp);
        mqttSP.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if(key == null) return;
            if (key.equals("mqtt_host")) {
                Log.d(TAG, "MQTT Host update: " + sharedPreferences.getString("mqtt_host", ""));
                client.disconnect();
                createClient();
                createNotification();
            }
        });
    }

    public void createClient(){
        String addressString = mqttSP.getString("mqtt_host", "localhost:1883");
        String host = null;
        int port = -1;
        try {
            host = addressString.split(":")[0];
            port = Integer.parseInt(addressString.split(":")[1]);
        } catch (Exception e) {
            Log.e(TAG, "createClient: Invalid address string: " + addressString);
            return;
        }
        if(host == null || port == -1){
            Log.e(TAG, "createClient: Invalid address string: " + addressString);
            return;
        }
        InetSocketAddress address = new InetSocketAddress(host, port);
        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverAddress(address)
                .automaticReconnect()
                .initialDelay(200, TimeUnit.MILLISECONDS)
                .maxDelay(30, TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .addConnectedListener(context -> {
                    Log.i(TAG, "createClient: Connected to MQTT server");
                    createNotification();
                })
                .addDisconnectedListener(context -> {
                    Log.i(TAG, "createClient: Disconnected from MQTT server");
                    createNotification();
                })
                .buildAsync();

        Log.i(TAG, "createClient: Client created with address: " + addressString);
    }

    private void createNotification(){
        StringBuilder s = new StringBuilder();
        String address = spg.getSharedPreference(SPType.mqtt_sp).getString("mqtt_host", "None");
        if(address.equals("None")){
            s.append("MQTT Host: None\n");
        } else {
            s.append("Host: ").append(address).append("\n");
            s.append("State: ").append(client.getState().toString()).append("\n");
        }
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(s));
        nm.notify(3, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = getSystemService(NotificationManager.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // create notification
            builder = new NotificationCompat.Builder(this, "OMNT_notification_channel")
                    .setContentTitle(getText(R.string.mqtt_service_running))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setColor(Color.WHITE)
                    .setContentIntent(pendingIntent)
                    // prevent to swipe the notification away
                    .setOngoing(true)
                    // don't wait 10 seconds to show the notification
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        } else {
            // create notification
            builder = new NotificationCompat.Builder(this, "OMNT_notification_channel")
                    .setContentTitle(getText(R.string.mqtt_service_running))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setColor(Color.WHITE)
                    .setContentIntent(pendingIntent)
                    // prevent to swipe the notification away
                    .setOngoing(true);
        }

    }

    public void publishToTopic(String topic, String message){
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(message.getBytes())
                .send();
    }

    public void disconnectClient(){
        CompletableFuture<Void> disconnect = client.disconnect();
        disconnect.whenComplete((aVoid, throwable) -> {
            if(throwable != null){
                Log.e(TAG, "disconnectClient: Error disconnecting from MQTT server: " + throwable.getMessage());
            } else {
                Log.i(TAG, "disconnectClient: Disconnected from MQTT server");
            }
        });
    }

    public  void connectClient(){
        CompletableFuture<Mqtt5ConnAck> connAck = client.connectWith()
                .keepAlive(10)
                .willPublish()
                    .topic("device/OMNT3/status")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload("offline".getBytes())
                    .retain(true)
                    .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                    .contentType("text/plain")
                    .noMessageExpiry()
                    .applyWillPublish()
                .send();

        connAck.whenComplete((mqtt5ConnAck, throwable) -> {
            if(throwable != null){
                Log.e(TAG, "connectClient: Error connecting to MQTT server: " + throwable.getMessage());
            } else {
                Log.i(TAG, "connectClient: Connected to MQTT server");
            }
        });
    }

    private boolean parseBoolean(String value){
        switch (value.toLowerCase()){
            case "true":
            case "1":
                return true;
            case "false":
            case "0":
                return false;
        }
        return false;
    }

    private void handleConfigMessage(String topic, String payload){


        // config logging service
        if(topic.contains("/logging/enable")){
            Log.d(TAG, "handleConfigMessage: Enable Logging: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("enable_logging", parseBoolean(payload)).apply();
            return;
        }

        if(topic.contains("/logging/start_on_boot")){
            Log.d(TAG, "handleConfigMessage: Start on Boot: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("start_logging_on_boot", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/logging/notification_update_enabled")){
            Log.d(TAG, "handleConfigMessage: Notification Update: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("enable_notification_update", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/logging/interval_ms")){
            Log.d(TAG, "handleConfigMessage: Logging Interval: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putString("logging_interval", payload).apply();
            return;
        }

        // config influxdv_v2 parameter
        if(topic.contains("/influxv2/enabled")){
            Log.d(TAG, "handleConfigMessage: Enable Influx: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("enable_influx", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/influxv2/address")){
            Log.d(TAG, "handleConfigMessage: Influx Address: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putString("influx_URL", payload).apply();
            return;
        }
        if(topic.contains("/influxv2/token")){
            Log.d(TAG, "handleConfigMessage: Influx Token received!");
            spg.getSharedPreference(SPType.logging_sp).edit().putString("influx_token", payload).apply();
            return;
        }
        if(topic.contains("/influxv2/bucket")){
            Log.d(TAG, "handleConfigMessage: Influx Bucket: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putString("influx_bucket", payload).apply();
            return;
        }
        if(topic.contains("/influxv2/org")){
            Log.d(TAG, "handleConfigMessage: Influx Org: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putString("influx_org", payload).apply();
            return;
        }
        if(topic.contains("/influxv2/tags")){
            Log.d(TAG, "handleConfigMessage: Influx Tags: " + payload);
            //spg.getSharedPreference(SPType.logging_sp).edit().putString("influx_org", payload).apply();
            //TODO
            return;
        }


        // config log file
        if(topic.contains("/file/enabled")){
            Log.d(TAG, "handleConfigMessage: Enable Local File Log: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("enable_local_file_log", parseBoolean(payload)).apply();
            return;
        }

        // config logging content
        if(topic.contains("/content/network_information")){
            Log.d(TAG, "handleConfigMessage: Network Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("influx_network_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/signal_information")){
            Log.d(TAG, "handleConfigMessage: Signal Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("log_signal_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/cell_information")){
            Log.d(TAG, "handleConfigMessage: Cell Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("influx_cell_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/neighbour_cells")){
            Log.d(TAG, "handleConfigMessage: Neighbour Cells: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("log_neighbour_cells", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/throughput_information")){
            Log.d(TAG, "handleConfigMessage: Throughput Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("influx_throughput_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/wifi_information")){
            Log.d(TAG, "handleConfigMessage: Wifi Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("log_wifi_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/battery_information")){
            Log.d(TAG, "handleConfigMessage: Battery Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("influx_battery_data", parseBoolean(payload)).apply();
            return;
        }
        if(topic.contains("/content/ip_information")){
            Log.d(TAG, "handleConfigMessage: IP Information: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("influx_ip_address_data", parseBoolean(payload)).apply();
            return;
        }


        Log.d(TAG, "handleConfigMessage: No matching topic found: " + topic);

        return;
    }

    private void subsribetoTopic(String topic){
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    if (!publish.getPayload().isPresent()){
                        Log.e(TAG, "Received empty payload from topic: " + publish.getTopic());
                        return;
                    };
                    new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG, "Received config message: " + publish.getTopic());
                            String payload = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
                            handleConfigMessage(publish.getTopic().toString(), payload);
                        }
                    }.run();
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if(throwable != null){
                        Log.e(TAG, "Error subscribing to topic: " + topic + " Error: " + throwable.getMessage());
                    } else {
                        Log.i(TAG, "Subscribed to topic: " + topic);
                    }
                });
    }


    private void subscribeToAllTopics(){
        // TODO fix hardcoded deviceID
        subsribetoTopic("device/OMNT3/#");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start MQTT service");
        context = getApplicationContext();
        mqttSP = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.mqtt_sp);
        startForeground(3, builder.build());
        setupSharedPreferences();
        createClient();
        connectClient();
        subscribeToAllTopics();

        return START_STICKY;
    }

}
