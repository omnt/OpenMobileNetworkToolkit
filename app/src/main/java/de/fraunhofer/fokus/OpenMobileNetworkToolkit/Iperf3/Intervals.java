package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import com.influxdb.client.JSON;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Interval;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class Intervals {
    private ArrayList<Interval> intervals;;

    public Intervals(){
        this.intervals = new ArrayList<>();
    }
    public void addInterval(Interval interval){
        intervals.add(interval);
    }
    public ArrayList<Interval> getIntervalArrayList(){
        return intervals;
    }

}
