package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface Iperf3RunResultDao {
    @Query("SELECT * FROM iperf3_result_database")
    LiveData<List<Iperf3RunResult>> getAll();

    @Query("SELECT COUNT(*) FROM iperf3_result_database")
    LiveData<Integer> getLength();

    @Query("SELECT uid FROM iperf3_result_database")
    List<String> getIDs();

    @Query("SELECT * FROM iperf3_result_database WHERE uid = :comp_uid")
    Iperf3RunResult getRunResult(String comp_uid);

    @Insert
    void insertAll(Iperf3RunResult... iperf3RunResults);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insert(Iperf3RunResult iperf3RunResult);

    @Update
    void update(Iperf3RunResult iperf3RunResult);

    @Delete
    void delete(Iperf3RunResult iperf3RunResult);
}
