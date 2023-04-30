
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stream {

    @SerializedName("socket")
    @Expose
    public Integer socket;
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
    @SerializedName("snd_cwnd")
    @Expose
    public Integer sndCwnd;
    @SerializedName("packets")
    @Expose
    public int packets;
    @SerializedName("jitter_ms")
    @Expose
    public Float jitterMs;
    @SerializedName("lost_packets")
    @Expose
    public Integer lostPackets;
    @SerializedName("lost_percent")
    @Expose
    public Float lostPercent;
    @SerializedName("snd_wnd")
    @Expose
    public Integer sndWnd;
    @SerializedName("rtt")
    @Expose
    public Integer rtt;
    @SerializedName("rttvar")
    @Expose
    public Integer rttvar;
    @SerializedName("pmtu")
    @Expose
    public Integer outOfOrder;
    @SerializedName("out_of_order")
    @Expose
    public Integer pmtu;
    @SerializedName("omitted")
    @Expose
    public Boolean omitted;
    @SerializedName("sender")
    @Expose
    public Boolean sender;

}
