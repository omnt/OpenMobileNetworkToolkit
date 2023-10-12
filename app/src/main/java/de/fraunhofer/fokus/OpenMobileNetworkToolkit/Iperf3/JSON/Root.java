/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Root {

    @SerializedName("start")
    @Expose
    public Start start;
    @SerializedName("intervals")
    @Expose
    public List<Interval> intervals = null;
    @SerializedName("end")
    @Expose
    public End end;

}
