package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams;

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

    private STREAM_TYPE streamType;

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

    public int getSocket() {
        return socket;
    }
    public int getStart() {
        return start;
    }
    public double getEnd() {
        return end;
    }
    public double getSeconds() {
        return seconds;
    }
    public long getBytes() {
        return bytes;
    }
    public double getBits_per_second() {
        return bits_per_second;
    }
    public boolean getOmitted() {
        return omitted;
    }
    public boolean getSender() {
        return sender;
    }

    public STREAM_TYPE getStreamType() {
        return streamType;
    }

    public void setStreamType(
        STREAM_TYPE streamType) {
        this.streamType = streamType;
    }
}
