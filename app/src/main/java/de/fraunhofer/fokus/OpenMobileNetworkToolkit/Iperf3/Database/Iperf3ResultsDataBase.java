package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.IntervalsConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3InputConverter;

@Database(entities = {Iperf3RunResult.class}, version = 1, exportSchema = false)
@TypeConverters({IntervalsConverter.class})
public abstract class Iperf3ResultsDataBase extends RoomDatabase {
    private static volatile Iperf3ResultsDataBase INSTANCE;

    public abstract Iperf3RunResultDao iperf3RunResultDao();

    public static Iperf3ResultsDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Iperf3ResultsDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Iperf3ResultsDataBase.class, "iperf3_database")
                            .addTypeConverter(new Iperf3InputConverter())
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}