package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sum {

    @SerializedName("start")
    @Expose
    public Float start;
    @SerializedName("end")
    @Expose
    public Float end;
    @SerializedName("seconds")
    @Expose
    public Float seconds;
    @SerializedName("bytes")
    @Expose
    public Integer bytes;
    @SerializedName("bits_per_second")
    @Expose
    public Float bitsPerSecond;
    @SerializedName("retransmits")
    @Expose
    public Integer retransmits;
    @SerializedName("omitted")
    @Expose
    public Boolean omitted;
    @SerializedName("sender")
    @Expose
    public Boolean sender;

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

}
