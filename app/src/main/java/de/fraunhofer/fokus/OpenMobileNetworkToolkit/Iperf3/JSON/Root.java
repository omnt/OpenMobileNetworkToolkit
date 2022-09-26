
package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
