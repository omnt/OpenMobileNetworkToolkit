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

public class CpuUtilizationPercent {

    @SerializedName("host_total")
    @Expose
    public Float hostTotal;
    @SerializedName("host_user")
    @Expose
    public Float hostUser;
    @SerializedName("host_system")
    @Expose
    public Float hostSystem;
    @SerializedName("remote_total")
    @Expose
    public Float remoteTotal;
    @SerializedName("remote_user")
    @Expose
    public Float remoteUser;
    @SerializedName("remote_system")
    @Expose
    public Float remoteSystem;

}
