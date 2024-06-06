package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Start{
    private ArrayList<Connected> connected = new ArrayList<Connected>();
    private String version;
    private String system_info;
    private Timestamp timestamp;
    private ConnectingTo connecting_to;
    private String cookie;
    private int tcp_mss_default;
    private int target_bitrate;
    private int fq_rate;
    private int sock_bufsize;
    private int sndbuf_actual;
    private int rcvbuf_actual;
    private TestStart test_start;

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
    public ArrayList<Connected> getConnected() {
        return connected;
    }
    public String getVersion() {
        return version;
    }
    public String getSystem_info() {
        return system_info;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public ConnectingTo getConnecting_to() {
        return connecting_to;
    }
    public String getCookie() {
        return cookie;
    }
    public int getTcp_mss_default() {
        return tcp_mss_default;
    }
    public int getTarget_bitrate() {
        return target_bitrate;
    }
    public int getFq_rate() {
        return fq_rate;
    }
    public int getSock_bufsize() {
        return sock_bufsize;
    }
    public int getSndbuf_actual() {
        return sndbuf_actual;
    }
    public int getRcvbuf_actual() {
        return rcvbuf_actual;
    }
    public TestStart getTest_start() {
        return test_start;
    }

}
