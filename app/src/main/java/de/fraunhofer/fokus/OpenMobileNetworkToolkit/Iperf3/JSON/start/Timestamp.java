package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import org.json.JSONException;
import org.json.JSONObject;

public class Timestamp{
    public String time;
    public int timesecs;

    public void parse(JSONObject data) throws JSONException {
        this.time = data.getString("time");
        this.timesecs = data.getInt("timesecs");
    }
}
