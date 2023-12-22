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

import java.util.List;

public class Start {

    @SerializedName("connected")
    @Expose
    public List<Connected> connected = null;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("system_info")
    @Expose
    public String systemInfo;
    @SerializedName("timestamp")
    @Expose
    public Timestamp timestamp;
    @SerializedName("connecting_to")
    @Expose
    public ConnectingTo connectingTo;
    @SerializedName("cookie")
    @Expose
    public String cookie;
    @SerializedName("tcp_mss_default")
    @Expose
    public int tcpMssDefault;
    @SerializedName("target_bitrate")
    @Expose
    public int targetBitrate;
    @SerializedName("sock_bufsize")
    @Expose
    public int sockBufsize;
    @SerializedName("sndbuf_actual")
    @Expose
    public int sndbufActual;
    @SerializedName("rcvbuf_actual")
    @Expose
    public int rcvbufActual;
    @SerializedName("test_start")
    @Expose
    public TestStart testStart;

}
