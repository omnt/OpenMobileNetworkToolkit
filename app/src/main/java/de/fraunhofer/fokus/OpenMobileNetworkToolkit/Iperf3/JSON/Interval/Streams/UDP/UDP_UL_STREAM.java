package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.STREAM_TYPE;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_UL_STREAM extends UDP_STREAM {
    public UDP_UL_STREAM(){
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.setStreamType(STREAM_TYPE.UDP_UL);
    }
}
