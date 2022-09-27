
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class End {

    @SerializedName("streams")
    @Expose
    public List<Stream__1> streams = null;
    @SerializedName("sum_sent")
    @Expose
    public SumSent sumSent;
    @SerializedName("sum_received")
    @Expose
    public SumReceived sumReceived;
    @SerializedName("cpu_utilization_percent")
    @Expose
    public CpuUtilizationPercent cpuUtilizationPercent;
    @SerializedName("sender_tcp_congestion")
    @Expose
    public String senderTcpCongestion;
    @SerializedName("receiver_tcp_congestion")
    @Expose
    public String receiverTcpCongestion;

}