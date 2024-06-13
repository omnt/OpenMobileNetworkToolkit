/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingInformation {
    private String line;
    private LINEType lineType;
    private Double unixTimestamp;

    public PingInformation(String line) {
        this.line = line;
    }

    private double unixTimestampWithMicrosToMillis(double timestampWithMicros) {
        long seconds = (long) timestampWithMicros;
        long microseconds = (long) ((timestampWithMicros - seconds) * 1e6);
        return seconds * 1000 + microseconds / 1000;
    }
    private LINEType getLineType(String line){
        if (line.contains("bytes from")) {
            return LINEType.RTT;
        } else if (line.contains("Unreachable")) {
            return LINEType.UNREACHABLE;
        } else if (line.contains("Request timeout")) {
            return LINEType.TIMEOUT;
        } else if (line.contains("packets transmitted")){
            return LINEType.PACKET_LOSS;
        } else {
            return LINEType.UNKNOWN;
        }
    }
    private void parseUnixTimestamp(){
        //Matcher matcher = //todo split and also split
        //if (matcher.find()) {
        //    unixTimestamp = unixTimestampWithMicrosToMillis(Double.parseDouble(matcher.group(1)));
        //}
    }
    public void parse() {
        parseUnixTimestamp();
        switch (getLineType(line)){
            case RTT:
                RTTLine rttLine = new RTTLine(line);
                rttLine.parse();
                break;
            case UNREACHABLE:
                //TDODO
                break;
            case TIMEOUT:
                //TODO
                break;
            case PACKET_LOSS:
                PacketLossLine packetLossLine = new PacketLossLine(line);
                packetLossLine.parse();
                break;
            case UNKNOWN:
                break;
        }

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

}
