package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Iperf3RunResultDao {
    @Query("SELECT * FROM iperf3_result_database")
    List<Iperf3RunResult> getAll();

    @Query("SELECT COUNT(*) FROM iperf3_result_database")
    int getLength();

    @Query("SELECT * FROM iperf3_result_database WHERE uid = :comp_uid")
    int getRunResult(String comp_uid);

    @Insert
    void insertAll(Iperf3RunResult... iperf3RunResults);

    @Insert
    void insert(Iperf3RunResult iperf3RunResult);

    @Update
    void update(Iperf3RunResult iperf3RunResult);

    @Delete
    void delete(Iperf3RunResult iperf3RunResult);
}
