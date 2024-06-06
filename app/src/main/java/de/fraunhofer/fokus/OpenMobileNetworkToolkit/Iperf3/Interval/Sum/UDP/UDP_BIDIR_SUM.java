package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.UDP;

import org.json.JSONException;
import org.json.JSONObject;

public class UDP_BIDIR_SUM {

    UDP_UL_SUM ul;
    UDP_DL_SUM dl;
    public UDP_BIDIR_SUM() {
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        this.ul = new UDP_UL_SUM();
        this.dl = new UDP_DL_SUM();
        this.ul.parse(data.getJSONObject("sum"));
        this.dl.parse(data.getJSONObject("sum_bidir_reverse"));
    }

}
