/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.util.Log;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;


public class InfluxdbConnection {
    // todo read this from app settings
    private final static String TAG = "InfluxDBConnection";
    private char[] token;
    private String org;
    private String bucket;
    private String url;
    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;


    public InfluxdbConnection(String URL, String token, String org, String bucket) {
            this.token = token.toCharArray();
            this.org = org;
            this.url = URL;
            this.bucket = bucket;
    }

    public boolean connect() {
        influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
        writeApi = influxDBClient.makeWriteApi();
        Log.d(TAG, "connect: Connected to influx DB");
        return true;
    }

    public boolean disconnect(){
        writeApi.flush();
        try {
            writeApi.close();
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "disconnect: Error while closing write API");
            e.printStackTrace();
        }
        try {
            influxDBClient.close();
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "disconnect: Error while closing influx connection");
            e.printStackTrace();
        }
        return true;
    }

    // Add a point to the message queue
    public boolean writePoint(Point point) {
        try {
            Log.d(TAG, "writePoint: "+point.toLineProtocol());
            writeApi.writePoint(point);
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "disconnect: Error while writing points to influx DB");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void sendAll(){
        writeApi.flush();
    }

}

