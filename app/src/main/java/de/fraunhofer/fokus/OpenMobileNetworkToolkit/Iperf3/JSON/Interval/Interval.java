package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval;

import static de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE.*;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.STREAM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Streams;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_BIDIR_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_UL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_BIDIR_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_UL_SUM;
import org.json.JSONException;
import org.json.JSONObject;

public class Interval {
    private Streams streams;
    private Sum sum;
    public Sum sumBidirReverse;

    public Interval(){
        streams = new Streams();
    }

    private SUM_TYPE getSumType(JSONObject data) throws JSONException {
        boolean sender = data.getBoolean("sender");
        if(sender){
            if(data.has("retransmits")) return SUM_TYPE.TCP_UL;
            if(data.has("packets")) return SUM_TYPE.UDP_UL;
        }
        if(data.has("jitter_ms")) return SUM_TYPE.UDP_DL;
        return SUM_TYPE.TCP_DL;
    }

    public Sum identifySum(JSONObject data) throws JSONException {
        Sum identifiedSum = null;
        switch (getSumType(data)){
            case TCP_DL:
                identifiedSum = new TCP_DL_SUM();
                ((TCP_DL_SUM) identifiedSum).parse(data);
                break;
            case TCP_UL:
                identifiedSum = new TCP_UL_SUM();
                ((TCP_UL_SUM) identifiedSum).parse(data);
                break;
            case UDP_DL:
                identifiedSum = new UDP_DL_SUM();
                ((UDP_DL_SUM) identifiedSum).parse(data);
                break;
            case UDP_UL:
                identifiedSum = new UDP_UL_SUM();
                ((UDP_UL_SUM) identifiedSum).parse(data);
                break;
        }
        return identifiedSum;
    }

    public void parse(JSONObject data) throws JSONException {
        streams.parse(data.getJSONArray("streams"));
        sum = identifySum(data.getJSONObject("sum"));
        if(data.has("sum_bidir_reverse")){
            sumBidirReverse = identifySum(data.getJSONObject("sum_bidir_reverse"));
        }
    }

    public Streams getStreams() {
        return streams;
    }
    public Sum getSum() {
        return sum;
    }
    public Sum getSumBidirReverse() {
        return sumBidirReverse;
    }
}
