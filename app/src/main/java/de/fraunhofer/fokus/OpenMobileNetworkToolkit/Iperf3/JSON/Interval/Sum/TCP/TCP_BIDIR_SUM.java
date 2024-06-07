package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import org.json.JSONException;
import org.json.JSONObject;

public class TCP_BIDIR_SUM extends TCP_SUM{
    Sum bidirReverse;
    SUM_TYPE sumType;
    public TCP_BIDIR_SUM() {
        super();
        this.sumType = SUM_TYPE.TCP_BIDIR;
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data.getJSONObject("sum"));
        this.bidirReverse.parse(data.getJSONObject("sum_bidir_reverse"));

    }

    public SUM_TYPE getSumType() {
        return sumType;
    }
}
