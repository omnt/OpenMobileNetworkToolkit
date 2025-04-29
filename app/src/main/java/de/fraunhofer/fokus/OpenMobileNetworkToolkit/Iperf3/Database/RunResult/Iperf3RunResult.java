/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Timestamp;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3ErrorConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3InputConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3IntervalsConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.Iperf3StartConverter;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.MetricConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;

@Entity(tableName = "iperf3_result_database")
@TypeConverters({Iperf3InputConverter.class, Iperf3IntervalsConverter.class, Iperf3StartConverter.class, MetricConverter.class})
public class Iperf3RunResult {
    @NonNull
    @PrimaryKey
    public String uid;

    @ColumnInfo(name = "result")
    public int result;

    @ColumnInfo(name = "uploaded")
    public boolean uploaded;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "input")
    public Iperf3Input input;

    @ColumnInfo(name = "_start")
    public Start start;

    @ColumnInfo(name = "_end")
    public String end;
    @ColumnInfo(name = "_intervals")
    public Intervals intervals;
    @ColumnInfo(name = "metricUL")
    public MetricCalculator metricUL;
    @ColumnInfo(name = "metricDL")
    public MetricCalculator metricDL;

    @ColumnInfo(name = "error")
    @TypeConverters({Iperf3ErrorConverter.class})
    public Error error;

    public Iperf3RunResult(String uid, int result, boolean upload, Iperf3Input input,
                           Timestamp timestamp) {
        this.uid = uid;
        this.result = result;
        this.uploaded = upload;
        this.input = input;
        this.timestamp = timestamp.getTime();
    }

    public Iperf3RunResult() {
    }
}