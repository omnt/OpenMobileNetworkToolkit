
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SumReceived {

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
    @SerializedName("sender")
    @Expose
    public Boolean sender;

}
