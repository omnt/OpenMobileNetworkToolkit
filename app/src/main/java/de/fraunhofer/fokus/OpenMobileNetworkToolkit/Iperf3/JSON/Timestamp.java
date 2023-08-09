package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timestamp {

    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("timesecs")
    @Expose
    public Integer timesecs;

}
