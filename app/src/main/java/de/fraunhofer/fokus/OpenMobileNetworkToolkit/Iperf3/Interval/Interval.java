package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval;

import com.influxdb.client.JSON;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.Streams;
import org.json.JSONObject;

public class Interval {
    private Streams streams;
    public Interval(){
        streams = new Streams();
    }
    public void parse(JSONObject data){
        streams.parse(data);
    }
}
