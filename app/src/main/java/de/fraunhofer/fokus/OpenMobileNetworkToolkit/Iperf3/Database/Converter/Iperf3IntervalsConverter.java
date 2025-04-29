/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.MoshiAdapter.ArrayListIntervalsAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.MoshiAdapter.ArrayListStreamsAdapter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.STREAM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Stream;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Streams;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP.TCP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP.TCP_UL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP.UDP_DL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.UDP.UDP_UL_STREAM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP.TCP_UL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_DL_SUM;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP.UDP_UL_SUM;

@ProvidedTypeConverter
public class Iperf3IntervalsConverter {


    @TypeConverter
    public Intervals stringToIperf3Intervals(String string) {
        Moshi moshi = new Moshi.Builder()
                .add(new ArrayListIntervalsAdapter())
                .add(new ArrayListStreamsAdapter())

                .add(PolymorphicJsonAdapterFactory.of(Stream.class, "streamType")
                        .withSubtype(TCP_DL_STREAM.class, STREAM_TYPE.TCP_DL.toString())
                        .withSubtype(TCP_UL_STREAM.class, STREAM_TYPE.TCP_UL.toString())
                        .withSubtype(UDP_DL_STREAM.class, STREAM_TYPE.UDP_DL.toString())
                        .withSubtype(UDP_UL_STREAM.class, STREAM_TYPE.UDP_UL.toString()))
                .add(PolymorphicJsonAdapterFactory.of(Sum.class, "sumType")
                        .withSubtype(TCP_DL_SUM.class, SUM_TYPE.TCP_DL.toString())
                        .withSubtype(TCP_UL_SUM.class, SUM_TYPE.TCP_UL.toString())
                        .withSubtype(UDP_DL_SUM.class, SUM_TYPE.UDP_DL.toString())
                        .withSubtype(UDP_UL_SUM.class, SUM_TYPE.UDP_UL.toString()))
                .build();
        JsonAdapter<Intervals> jsonAdapter = moshi.adapter(Intervals.class);
        Intervals intervals = new Intervals();
        try {
            intervals = jsonAdapter.fromJson(string);
        } catch (IOException e) {
        }


        return intervals;
    }

    @TypeConverter
    public String iperf3IntervalsToString(Intervals intervals) {

        Moshi moshi = new Moshi.Builder()
                .add(PolymorphicJsonAdapterFactory.of(Stream.class, "streamType")
                        .withSubtype(TCP_DL_STREAM.class, STREAM_TYPE.TCP_DL.toString())
                        .withSubtype(TCP_UL_STREAM.class, STREAM_TYPE.TCP_UL.toString())
                        .withSubtype(UDP_DL_STREAM.class, STREAM_TYPE.UDP_DL.toString())
                        .withSubtype(UDP_UL_STREAM.class, STREAM_TYPE.UDP_UL.toString()))
                .add(PolymorphicJsonAdapterFactory.of(Sum.class, "sumType")
                        .withSubtype(TCP_DL_SUM.class, SUM_TYPE.TCP_DL.toString())
                        .withSubtype(TCP_UL_SUM.class, SUM_TYPE.TCP_UL.toString())
                        .withSubtype(UDP_DL_SUM.class, SUM_TYPE.UDP_DL.toString())
                        .withSubtype(UDP_UL_SUM.class, SUM_TYPE.UDP_UL.toString()))
                .add(new ArrayListIntervalsAdapter())
                .add(new ArrayListStreamsAdapter())
                .build();
        JsonAdapter<Intervals> jsonAdapter = moshi.adapter(Intervals.class);

        return jsonAdapter.toJson(intervals);
    }
}
