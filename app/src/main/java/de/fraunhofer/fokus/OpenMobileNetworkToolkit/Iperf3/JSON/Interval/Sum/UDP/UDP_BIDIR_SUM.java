package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_BIDIR_SUM extends UDP_SUM{

    Sum bidirReverse;
    SUM_TYPE sumType;
    public UDP_BIDIR_SUM() {
        super();
        sumType = SUM_TYPE.UDP_BIDIR;
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data.getJSONObject("sum"));
        this.bidirReverse.parse(data.getJSONObject("sum_bidir_reverse"));

    }

    public SUM_TYPE getSumType() {
        return sumType;
    }
}
