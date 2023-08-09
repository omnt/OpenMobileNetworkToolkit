package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sender {

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
    @SerializedName("retransmits")
    @Expose
    public Integer retransmits;
    @SerializedName("max_snd_cwnd")
    @Expose
    public Integer maxSndCwnd;
    @SerializedName("max_snd_wnd")
    @Expose
    public Integer maxSndWnd;
    @SerializedName("max_rtt")
    @Expose
    public Integer maxRtt;
    @SerializedName("min_rtt")
    @Expose
    public Integer minRtt;
    @SerializedName("mean_rtt")
    @Expose
    public Integer meanRtt;
    @SerializedName("sender")
    @Expose
    public Boolean sender;

}
