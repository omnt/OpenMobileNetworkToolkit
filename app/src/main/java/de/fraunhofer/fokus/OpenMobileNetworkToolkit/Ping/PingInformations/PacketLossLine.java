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

public class PacketLossLine extends PingInformation{
    private double packetLoss;
    private long packetsTransmitted;
    private long packetsReceived;
    Pattern pattern = Pattern.compile("(\\d+)\\s+packets\\s+transmitted.*?(\\d+)\\s+received.*?(\\d+%)\\s+packet\\s+loss.*?time\\s+(\\d+ms)");
    Matcher matcher;
    public PacketLossLine(String line) {
        super(line);
        this.setLineType(LINEType.PACKET_LOSS);
    }
    public void parse(){
        super.parse();
        matcher = pattern.matcher(this.getLine());
        if(matcher.find()){
            packetsTransmitted = Long.parseLong(matcher.group(1));
            packetsReceived = Long.parseLong(matcher.group(2));
            packetLoss = Double.parseDouble(matcher.group(3).replace("%", ""));
        }

    }
    public long getPacketsReceived() {
        return packetsReceived;
    }
    public long getPacketsTransmitted() {
        return packetsTransmitted;
    }
    public double getPacketLoss() {
        return packetLoss;
    }

    public Point getPoint(){
        return super.getPoint()
            .addField("packets_transmitted", this.getPacketsTransmitted())
            .addField("packets_received", this.getPacketsReceived())
            .addField("packet_loss", this.getPacketLoss())
                .time(System.currentTimeMillis(), WritePrecision.MS);
        //ping does not provide timestamp for packet loss line
    }
}
