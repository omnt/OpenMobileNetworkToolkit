
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConnectingTo {

    @SerializedName("host")
    @Expose
    public String host;
    @SerializedName("port")
    @Expose
    public Integer port;

}
