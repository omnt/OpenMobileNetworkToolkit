package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import org.json.JSONException;
import org.json.JSONObject;

public class TestStart{
    public String protocol;
    public int num_streams;
    public int blksize;
    public int omit;
    public int duration;
    public int bytes;
    public int blocks;
    public int reverse;
    public int tos;
    public int target_bitrate;
    public boolean bidir;
    public int fqrate;
    public int interval;

    public void parse(JSONObject data) throws JSONException {
        this.protocol = data.getString("protocol");
        this.num_streams = data.getInt("num_streams");
        this.blksize = data.getInt("blksize");
        this.omit = data.getInt("omit");
        this.duration = data.getInt("duration");
        this.bytes = data.getInt("bytes");
        this.blocks = data.getInt("blocks");
        this.reverse = data.getInt("reverse");
        this.tos = data.getInt("tos");
        this.target_bitrate = data.getInt("target_bitrate");
        this.bidir = data.getBoolean("bidir");
        this.fqrate = data.getInt("fqrate");
        this.interval = data.getInt("interval");
    }
}
