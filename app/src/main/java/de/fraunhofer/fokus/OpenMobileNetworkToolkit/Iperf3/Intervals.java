package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.room.TypeConverters;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.IntervalsConverter;
import java.util.ArrayList;

public class Intervals {
    @TypeConverters({IntervalsConverter.class})
    private ArrayList<Interval> intervals;

    public Intervals(ArrayList<Interval> intervals){
        this.intervals = intervals;
    }
    public Intervals(){
        this.intervals = new ArrayList<>();
    }
    public void addInterval(Interval interval){
        intervals.add(interval);
    }
    public ArrayList<Interval> getIntervalArrayList(){
        return intervals;
    }

    public void setIntervals(ArrayList<Interval> intervals){
        this.intervals = intervals;
    }
}