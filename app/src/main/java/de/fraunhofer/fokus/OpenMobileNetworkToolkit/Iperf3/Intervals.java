/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import java.util.ArrayList;

public class Intervals {
    private final ArrayList<Interval> intervals;

    public Intervals(){
        this.intervals = new ArrayList<>();
    }
    public void addInterval(Interval interval){
        intervals.add(interval);
    }
    public ArrayList<Interval> getIntervalArrayList(){
        return intervals;
    }

    public void setIntervals(ArrayList<Interval> intervals) {
        this.intervals.clear();
        this.intervals.addAll(intervals);
    }
}
