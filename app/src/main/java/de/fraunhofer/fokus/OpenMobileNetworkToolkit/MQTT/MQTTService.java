/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT;

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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.multiprocess.RemoteWorkManager;


import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.influxdb.client.domain.Run;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.CustomEventListener;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler.Iperf3Handler;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.Handler.PingHandler;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MainActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.RemoteWorkInfoChecker;

public class MQTTService extends Service {
    private static final String TAG = "MQTTService";
    private Context context;
    private SharedPreferences mqttSP;
    private NotificationCompat.Builder builder;
    public NotificationManager nm;
    private Handler notificationHandler;
    private Mqtt5AsyncClient client;
    private SharedPreferencesGrouper spg;
    private String deviceName;
    private Iperf3Handler iperf3Handler;
    private PingHandler pingHandler;
    private boolean isEnabled = false;

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
                .identifier(deviceName)
                .serverAddress(address)
                .automaticReconnect()
                .initialDelay(5, TimeUnit.SECONDS)
                .maxDelay(30, TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .addConnectedListener(context -> {
                    Log.i(TAG, "createClient: Connected to MQTT server");
                    createNotification();
                    publishToTopic(String.format("device/%s/status", deviceName), "1", false);
                })
                .addDisconnectedListener(context -> {
                    Log.i(TAG, "createClient: Disconnected from MQTT server");
                    createNotification();
                })
                .willPublish()
                    .topic(String.format("device/%s/status", deviceName))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload("0".getBytes())
                    .retain(true)
                    .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                    .contentType("text/plain")
                    .noMessageExpiry()
                    .applyWillPublish()
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

    public void publishToTopic(String topic, String message, boolean retain){
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(message.getBytes())
                .retain(retain)
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
                .keepAlive(1)
                .willPublish()
                    .topic(String.format("device/%s/status", deviceName))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload("0".getBytes())
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
                publishToTopic(String.format("device/%s/status", deviceName), "1", true);
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
        if(topic.contains("/content/measurement_name")){
            Log.d(TAG, "handleConfigMessage: Measurement Name: " + payload);
            spg.getSharedPreference(SPType.logging_sp).edit().putString("measurement_name", payload).apply();
            return;
        }
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

        if(topic.contains("/iperf3/command")){
            Log.d(TAG, "handleConfigMessage: Iperf3 Command: " + payload);
            iperf3Handler = new Iperf3Handler(context);
            try {
                iperf3Handler.parsePayload(payload);
            } catch (Exception e) {
                Log.e(TAG, "handleConfigMessage: Error parsing iperf3 payload: " + e.getMessage());
                //TODO PUBLISH ERROR
            }
            return;
        }
        if(topic.contains("/iperf3/enable")){
            Log.d(TAG, "handleConfigMessage: Enable Iperf3: " + payload);

            if(iperf3Handler != null && parseBoolean(payload)){
                iperf3Handler.preperareSequence(getApplicationContext());
                //TODO PUBLISH iperf3 sequence enabled
            } else if(iperf3Handler != null && !parseBoolean(payload)){
                iperf3Handler.disableSequence(getApplicationContext());
                //TODO PUBLISH iperf3 sequence disabled
            }
            return;
        }

        if(topic.contains("/ping/command")){
            Log.d(TAG, "handleConfigMessage: Ping Command: " + payload);
            pingHandler = new PingHandler();
            try {
                pingHandler.parsePayload(payload);
            } catch (Exception e) {
                Log.e(TAG, "handleConfigMessage: Error parsing ping payload: " + e.getMessage());
                //TODO PUBLISH ERROR
            }
            return;
        }

        if(topic.contains("/ping/enable")){
            Log.d(TAG, "handleConfigMessage: Enable Ping: " + payload);

            if(pingHandler != null && parseBoolean(payload)){
                pingHandler.preperareSequence(getApplicationContext());
                //TODO PUBLISH ping sequence enabled
            } else if(pingHandler != null && !parseBoolean(payload)){
                pingHandler.disableSequence(getApplicationContext());
                //TODO PUBLISH ping sequence disabled
            }
            return;
        }

        if(topic.contains("/sequence/enable")){
            Log.d(TAG, "handleConfigMessage: Enable Sequence: " + payload);
            setEnabled(parseBoolean(payload));
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
                        Log.d(TAG, "Received message: " + publish.getTopic());
                        String payload = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
                        handleConfigMessage(publish.getTopic().toString(), payload);
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
        subsribetoTopic(String.format("device/%s/#", deviceName));
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start MQTT service");
        context = getApplicationContext();
        mqttSP = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.mqtt_sp);
        deviceName = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.default_sp).getString("device_name", "null").strip();
        startForeground(3, builder.build());
        setupSharedPreferences();
        createClient();
        if(client == null){
            Log.e(TAG, "onStartCommand: Client is null");
            spg.getSharedPreference(SPType.mqtt_sp).edit().putBoolean("enable_mqtt", false).apply();
            return START_NOT_STICKY;
        }
        connectClient();

        subscribeToAllTopics();

        return START_STICKY;
    }

    private void executeWork() {
        new Runnable(){
            @Override
            public void run() {
                if (iperf3Handler != null && isEnabled) {
                    iperf3Handler.enableSequence();
                } else {
                    Log.d(TAG, "executeWork: Iperf3 Handler is either null or not enabled");
                }
            }
        }.run();
        new Runnable(){
            @Override
            public void run() {
                if (pingHandler != null && isEnabled) {
                    pingHandler.enableSequence();
                } else {
                    Log.d(TAG, "executeWork: Ping Handler is either null or not enabled");
                }
            }
        }.run();

        CustomEventListener listener = new CustomEventListener() {
            @Override
            public void onChange(HashMap<UUID, WorkInfo> workInfos) {
                for (WorkInfo info : workInfos.values()) {
                    WorkInfo.State state = info.getState();
                    Log.d(TAG, "onChange: WorkInfo: " + info.getTags() + " State: " + state);
                    Data data = info.getOutputData();
                    Log.i(TAG, "onChange: "+data.toString());

                    publishToTopic("device/"+deviceName+"/campaign/status", String.valueOf(state.ordinal()), false);
                }

            }
        };
        //startWorkInfoChecker(RemoteWorkManager.getInstance(context), iperf3Handler.getExecutorWorkRequests(context), listener);
        //TODO
    }

    private void startWorkInfoChecker(RemoteWorkManager remoteWorkManager, ArrayList<OneTimeWorkRequest> workRequests, CustomEventListener listener) {
        ArrayList<UUID> workIdGroups = new ArrayList<>();
        for (OneTimeWorkRequest workRequest : workRequests) {
            workIdGroups.add(workRequest.getId());
        }
        RemoteWorkInfoChecker remoteWorkInfoChecker = new RemoteWorkInfoChecker(remoteWorkManager, workIdGroups);
        remoteWorkInfoChecker.setListener(listener);
        remoteWorkInfoChecker.start();
    }

    private void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        executeWork();
    }
}
