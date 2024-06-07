package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import org.json.JSONException;
import org.json.JSONObject;

public class TCP_DL_SUM extends TCP_SUM{
    public TCP_DL_SUM() {
        super();
        this.setSumType(SUM_TYPE.TCP_DL);
    }

    @Override
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
    }
}
