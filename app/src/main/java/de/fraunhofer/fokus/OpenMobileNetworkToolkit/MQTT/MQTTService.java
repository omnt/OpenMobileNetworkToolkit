package de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class MQTTService extends Service {
    private static final String TAG = "MQTTService";
    private Context context;
    private SharedPreferences mqttSP;

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
                client = Mqtt5Client.builder()
                        .identifier(UUID.randomUUID().toString())
                        .serverHost(sharedPreferences.getString("mqtt_host", "tcp://localhost:1883"))
                        .buildAsync();
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
                .buildAsync();
        Log.i(TAG, "createClient: Client created with address: " + addressString);
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
                .send();

        connAck.whenComplete((mqtt5ConnAck, throwable) -> {
            if(throwable != null){
                Log.e(TAG, "connectClient: Error connecting to MQTT server: " + throwable.getMessage());
            } else {
                Log.i(TAG, "connectClient: Connected to MQTT server");
            }
        });
    }

    private void handleConfigMessage(String topic, String payload){
        // TODO impl
    }

    private void handleCommandMessage(String topic, String payload){
        // TODO implement
    }

    private void subsribeToConfigTopic(String topic){
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(publish -> {
                    Log.d(TAG, "Received message: " + publish.getTopic());
                    String payload = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
                    Log.d(TAG, "Received message: " + payload);
                })
                .send();
    }

    private void subsribeToCommandTopic(String topic){
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(publish -> {
                    Log.d(TAG, "Received message: " + publish.getTopic());
                    String payload = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
                    Log.d(TAG, "Received message: " + payload);
                })
                .send();
    }

    private void subscribeToAllTopics(){
        // TODO fix hardcoded deviceID
        subsribeToConfigTopic("device/OMNT3/config/#");
        subsribeToCommandTopic("device/OMNT3/command/#");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Start logging service");
        GlobalVars gv = GlobalVars.getInstance();
        // setup class variables
        context = getApplicationContext();
        mqttSP = SharedPreferencesGrouper.getInstance(context).getSharedPreference(SPType.mqtt_sp);
        setupSharedPreferences();
        createClient();
        connectClient();

        subscribeToAllTopics();

        return START_STICKY;
    }
}
