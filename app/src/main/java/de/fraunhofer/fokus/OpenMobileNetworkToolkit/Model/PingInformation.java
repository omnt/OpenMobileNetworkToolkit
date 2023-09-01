package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class PingInformation {
    private Double unixTimestamp;
    private int icmpSeq;
    private int ttl;
    private double rtt;

    private String host;
}
