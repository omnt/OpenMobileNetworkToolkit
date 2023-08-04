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

import java.util.List;

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

    public InfluxdbConnection(String URL, String token, String org, String bucket, Context context) {
            this.token = token.toCharArray();
            this.org = org;
            this.url = URL;
            this.bucket = bucket;
            this.context = context;
            sp = PreferenceManager.getDefaultSharedPreferences(context);
            influxDBClient = InfluxDBClientFactory.create(url, this.token, org, bucket);
    }

    public void open_write_api(){
        try {
            influxDBClient = InfluxDBClientFactory.create(url, this.token, org, bucket);
            writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                    .batchSize(1000)
                    .flushInterval(1000)
                    .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
                    .bufferLimit(100000)
                    .jitterInterval(10)
                    .retryInterval(500)
                    .exponentialBase(4)
                    .build());
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "connect: Can't connect to InfluxDB");
            e.printStackTrace();
        }
    }


    public void disconnect(){
        // make sure we a instance of the client. This can happen on an app resume
        if (influxDBClient != null) {
            Log.d(TAG, "disconnect: Flushing Influx write API if possible");
            flush();
            try {
                Log.d(TAG, "disconnect: Closing Influx write API");
                writeApi.close();
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "disconnect: Error while closing write API");
                e.printStackTrace();
            }
            try {
                Log.d(TAG, "disconnect: Closing influx connection");
                influxDBClient.close();
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "disconnect: Error while closing influx connection");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "disconnect() was called on not existing instance of the influx client");
        }
    }

    // Add a point to the message queue
    public boolean writePoint(Point point) {
        // only add the point if the database is reachable
        if (influxDBClient != null && influxDBClient.ping())  {
            try {
                writeApi.writePoint(point);
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "writePoint: Error while writing points to influx DB");
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.d(TAG, "writePoint: InfluxDB not reachable");
            return false;
        }
    }

    public boolean writePoints(List<Point> points) {
        // only add the point if the database is reachable
        if (influxDBClient != null && influxDBClient.ping())  {
            try {
                for (Point point : points){
                    writeApi.writePoint(point);
                }
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "writePointa: Error while writing points to influx DB");
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.d(TAG, "writePoints: InfluxDB not reachable");
            return false;
        }
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
                Log.d(TAG, "setup: Database onboarding successfully");
                return true;
            } else {
                Log.d(TAG, "setup: Database was already onboarded");
                return false;
            }
        } catch (com.influxdb.exceptions.InfluxException e){
            return false;
        }
    }

    // If we can reach the influxDB call flush on the write API
    public boolean flush(){
        if (influxDBClient.ping()) {
            writeApi.flush();
            return true;
        } else {
            return false;
        }
    }

    public boolean ping(){
        return influxDBClient.ping();
    }
}

