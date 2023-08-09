package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Interval {

    @SerializedName("streams")
    @Expose
    public List<Stream> streams = null;
    @SerializedName("sum")
    @Expose
    public Sum sum;

}
