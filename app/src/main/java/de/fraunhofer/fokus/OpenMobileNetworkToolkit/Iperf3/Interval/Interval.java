package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.Streams;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Sum.Sum;
import org.json.JSONException;
import org.json.JSONObject;

public class Interval {
    private Streams streams;
    private Sum sum;
    public Sum sum_bidir_reverse;

    public Interval(){
        streams = new Streams();
    }
    public void parse(JSONObject data) throws JSONException {
        streams.parse(data.getJSONObject("streams"));
        sum = new Sum();
        sum.parse(data.getJSONObject("sum"));
        if(data.has("sum_bidir_reverse")){
            sum_bidir_reverse = new Sum();
            sum_bidir_reverse.parse(data.getJSONObject("sum_bidir_reverse"));
        }
    }

    public Streams getStreams() {
        return streams;
    }
    public Sum getSum() {
        return sum;
    }
}
