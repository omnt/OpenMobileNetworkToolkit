package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Udp{
    @SerializedName("socket")
    @Expose
    public Integer socket;
    @SerializedName("start")
    @Expose
    public Integer start;
    @SerializedName("end")
    @Expose
    public Float end;
    @SerializedName("seconds")
    @Expose
    public Float seconds;
    @SerializedName("bytes")
    @Expose
    public Long bytes;
    @SerializedName("bits_per_second")
    @Expose
    public Float bitsPerSecond;
    @SerializedName("packets")
    @Expose
    public Integer packets;
    @SerializedName("jitter_ms")
    @Expose
    public Float jitterMs;
    @SerializedName("lost_packets")
    @Expose
    public Integer lostPackets;
    @SerializedName("lost_percent")
    @Expose
    public Float lostPercent;
    @SerializedName("out_of_order")
    @Expose
    public Integer outOfOrder;
    @SerializedName("sender")
    @Expose
    public Boolean sender;
}