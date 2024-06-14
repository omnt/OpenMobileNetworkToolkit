package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations;

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
}
