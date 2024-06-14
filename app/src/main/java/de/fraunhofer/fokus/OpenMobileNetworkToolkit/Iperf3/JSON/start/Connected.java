package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import org.json.JSONException;
import org.json.JSONObject;

public class Connected {
    public int socket;
    public String local_host;
    public int local_port;
    public String remote_host;
    public int remote_port;

    public void parse(JSONObject data) throws JSONException {
        this.socket = data.getInt("socket");
        this.local_host = data.getString("local_host");
        this.local_port = data.getInt("local_port");
        this.remote_host = data.getString("remote_host");
        this.remote_port = data.getInt("remote_port");

    }
}
