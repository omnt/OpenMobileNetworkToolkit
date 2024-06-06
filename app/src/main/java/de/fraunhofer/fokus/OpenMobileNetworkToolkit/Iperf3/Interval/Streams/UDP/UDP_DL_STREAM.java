package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.Stream;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_DL_STREAM extends UDP_STREAM {
    public double jitter_ms;
    public int lost_packets;
    public int packets;
    public int lost_percent;
    public UDP_DL_STREAM(){
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.jitter_ms = data.getDouble("jitter_ms");
        this.lost_packets = data.getInt("lost_packets");
        this.packets = data.getInt("packets");
        this.lost_percent = data.getInt("lost_percent");
    }
}
