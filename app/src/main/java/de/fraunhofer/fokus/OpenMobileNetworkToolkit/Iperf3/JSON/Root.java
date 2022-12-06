
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Root {

    @SerializedName("start")
    @Expose
    public Start start;
    @SerializedName("intervals")
    @Expose
    public List<Interval> intervals = null;
    @SerializedName("end")
    @Expose
    public End end;

}
