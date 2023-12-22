/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Connected {

    @SerializedName("socket")
    @Expose
    public int socket;
    @SerializedName("local_host")
    @Expose
    public String localHost;
    @SerializedName("local_port")
    @Expose
    public int localPort;
    @SerializedName("remote_host")
    @Expose
    public String remoteHost;
    @SerializedName("remote_port")
    @Expose
    public int remotePort;

}
