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

    @Query("UPDATE iperf3_result_database SET result=:result WHERE uid=:uid")
    void updateResult(String uid, int result);

    @Query("UPDATE iperf3_result_database SET uploaded=:uploaded WHERE uid=:uid")
    void updateUpload(String uid, boolean uploaded);

    @Query("UPDATE iperf3_result_database SET moved=:moved WHERE uid=:uid")
    void updateMove(String uid, boolean moved);

    @Update
    void update(Iperf3RunResult iperf3RunResult);

    @Delete
    void delete(Iperf3RunResult iperf3RunResult);
}
