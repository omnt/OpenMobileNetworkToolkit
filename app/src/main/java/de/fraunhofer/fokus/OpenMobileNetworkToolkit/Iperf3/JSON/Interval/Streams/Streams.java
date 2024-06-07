package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP.TCP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP.TCP_UL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP.UDP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP.UDP_UL_STREAM;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Streams {
    private ArrayList<Stream> streams;
    public Streams(){
        this.streams = new ArrayList<>();
    }

    private STREAM_TYPE identifyStream(JSONObject data) throws JSONException {
        boolean sender = data.getBoolean("sender");
        if(sender){
            if(data.has("retransmits")) return STREAM_TYPE.TCP_UL;
            if(data.has("packets")) return STREAM_TYPE.UDP_UL;
        }
        if(data.has("jitter_ms")) return STREAM_TYPE.UDP_DL;
        return STREAM_TYPE.TCP_DL;
    }

    private Stream parseStream(JSONObject data) throws JSONException{
        STREAM_TYPE type = identifyStream(data);
        Stream stream = null;
        switch (type) {
            case TCP_UL:
                stream = new TCP_UL_STREAM();
                break;
            case TCP_DL:
                stream = new TCP_DL_STREAM();
                break;
            case UDP_UL:
                stream = new UDP_UL_STREAM();
                break;
            case UDP_DL:
                stream = new UDP_DL_STREAM();
                break;
            case UNKNOWN:
                return stream;
        }
        stream.parse(data);
        return stream;
    }

    public void parse(JSONArray streams) throws JSONException {
        for (int i = 0; i < streams.length(); i++) {
            JSONObject streamJSONObject = streams.getJSONObject(i);
            Stream stream = parseStream(streamJSONObject);
            if(stream == null){
                System.out.println("Stream is null!");
                continue;
            }
            addStream(stream);
        }
    }
    public int size(){
        return streams.size();
    }
    public ArrayList<Stream> getStreamArrayList() {
        return streams;
    }
    public void addStream(Stream stream){
        streams.add(stream);
    }
    public Stream getStream(int i) {
        return streams.get(i);
    }
}
