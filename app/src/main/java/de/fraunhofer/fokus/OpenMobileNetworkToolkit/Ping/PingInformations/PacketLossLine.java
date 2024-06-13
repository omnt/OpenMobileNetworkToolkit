package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations;

public class PacketLossLine extends PingInformation{
    private double packetLoss;
    private long transmitted;
    private long received;
    public PacketLossLine(String line) {
        super(line);
        this.setLineType(LINEType.PACKET_LOSS);
    }

    public void parse(){

    }
    public double getPacketLoss() {
        return ;
    }
}
