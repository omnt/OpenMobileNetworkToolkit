package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Start{
    public ArrayList<Connected> connected = new ArrayList<Connected>();
    public String version;
    public String system_info;
    public Timestamp timestamp;
    public ConnectingTo connecting_to;
    public String cookie;
    public int tcp_mss_default;
    public int target_bitrate;
    public int fq_rate;
    public int sock_bufsize;
    public int sndbuf_actual;
    public int rcvbuf_actual;
    public TestStart test_start;

    public Start(){
    }
    public void parseStart(JSONObject data) throws JSONException {
        JSONArray connected = data.getJSONArray("connected");
        for (int i = 0; i < connected.length(); i++) {
            Connected connectedObj = new Connected();
            connectedObj.parse(connected.getJSONObject(i));
            this.connected.add(connectedObj);
        }
        this.version = data.getString("version");
        this.system_info = data.getString("system_info");
        this.timestamp = new Timestamp();
        this.timestamp.parse(data.getJSONObject("timestamp"));
        this.connecting_to = new ConnectingTo();
        this.connecting_to.parse(data.getJSONObject("connecting_to"));
        this.cookie = data.getString("cookie");
        this.tcp_mss_default = data.getInt("tcp_mss_default");
        this.target_bitrate = data.getInt("target_bitrate");
        this.fq_rate = data.getInt("fq_rate");
        this.sock_bufsize = data.getInt("sock_bufsize");
        this.sndbuf_actual = data.getInt("sndbuf_actual");
        this.rcvbuf_actual = data.getInt("rcvbuf_actual");
        this.test_start = new TestStart();
        this.test_start.parse(data.getJSONObject("test_start"));
    }
}
