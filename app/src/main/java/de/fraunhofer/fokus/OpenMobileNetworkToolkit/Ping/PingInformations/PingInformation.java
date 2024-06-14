/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingInformation {
    private String line;
    private LINEType lineType;
    private Double unixTimestamp;
    private DataProvider dp;
    public PingInformation(String line) {
        this.line = line;
    }

    private double unixTimestampWithMicrosToMillis(double timestampWithMicros) {
        long seconds = (long) timestampWithMicros;
        long microseconds = (long) ((timestampWithMicros - seconds) * 1e6);
        return seconds * 1000 + microseconds / 1000;
    }

    private void parseUnixTimestamp(){
        Pattern pattern = Pattern.compile("\\[\\d+\\.\\d+\\]");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String unixTimestampString = matcher.group(0).replace("[", "").replace("]", "");
            unixTimestamp = unixTimestampWithMicrosToMillis(Double.parseDouble(unixTimestampString));
        }
    }
    public void parse() {
        parseUnixTimestamp();
    }
    public void setLineType(LINEType lineType) {
        this.lineType = lineType;
    }
    public LINEType getLineType() {
        return lineType;
    }
    public String getLine() {
        return line;
    }
    public Double getUnixTimestamp() {
        return unixTimestamp;
    }

    public Point getPoint(){
        Point point = new Point("Ping");
        point.time(this.getUnixTimestamp(), WritePrecision.MS);
        return point;
    }
}
