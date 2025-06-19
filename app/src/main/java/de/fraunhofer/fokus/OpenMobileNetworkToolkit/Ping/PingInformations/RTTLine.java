/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTTLine extends PingInformation{
    public static Pattern pattern = Pattern.compile(
            "\\[(\\d+\\.\\d+)\\] (\\d+ bytes from ([^(]+)(?: \\(([^)]+)\\))?): icmp_seq=(\\d+) ttl=(\\d+) time=([\\d.]+) ms");

    private int icmpSeq;
    private int ttl;
    private double rtt;
    private String host;

    public RTTLine(String line) {
        super(line);
        super.setLineType(LINEType.RTT);
    }

    public void parse(){
        super.parse();
        Matcher matcher = pattern.matcher(this.getLine());
        if (matcher.find()) {
            icmpSeq = Integer.parseInt(matcher.group(5));
            ttl = Integer.parseInt(matcher.group(6));
            // Group 3 = hostname, Group 4 = optional IP
            String hostname = matcher.group(3).trim();
            String ip = matcher.group(4);
            host = (ip != null) ? ip : hostname;
            rtt = Double.parseDouble(matcher.group(7));
        }

    }

    public double getRtt() {
        return rtt;
    }

    public int getIcmpSeq() {
        return icmpSeq;
    }

    public int getTtl() {
        return ttl;
    }

    public String getHost() {
        return host;
    }

    public Point getPoint(){
        return super.getPoint()
            .addTag("toHost", this.getHost())
            .addField("icmp_seq", this.getIcmpSeq())
            .addField("ttl", this.getTtl())
            .addField("rtt", this.getRtt());
    }
}
