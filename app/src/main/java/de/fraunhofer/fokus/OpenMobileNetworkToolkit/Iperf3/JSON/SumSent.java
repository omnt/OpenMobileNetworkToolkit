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

public class SumSent {

    @SerializedName("start")
    @Expose
    public int start;
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
    @SerializedName("sender")
    @Expose
    public boolean sender;
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
}
