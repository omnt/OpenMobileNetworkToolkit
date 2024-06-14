package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Stream;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_STREAM extends Stream {
    public int packets;
    public UDP_STREAM(){
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
    }
}
