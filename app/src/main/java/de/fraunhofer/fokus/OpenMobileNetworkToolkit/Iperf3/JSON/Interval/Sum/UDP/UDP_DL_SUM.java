package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_DL_SUM extends UDP_SUM{
    private double jitter_ms;
    private int lost_packets;
    private double lost_percent;
    public UDP_DL_SUM() {
        super();
        this.setSumType(SUM_TYPE.UDP_DL);
    }

    @Override
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.jitter_ms = data.getDouble("jitter_ms");
        this.lost_packets = data.getInt("lost_packets");
        this.lost_percent = data.getDouble("lost_percent");

    }

    public double getJitter_ms() {
        return jitter_ms;
    }

    public int getLost_packets() {
        return lost_packets;
    }

    public double getLost_percent() {
        return lost_percent;
    }

}
