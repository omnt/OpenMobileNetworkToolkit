package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.UDP;

import org.json.JSONException;
import org.json.JSONObject;

public class UDP_UL_SUM extends UDP_SUM{
    private int packets;
    public UDP_UL_SUM() {
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.packets = data.getInt("packets");
    }
}
