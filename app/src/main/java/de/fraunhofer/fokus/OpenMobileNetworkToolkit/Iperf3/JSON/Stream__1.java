
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stream__1 {

    @SerializedName("sender")
    @Expose
    public Sender sender;
    @SerializedName("receiver")
    @Expose
    public Receiver receiver;

}
