package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.UDP;

import org.json.JSONException;
import org.json.JSONObject;

public class UDP_DL_SUM extends UDP_SUM{
    private double jitter_ms;
    private int lost_packets;
    private int lost_percent;
    public UDP_DL_SUM() {
        super();
    }

    @Override
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.jitter_ms = data.getDouble("jitter_ms");
        this.lost_packets = data.getInt("lost_packets");
        this.lost_percent = data.getInt("lost_percent");
    }
}
