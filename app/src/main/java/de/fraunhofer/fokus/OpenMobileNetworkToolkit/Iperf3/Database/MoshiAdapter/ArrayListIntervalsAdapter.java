/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.MoshiAdapter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Intervals;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;

public class ArrayListIntervalsAdapter {
    @ToJson
    List<Interval> ToJson(Intervals interval) {
        return interval.getIntervalArrayList();
    }

    @FromJson
    Intervals FromJson(List<Interval> listInterval) {
        Intervals intervals = new Intervals();
        intervals.setIntervals(new ArrayList<>(listInterval));
        return intervals;
    }
}