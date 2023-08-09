package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;
import com.google.gson.Gson;

@ProvidedTypeConverter
public class Iperf3InputConverter {
    @TypeConverter
    public Iperf3Fragment.Iperf3Input StringToIperf3Input(String string) {
        return new Gson().fromJson(string, Iperf3Fragment.Iperf3Input.class);
    }

    @TypeConverter
    public String Iperf3InputToString(Iperf3Fragment.Iperf3Input example) {
        return new Gson().toJson(example);
    }
}
