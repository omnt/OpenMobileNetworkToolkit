package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Start {

    @SerializedName("connected")
    @Expose
    public List<Connected> connected = null;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("system_info")
    @Expose
    public String systemInfo;
    @SerializedName("timestamp")
    @Expose
    public Timestamp timestamp;
    @SerializedName("connecting_to")
    @Expose
    public ConnectingTo connectingTo;
    @SerializedName("cookie")
    @Expose
    public String cookie;
    @SerializedName("tcp_mss_default")
    @Expose
    public Integer tcpMssDefault;
    @SerializedName("target_bitrate")
    @Expose
    public Integer targetBitrate;
    @SerializedName("sock_bufsize")
    @Expose
    public Integer sockBufsize;
    @SerializedName("sndbuf_actual")
    @Expose
    public Integer sndbufActual;
    @SerializedName("rcvbuf_actual")
    @Expose
    public Integer rcvbufActual;
    @SerializedName("test_start")
    @Expose
    public TestStart testStart;

}
