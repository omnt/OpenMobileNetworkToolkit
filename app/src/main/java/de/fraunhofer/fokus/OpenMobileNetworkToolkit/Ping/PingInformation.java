/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingInformation {
    private Pattern pattern = Pattern.compile(
        "\\[(\\d+\\.\\d+)\\] (\\d+ bytes from (\\S+|\\d+\\.\\d+\\.\\d+\\.\\d+)): icmp_seq=(\\d+) ttl=(\\d+) time=([\\d.]+) ms");
    private Double unixTimestamp;
    private int icmpSeq;
    private int ttl;
    private double rtt;
    private String host;
    private String line;

    public PingInformation(String line) {
        this.line = line;
    }

    private double unixTimestampWithMicrosToMillis(double timestampWithMicros) {
        long seconds = (long) timestampWithMicros;
        long microseconds = (long) ((timestampWithMicros - seconds) * 1e6);
        return seconds * 1000 + microseconds / 1000;
    }

    public boolean parse() {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            unixTimestamp = unixTimestampWithMicrosToMillis(Double.parseDouble(matcher.group(1)));
            icmpSeq = Integer.parseInt(matcher.group(4));
            ttl = Integer.parseInt(matcher.group(5));
            host = matcher.group(3);
            rtt = Double.parseDouble(matcher.group(6));
            return true;
        }
        return false;
    }

    public double getRtt() {
        return rtt;
    }

    public Double getUnixTimestamp() {
        return unixTimestamp;
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

    public String getLine() {
        return line;
    }

}
