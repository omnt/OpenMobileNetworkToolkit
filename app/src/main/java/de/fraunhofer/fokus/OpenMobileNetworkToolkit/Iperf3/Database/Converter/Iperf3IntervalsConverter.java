package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.gson.Gson;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;

@ProvidedTypeConverter
public class Iperf3IntervalsConverter {
    @TypeConverter
    public Intervals StringToIperf3Intervals(String string) {
        return new Gson().fromJson(string, Intervals.class);
    }

    @TypeConverter
    public String Iperf3IntervalsToString(Intervals intervals) {
        return new Gson().toJson(intervals);
    }
}