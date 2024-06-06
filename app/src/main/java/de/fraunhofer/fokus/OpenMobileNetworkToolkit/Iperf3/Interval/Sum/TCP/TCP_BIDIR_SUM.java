package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.TCP;

import org.json.JSONException;
import org.json.JSONObject;

public class TCP_BIDIR_SUM extends TCP_SUM{
    TCP_UL_SUM ul;
    TCP_DL_SUM dl;
    public TCP_BIDIR_SUM() {
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        this.ul = new TCP_UL_SUM();
        this.dl = new TCP_DL_SUM();
        this.ul.parse(data.getJSONObject("sum"));
        this.dl.parse(data.getJSONObject("sum_bidir_reverse"));
    }
}
