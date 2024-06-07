package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import java.util.ArrayList;

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
