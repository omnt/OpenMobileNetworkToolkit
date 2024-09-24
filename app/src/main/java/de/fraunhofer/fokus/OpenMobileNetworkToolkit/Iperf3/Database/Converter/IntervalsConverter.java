package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;

@ProvidedTypeConverter
public class IntervalsConverter {
    @TypeConverter
    public static ArrayList<Interval> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Interval>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Interval> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}