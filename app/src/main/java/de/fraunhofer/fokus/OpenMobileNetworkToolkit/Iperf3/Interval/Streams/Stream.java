package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams;

import org.json.JSONException;
import org.json.JSONObject;

public class Stream {
    private int socket;
    private int start;
    private double end;
    private double seconds;
    private long bytes;
    private double bits_per_second;
    private boolean omitted;
    private boolean sender;

    public Stream(){
    }
    public void parse(JSONObject data) throws JSONException {
        this.socket = data.getInt("socket");
        this.start = data.getInt("start");
        this.end = data.getDouble("end");
        this.seconds = data.getDouble("seconds");
        this.bytes = data.getLong("bytes");
        this.bits_per_second = data.getDouble("bits_per_second");
        this.omitted = data.getBoolean("omitted");
        this.sender = data.getBoolean("sender");

    }

}
