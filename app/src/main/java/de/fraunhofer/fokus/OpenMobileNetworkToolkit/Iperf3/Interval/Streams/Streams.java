package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Streams {
    private ArrayList<Stream> streams;
    public Streams(){
        this.streams = new ArrayList<>();
    }

    public void parse(JSONObject data) throws JSONException {
        JSONArray streams = data.getJSONArray("streams");
        for (int i = 0; i < streams.length(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            Stream s = new Stream();
            s.parse(stream);
            addStream(s);
        }
    }

    public void addStream(Stream stream){
        streams.add(stream);
    }
    public Stream getStream(int i) {
        return streams.get(i);
    }
}
