package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.TCP;

import org.json.JSONException;
import org.json.JSONObject;

public class TCP_UL_SUM extends TCP_SUM{
    public int retransmits;
    public TCP_UL_SUM() {
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.retransmits = data.getInt("retransmits");
    }
}
