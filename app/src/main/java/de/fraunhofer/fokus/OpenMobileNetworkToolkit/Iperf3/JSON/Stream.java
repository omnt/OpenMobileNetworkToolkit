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

public class Stream {

    @SerializedName("socket")
    @Expose
    public int socket;
    @SerializedName("start")
    @Expose
    public float start;
    @SerializedName("end")
    @Expose
    public float end;
    @SerializedName("seconds")
    @Expose
    public float seconds;
    @SerializedName("bytes")
    @Expose
    public long bytes;
    @SerializedName("bits_per_second")
    @Expose
    public float bitsPerSecond;
    @SerializedName("retransmits")
    @Expose
    public int retransmits;
    @SerializedName("snd_cwnd")
    @Expose
    public int sndCwnd;
    @SerializedName("packets")
    @Expose
    public int packets;
    @SerializedName("jitter_ms")
    @Expose
    public float jitterMs;
    @SerializedName("lost_packets")
    @Expose
    public int lostPackets;
    @SerializedName("lost_percent")
    @Expose
    public float lostPercent;
    @SerializedName("snd_wnd")
    @Expose
    public int sndWnd;
    @SerializedName("rtt")
    @Expose
    public int rtt;
    @SerializedName("rttvar")
    @Expose
    public int rttvar;
    @SerializedName("pmtu")
    @Expose
    public int outOfOrder;
    @SerializedName("out_of_order")
    @Expose
    public int pmtu;
    @SerializedName("omitted")
    @Expose
    public boolean omitted;
    @SerializedName("sender")
    @Expose
    public boolean sender;

}
