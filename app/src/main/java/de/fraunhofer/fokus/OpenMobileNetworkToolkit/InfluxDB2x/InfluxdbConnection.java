/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.write.Point;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;


public class InfluxdbConnection {
    private final static String TAG = "InfluxDBConnection";
    private char[] token;
    private String org;
    private String bucket;
    private String url;
    private Context context;
    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public InfluxdbConnection(String URL, String token, String org, String bucket, Context context) {
            this.token = token.toCharArray();
            this.org = org;
            this.url = URL;
            this.bucket = bucket;
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean connect() {
        try {
            influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
            writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                    .batchSize(1000)
                    .flushInterval(1000)
                    .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
                    .bufferLimit(100000)
                    .jitterInterval(10)
                    .retryInterval(500)
                    .build());
            Log.d(TAG, "connect: Connected to InfluxDB");

        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "connect: Can't connect to InfluxDB");
            e.printStackTrace();
            return false;

        }
        return true;
    }

    public boolean disconnect(){
        writeApi.flush();
        try {
            writeApi.close();
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "disconnect: Error while closing write API");
            e.printStackTrace();
            return false;
        }
        try {
            influxDBClient.close();
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "disconnect: Error while closing influx connection");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Add a point to the message queue
    public boolean writePoint(Point point) {
        //if (influxDBClient != null && influxDBClient.ready().getStatus() == Ready.StatusEnum.READY)  {
            try {
                writeApi.writePoint(point);
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "disconnect: Error while writing points to influx DB");
                e.printStackTrace();
                return false;
            }
            return true;
        //} else {
        //    Log.d(TAG, "influx client not ready");
        //    return false;
        //}
    }

    // Setup a local database and store credentials
    public  boolean setup() {
        try {
            if (influxDBClient.isOnboardingAllowed()) {
                OnboardingRequest or = new OnboardingRequest();
                or.bucket("omnt");
                or.org("OMNT");
                or.password("omnt2022"); //todo THIS SHOULD NOT BE HARDCODED
                or.username("omnt");
                or.token("1234567890"); //todo generate a token
                influxDBClient.onBoarding(or);
                Log.d(TAG, "Database onboarding successfully");
                return true;
            } else {
                Log.d(TAG, "Database was already onboarded");
                return false;
            }

        } catch (com.influxdb.exceptions.InfluxException e){
            return false;
        }
    }

    public void sendAll(){
        writeApi.flush();
    }

}

