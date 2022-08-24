/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;



import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;


public class InfluxdbConnection {
    private static final char[] token = "TJM4ZjsoUwsiHC1Zr_hz__oa83VAKTRvfqfeg_4BEGhyb6ox_MthgA0RZzJ1odLLmGgWVgteHXRXAP_qUJL4Gw==".toCharArray();
    private static String org = "hoelle";
    private static String bucket = "97542ee36889738b";
    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;


    public boolean connect() {
        influxDBClient = InfluxDBClientFactory.create("http://10.79.30.6:8086", token, org, bucket);
        writeApi = influxDBClient.makeWriteApi();
        return true;
    }

    public boolean disconnect(){
        influxDBClient.close();
        return true;
    }

    // Add a point to the message queue
    public boolean writePoint(Point point) {

/*        Point point = Point.measurement("5G")
                .addTag("Aladin", "Hans")
                .addField("RSSI", 9000)
                .time(currentTimeMillis(), WritePrecision.MS);*/

        writeApi.writePoint(point);

        return true;
    }

}

