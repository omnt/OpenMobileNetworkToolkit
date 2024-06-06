package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectingTo{
    public String host;
    public int port;

    public void parse(JSONObject data) throws JSONException {
        this.host = data.getString("host");
        this.port = data.getInt("port");
    }
}
