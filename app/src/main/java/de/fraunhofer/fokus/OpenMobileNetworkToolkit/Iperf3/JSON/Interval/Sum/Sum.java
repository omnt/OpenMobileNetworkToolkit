package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum;

import org.json.JSONException;
import org.json.JSONObject;

public class Sum {
    private int start;
    private float end;
    private float seconds;
    private long bytes;
    private double bits_per_second;
    private boolean omitted;
    private boolean sender;
    private SUM_TYPE sumType;
    public Sum(){
    }
    public void parse(JSONObject data) throws JSONException {
        this.start = data.getInt("start");
        this.end = (float) data.getDouble("end");
        this.seconds = (float) data.getDouble("seconds");
        this.bytes = data.getLong("bytes");
        this.bits_per_second = data.getDouble("bits_per_second");
        this.omitted = data.getBoolean("omitted");
        this.sender = data.getBoolean("sender");
    }
    public int getStart() {
        return start;
    }
    public float getEnd() {
        return end;
    }
    public float getSeconds() {
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
    public SUM_TYPE getSumType() {
        return sumType;
    }
    public void setSumType(SUM_TYPE sumType) {
        this.sumType = sumType;
    }

}
