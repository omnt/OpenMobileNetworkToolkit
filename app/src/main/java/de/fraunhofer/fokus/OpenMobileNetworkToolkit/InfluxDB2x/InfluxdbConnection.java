/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteRetriableErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;

import java.io.IOException;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;


public class InfluxdbConnection {
    private final static String TAG = "InfluxDBConnection";
    private final SharedPreferencesGrouper spg;
    private final String url;
    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;
    private final GlobalVars gv;

    public InfluxdbConnection(String URL, String token, String org, String bucket,
                              Context context) {
        char[] token1 = token.toCharArray();
        this.url = URL;
        this.gv = GlobalVars.getInstance();
        influxDBClient = InfluxDBClientFactory.create(this.url, token1, org, bucket);
        influxDBClient.enableGzip();
        spg = SharedPreferencesGrouper.getInstance(context);
    }

    /**
     * Open the write API on the InfluxConnection
     */
    public void open_write_api() {
        if (writeApi != null) return;
        try {
            //influxDBClient = InfluxDBClientFactory.create(url, this.token, org, bucket);
            writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(1000)
                .flushInterval(1000)
                .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
                .bufferLimit(100000)
                .jitterInterval(10)
                .retryInterval(500)
                .exponentialBase(4)
                .build());
            writeApi.listenEvents(BackpressureEvent.class, value -> {
                Log.d(TAG, "open_write_api: Could not write to InfluxDBv2 due to backpressure");
            });
            writeApi.listenEvents(WriteSuccessEvent.class, value -> {
                //Log.d(TAG, "open_write_api: Write to InfluxDBv2 was successful");
                if ( spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)) {
                    gv.getLog_status().setColorFilter(Color.argb(255, 0, 255, 0));
                }
            });
            writeApi.listenEvents(WriteErrorEvent.class, value -> {
                Log.d(TAG, "open_write_api: Could not write to InfluxDBv2 due to error");
                if ( spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)) {
                    gv.getLog_status().setColorFilter(Color.argb(255, 255, 0, 0));
                }
            });
            writeApi.listenEvents(WriteRetriableErrorEvent.class, value -> {
                Log.d(TAG, "open_write_api: Could not write to InfluxDBv2 due to retriable error");
                if ( spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_influx", false)) {
                    gv.getLog_status().setColorFilter(Color.argb(255, 255, 0, 0));
                }
            });
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, "connect: Can't connect to InfluxDB");
            Log.d(TAG,e.toString());
        }
    }

    /**
     * Disconnect and destroy the client
     */
    public void disconnect() {
        // make sure we a instance of the client. This can happen on an app resume
        if (influxDBClient != null) {
            Log.d(TAG, "disconnect: Flushing Influx write API if possible");
            flush();
            try {
                Log.d(TAG, "disconnect: Closing Influx write API");
                writeApi.close();
                writeApi = null;
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.e(TAG, "disconnect: Error while closing write API");
                Log.d(TAG,e.toString());
            }
        } else {
            Log.d(TAG, "disconnect() was called on not existing instance of the influx client");
        }
        Log.d(TAG, "disconnect: InfluxDB connection closed");
    }

    /**
     * Add a point to the message queue
     */
    public boolean writePoint(Point point) {
        if (influxDBClient != null && ping()) {
            try {
                writeApi.writePoint(point);
            } catch (com.influxdb.exceptions.InfluxException e) {
                Log.d(TAG, "writePoint: Error while writing points to influx DB");
                Log.d(TAG,e.toString());
                return false;
            }
            return true;
        } else {
            Log.d(TAG, "writePoint: InfluxDB not reachable");
            return false;
        }
    }

    /**
     * Write string records to the queue
     * @param points String list of records
     * @return not yet useful
     * @throws IOException if record cant be written
     */
    public boolean writeRecords(List<String> points) throws IOException {
        new Thread(() -> {
            try {
                if (influxDBClient != null && ping()) {
                    try {
                        writeApi.writeRecords(WritePrecision.MS, points);
                    } catch (com.influxdb.exceptions.InfluxException e) {
                        Log.d(TAG, "writeRecords: Error while writing points to influx DB");
                        Log.d(TAG,e.toString());
                    }
                } else {
                    Log.d(TAG, "writeRecords: InfluxDB not reachable: " + url);
                }
            }
            catch (Exception e) {
                Log.d(TAG,e.toString());
            }
        }).start();
        return true;
    }

    /**
     *
     * @param points influx points to write
     * @return true if no exception happen
     * @throws IOException if points cant be written
     */
    public boolean writePoints(List<Point> points) throws IOException {
        new Thread(() -> {
            try {
                if (influxDBClient != null && ping()) {
                    try {
                        writeApi.writePoints(points);
                    } catch (com.influxdb.exceptions.InfluxException e) {
                        Log.e(TAG, "writePoint: Error while writing points to influx DB");
                        Log.d(TAG,e.toString());
                    }
                } else {
                    Log.e(TAG, "writePoints: InfluxDB not reachable: " + url);
                }
            }
            catch (Exception e) {
                Log.e(TAG, "writePoints: Error while writing points to influx DB");
                Log.d(TAG,e.toString());
            }
        }).start();
        return true;
    }

    /**
     * Onboard a influxDB and store credentials
     * @return if onboarding was successful or not
     */
    public boolean onboard() {
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
        } catch (com.influxdb.exceptions.InfluxException e) {
            return false;
        }
    }

    /**
     * If we can reach the influxDB call flush on the write API
     * @return true if flush was successful
     */
    public boolean flush() {
        new Thread(() -> {
            try {
                if (ping()) {
                    writeApi.flush();
                }
            } catch (Exception e) {
                Log.e(TAG, "flush: Error while flushing write API");
                Log.d(TAG, "flush: \n"+e.toString());
            }
        }).start();
        return true;
    }

    public WriteApi getWriteApi() {
        return writeApi;
    }
    public String getUrl() {
        return this.url;
    }

    public boolean ping() {
        return influxDBClient.ping();
    }
}

