package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Connected {

    @SerializedName("socket")
    @Expose
    public Integer socket;
    @SerializedName("local_host")
    @Expose
    public String localHost;
    @SerializedName("local_port")
    @Expose
    public Integer localPort;
    @SerializedName("remote_host")
    @Expose
    public String remoteHost;
    @SerializedName("remote_port")
    @Expose
    public Integer remotePort;

}
