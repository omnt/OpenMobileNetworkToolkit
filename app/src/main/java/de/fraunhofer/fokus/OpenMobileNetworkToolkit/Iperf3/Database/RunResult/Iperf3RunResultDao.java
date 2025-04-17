/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3ErrorConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3IntervalsConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.MetricConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;

@Dao
public interface Iperf3RunResultDao {
    @Query("SELECT * FROM iperf3_result_database")
    LiveData<List<Iperf3RunResult>> getAll();

    @Query("SELECT uid FROM iperf3_result_database ORDER BY timestamp DESC")
    List<String> getIDs();

    @Query("SELECT * FROM iperf3_result_database WHERE uid = :comp_uid")
    Iperf3RunResult getRunResult(String comp_uid);

    @Query("SELECT timestamp FROM iperf3_result_database WHERE uid = :comp_uid")
    long getTimestampFromUid(String comp_uid);

    @Query("SELECT _intervals FROM iperf3_result_database WHERE uid = :comp_uid")
    @TypeConverters({Iperf3IntervalsConverter.class})
    Intervals getIntervals(String comp_uid);

    @Query("UPDATE iperf3_result_database SET _intervals = :intervals WHERE uid = :uid")
    @TypeConverters({Iperf3IntervalsConverter.class})
    void updateIntervals(String uid, Intervals intervals);
    @Query("UPDATE iperf3_result_database SET metricUL = :metricUL WHERE uid = :uid")
    @TypeConverters({MetricConverter.class})
    void updateMetricUL(String uid, MetricCalculator metricUL);

    @Query("UPDATE iperf3_result_database SET metricDL = :metricDL WHERE uid = :uid")
    @TypeConverters({MetricConverter.class})
    void updateMetricDL(String uid, MetricCalculator metricDL);

    @Query("UPDATE iperf3_result_database SET _start = :start WHERE uid = :uid")
    void updateStart(String uid, String start);

    @Query("UPDATE iperf3_result_database SET _end = :end WHERE uid = :uid")
    void updateEnd(String uid, String end);

    @Query("UPDATE iperf3_result_database SET uploaded = :uploaded WHERE uid = :uid")
    void updateUploaded(String uid, boolean uploaded);

    @Query("UPDATE iperf3_result_database SET result = :result WHERE uid = :uid")
    void updateResult(String uid, int result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Iperf3RunResult iperf3RunResult);

    @Query("SELECT metricDL FROM iperf3_result_database WHERE uid = :uid")
    @TypeConverters({MetricConverter.class})
    MetricCalculator getMetricDL(String uid);

    @Query("SELECT metricUL FROM iperf3_result_database WHERE uid = :uid")
    @TypeConverters({MetricConverter.class})
    MetricCalculator getMetricUL(String uid);

    @Query("SELECT error FROM iperf3_result_database WHERE uid = :uid")
    @TypeConverters({Iperf3ErrorConverter.class})
    Error getError(String uid);

    @Query("UPDATE iperf3_result_database SET error = :error WHERE uid = :uid")
    @TypeConverters({Iperf3ErrorConverter.class})
    void updateError(String uid, Error error);


}
