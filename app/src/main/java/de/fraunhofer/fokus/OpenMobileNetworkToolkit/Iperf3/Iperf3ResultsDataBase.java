package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Iperf3RunResult.class},
        version = 3
)
public abstract class Iperf3ResultsDataBase extends RoomDatabase {
    public abstract Iperf3RunResultDao iperf3RunResultDao();
    private static volatile Iperf3ResultsDataBase INSTANCE;

    static Iperf3ResultsDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Iperf3ResultsDataBase.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Iperf3ResultsDataBase.class, "iperf3_result_database")
                            .addTypeConverter(new Iperf3InputConverter())
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
